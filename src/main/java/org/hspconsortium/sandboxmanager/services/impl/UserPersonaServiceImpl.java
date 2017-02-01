package org.hspconsortium.sandboxmanager.services.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.hspconsortium.platform.messaging.model.user.SandboxUserInfo;
import org.hspconsortium.sandboxmanager.model.UserPersona;
import org.hspconsortium.sandboxmanager.model.Visibility;
import org.hspconsortium.sandboxmanager.repositories.UserPersonaRepository;
import org.hspconsortium.sandboxmanager.services.UserPersonaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.net.ssl.SSLContext;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class UserPersonaServiceImpl implements UserPersonaService {

    private static Logger LOGGER = LoggerFactory.getLogger(UserPersonaServiceImpl.class.getName());
    private final UserPersonaRepository repository;

    @Value("${hspc.platform.messaging.sandboxUserEndpointURL}")
    private String sandboxUserEndpointURL;


    @Inject
    public UserPersonaServiceImpl(final UserPersonaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public UserPersona save(UserPersona userPersona) {
        return repository.save(userPersona);
    }

    @Override
    public UserPersona getById(final int id) {
        return  repository.findOne(id);
    }

    @Override
    public UserPersona findByLdapId(String ldapId) {
        return repository.findByLdapId(ldapId);
    }

    @Override
    public UserPersona findByLdapIdAndSandboxId(final String ldapId, final String sandboxId) {
        return  repository.findByLdapIdAndSandboxId(ldapId, sandboxId);
    }

    @Override
    public List<UserPersona> findBySandboxIdAndCreatedByOrVisibility(String sandboxId, String createdBy, Visibility visibility) {
        return  repository.findBySandboxIdAndCreatedByOrVisibility(sandboxId, createdBy, visibility);
    }

    @Override
    public List<UserPersona> findBySandboxIdAndCreatedBy(String sandboxId, String createdBy) {
        return  repository.findBySandboxIdAndCreatedBy(sandboxId, createdBy);
    }

    @Override
    public List<UserPersona> findBySandboxId(final String sandboxId) {
        return  repository.findBySandboxId(sandboxId);
    }

    @Override
    @Transactional
    public void delete(final int id) {
        repository.delete(id);
    }

    @Override
    public void delete(UserPersona userPersona, String bearerToken) {
        String url = this.sandboxUserEndpointURL + "/" + userPersona.getLdapId();

        HttpDelete deleteRequest = new HttpDelete(url);
        deleteRequest.setHeader("Authorization", "BEARER " + bearerToken);

        SSLContext sslContext = null;
        try {
            sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).useSSL().build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            LOGGER.error("Error loading ssl context", e);
            throw new RuntimeException(e);
        }
        HttpClientBuilder builder = HttpClientBuilder.create();
        SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        builder.setSSLSocketFactory(sslConnectionFactory);
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", sslConnectionFactory)
                .register("http", new PlainConnectionSocketFactory())
                .build();
        HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(registry);
        builder.setConnectionManager(ccm);

        CloseableHttpClient httpClient = builder.build();

        try (CloseableHttpResponse closeableHttpResponse = httpClient.execute(deleteRequest)) {
            if (closeableHttpResponse.getStatusLine().getStatusCode() != 200) {
                HttpEntity rEntity = closeableHttpResponse.getEntity();
                String responseString = EntityUtils.toString(rEntity, "UTF-8");
                String errorMsg = String.format("There was a problem deleting the sandbox user.\n" +
                                "Response Status : %s .\nResponse Detail :%s. \nUrl: :%s",
                        closeableHttpResponse.getStatusLine(),
                        responseString,
                        url);
                LOGGER.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }
            delete(userPersona.getId());
        } catch (IOException e) {
            LOGGER.error("Error posting to " + url, e);
            throw new RuntimeException(e);
        } finally {
            try {
                httpClient.close();
            }catch (IOException e) {
                LOGGER.error("Error closing HttpClient");
            }
        }
    }

    @Override
    @Transactional
    public UserPersona create(UserPersona userPersona, String bearerToken) throws UnsupportedEncodingException {

        SandboxUserInfo sandboxUserInfo = createUser(userPersona);
        userPersona.setCreatedTimestamp(new Timestamp(new Date().getTime()));

        return createOrUpdate(userPersona, sandboxUserInfo, bearerToken);
    }

    @Override
    @Transactional
    public UserPersona update(UserPersona userPersona, String bearerToken) throws UnsupportedEncodingException {

        SandboxUserInfo sandboxUserInfo = getSandboxUserInfo(userPersona.getLdapId(), bearerToken);
        sandboxUserInfo = populateUser(sandboxUserInfo, userPersona);
        return createOrUpdate(userPersona,sandboxUserInfo, bearerToken);
    }

    private UserPersona createOrUpdate(final UserPersona userPersona, final SandboxUserInfo sandboxUserInfo, final String bearerToken) throws UnsupportedEncodingException {
        String url = this.sandboxUserEndpointURL;

        HttpPut putRequest = new HttpPut(url);
        putRequest.setEntity(new StringEntity(toJson(sandboxUserInfo)));
        putRequest.setHeader("Accept", "application/json");
        putRequest.setHeader("Content-type", "application/json");
        putRequest.setHeader("Authorization", "BEARER " + bearerToken);

        SSLContext sslContext = null;
        try {
            sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).useSSL().build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            LOGGER.error("Error loading ssl context", e);
            throw new RuntimeException(e);
        }
        HttpClientBuilder builder = HttpClientBuilder.create();
        SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        builder.setSSLSocketFactory(sslConnectionFactory);
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", sslConnectionFactory)
                .register("http", new PlainConnectionSocketFactory())
                .build();
        HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(registry);
        builder.setConnectionManager(ccm);

        CloseableHttpClient httpClient = builder.build();

        try (CloseableHttpResponse closeableHttpResponse = httpClient.execute(putRequest)) {
            if (closeableHttpResponse.getStatusLine().getStatusCode() != 200) {
                HttpEntity rEntity = closeableHttpResponse.getEntity();
                String responseString = EntityUtils.toString(rEntity, "UTF-8");
                String errorMsg = String.format("There was a problem creating the sandbox user.\n" +
                                "Response Status : %s .\nResponse Detail :%s. \nUrl: :%s",
                        closeableHttpResponse.getStatusLine(),
                        responseString,
                        url);
                LOGGER.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }

            return save(userPersona);
        } catch (IOException e) {
            LOGGER.error("Error posting to " + url, e);
            throw new RuntimeException(e);
        } finally {
            try {
                httpClient.close();
            }catch (IOException e) {
                LOGGER.error("Error closing HttpClient");
            }
        }
    }

    private SandboxUserInfo getSandboxUserInfo(final String userId, final String bearerToken) throws UnsupportedEncodingException {
        String url = this.sandboxUserEndpointURL + "/" + userId;

        HttpGet getRequest = new HttpGet(url);
        getRequest.setHeader("Accept", "application/json");
        getRequest.setHeader("Authorization", "BEARER " + bearerToken);

        SSLContext sslContext = null;
        try {
            sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).useSSL().build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            LOGGER.error("Error loading ssl context", e);
            throw new RuntimeException(e);
        }
        HttpClientBuilder builder = HttpClientBuilder.create();
        SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        builder.setSSLSocketFactory(sslConnectionFactory);
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", sslConnectionFactory)
                .register("http", new PlainConnectionSocketFactory())
                .build();
        HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(registry);
        builder.setConnectionManager(ccm);

        CloseableHttpClient httpClient = builder.build();

        try (CloseableHttpResponse closeableHttpResponse = httpClient.execute(getRequest)) {
            if (closeableHttpResponse.getStatusLine().getStatusCode() != 200) {
                HttpEntity rEntity = closeableHttpResponse.getEntity();
                String responseString = EntityUtils.toString(rEntity, "UTF-8");
                String errorMsg = String.format("There was a problem getting the sandbox user.\n" +
                                "Response Status : %s .\nResponse Detail :%s. \nUrl: :%s",
                        closeableHttpResponse.getStatusLine(),
                        responseString,
                        url);
                LOGGER.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }

            HttpEntity httpEntity = closeableHttpResponse.getEntity();
            return fromJson(EntityUtils.toString(httpEntity));
        } catch (IOException e) {
            LOGGER.error("Error posting to " + url, e);
            throw new RuntimeException(e);
        } finally {
            try {
                httpClient.close();
            }catch (IOException e) {
                LOGGER.error("Error closing HttpClient");
            }
        }
    }

    private SandboxUserInfo populateUser(SandboxUserInfo userInfo, UserPersona userPersona) {
        userInfo.setDisplayName(userPersona.getLdapName());
        userInfo.setProfileUrl(userPersona.getResourceUrl());
        userInfo.setUserPassword(userPersona.getPassword());
        return userInfo;
    }


    private SandboxUserInfo createUser(UserPersona userPersona) {
        SandboxUserInfo userInfo = new SandboxUserInfo();
        userInfo.setCn(UUID.randomUUID().toString().replace("-",""));
        userInfo.setLastName(UUID.randomUUID().toString());
        userInfo.setFirstName(UUID.randomUUID().toString());
//        userInfo.setOrganization("Intermountain Healthcare");
//        userInfo.setOrganizationName("Intermountain Healthcare Name");
        userInfo.setDisplayName(userPersona.getLdapName());
//        userInfo.setEmployeeNumber("896512");
        userInfo.setEmail("none@nowhere.com");
//        userInfo.setLdapHost("ldap://lpv-hdsvnev02.co.ihc.com:10389");
        userInfo.setProfileUrl(userPersona.getResourceUrl());
        userInfo.setUserId(userPersona.getLdapId());
        userInfo.setUserPassword(userPersona.getPassword());
        userInfo.setDistinctName("cn=" + userInfo.getCn());
        return userInfo;
    }

    private String toJson(SandboxUserInfo userInfo) {
        Gson gson = new Gson();
        Type type = new TypeToken<SandboxUserInfo>() {
        }.getType();
        return gson.toJson(userInfo, type);
    }

    private SandboxUserInfo fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, SandboxUserInfo.class);
    }
}


