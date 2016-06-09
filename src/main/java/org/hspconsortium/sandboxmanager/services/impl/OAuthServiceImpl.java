package org.hspconsortium.sandboxmanager.services.impl;


import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
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
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.hspconsortium.sandboxmanager.services.OAuthService;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Service
public class OAuthServiceImpl implements OAuthService {
    private static Logger LOGGER = LoggerFactory.getLogger(OAuthServiceImpl.class.getName());

    @Value("${hspc.platform.api.oauthClientEndpointURL}")
    private String oauthClientEndpointURL;

    @Value("${hspc.platform.api.oauthUserInfoEndpointURL}")
    private String oauthUserInfoEndpointURL;

    public String getBearerToken(HttpServletRequest request) {

        String authToken = request.getHeader("Authorization");
        if (authToken == null) {
            return null;
        }
        return authToken.substring(7);
    }

    public String getOAuthUserId(HttpServletRequest request) {

        String authToken = getBearerToken(request);
        if (authToken == null) {
            return null;
        }

        HttpGet getRequest = new HttpGet(this.oauthUserInfoEndpointURL);
        getRequest.setHeader("Authorization", "BEARER " + authToken);

        SSLContext sslContext;
        try {
            sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).useSSL().build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
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
            if (closeableHttpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                HttpEntity rEntity = closeableHttpResponse.getEntity();
                String responseString = EntityUtils.toString(rEntity, "UTF-8");
                throw new RuntimeException(String.format("Response Status : %s .\nResponse Detail :%s."
                        , closeableHttpResponse.getStatusLine()
                        , responseString));
            }

            HttpEntity httpEntity = closeableHttpResponse.getEntity();
            String entity = IOUtils.toString(httpEntity.getContent());
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(entity);
                return (String)jsonObject.get("sub");
            } catch (JSONException e) {
                LOGGER.error("JSON Error reading entity: " + entity, e);
                throw new RuntimeException(e);
            }
        } catch (IOException io_ex) {
            LOGGER.error("Error on HTTP GET", io_ex);
            throw new RuntimeException(io_ex);
        } finally {
            try {
                httpClient.close();
            }catch (IOException e) {
                LOGGER.error("Error closing HttpClient");
            }
        }
    }

    public String postOAuthClient(String clientJSON) {

        CloseableHttpClient httpClient = getAuthenticatedHttpClient();
        HttpPost postRequest = new HttpPost(oauthClientEndpointURL);
        postRequest.addHeader("Content-Type", "application/json");

        try {
            JSONObject jsonObject = new JSONObject(clientJSON);
            StringEntity entity = new StringEntity(jsonObject.toString());
            postRequest.setEntity(entity);
        } catch (JSONException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        try (CloseableHttpResponse closeableHttpResponse = httpClient.execute(postRequest)) {
            if (closeableHttpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK ) {
                HttpEntity rEntity = closeableHttpResponse.getEntity();
                String responseString = EntityUtils.toString(rEntity, "UTF-8");
                throw new RuntimeException(String.format("There was a problem registering the oauth client.\n" +
                                "Response Status : %s .\nResponse Detail :%s."
                        , closeableHttpResponse.getStatusLine()
                        , responseString));
            }

            HttpEntity httpEntity = closeableHttpResponse.getEntity();
            return IOUtils.toString(httpEntity.getContent());
        } catch (IOException io_ex) {
            throw new RuntimeException(io_ex);
        } finally {
            try {
                httpClient.close();
            }catch (IOException e) {
                LOGGER.error("Error closing HttpClient");
            }
        }
    }

    public String putOAuthClient(Integer id, String clientJSON) {

        CloseableHttpClient httpClient = getAuthenticatedHttpClient();
        HttpPut putRequest = new HttpPut(oauthClientEndpointURL + "/" + id);
        putRequest.addHeader("Content-Type", "application/json");

        try {
            JSONObject jsonObject = new JSONObject(clientJSON);
            StringEntity entity = new StringEntity(jsonObject.toString());
            putRequest.setEntity(entity);
        } catch (JSONException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        try (CloseableHttpResponse closeableHttpResponse = httpClient.execute(putRequest)) {
            if (closeableHttpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK ) {
                HttpEntity rEntity = closeableHttpResponse.getEntity();
                String responseString = EntityUtils.toString(rEntity, "UTF-8");
                throw new RuntimeException(String.format("There was a problem updating the client.\n" +
                                "Response Status : %s .\nResponse Detail :%s."
                        , closeableHttpResponse.getStatusLine()
                        , responseString));
            }
            HttpEntity httpEntity = closeableHttpResponse.getEntity();
            return IOUtils.toString(httpEntity.getContent());
        } catch (IOException io_ex) {
            throw new RuntimeException(io_ex);
        } finally {
            try {
                httpClient.close();
            }catch (IOException e) {
                LOGGER.error("Error closing HttpClient");
            }
        }
    }

    public String getOAuthClient(Integer id) {

        CloseableHttpClient httpClient = getAuthenticatedHttpClient();
        HttpGet getRequest = new HttpGet(oauthClientEndpointURL + "/" + id);

        try (CloseableHttpResponse closeableHttpResponse = httpClient.execute(getRequest)) {
            if (closeableHttpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK ) {
                HttpEntity rEntity = closeableHttpResponse.getEntity();
                String responseString = EntityUtils.toString(rEntity, "UTF-8");
                throw new RuntimeException(String.format("There was a problem registering the client.\n" +
                                "Response Status : %s .\nResponse Detail :%s."
                        , closeableHttpResponse.getStatusLine()
                        , responseString));
            }
            HttpEntity httpEntity = closeableHttpResponse.getEntity();
            return EntityUtils.toString(httpEntity);
        } catch (IOException io_ex) {
            throw new RuntimeException(io_ex);
        } finally {
            try {
                httpClient.close();
            }catch (IOException e) {
                LOGGER.error("Error closing HttpClient");
            }
        }
    }

    public void deleteOAuthClient(Integer id) {

        CloseableHttpClient httpClient = getAuthenticatedHttpClient();
        HttpDelete deleteRequest = new HttpDelete(oauthClientEndpointURL + "/" + id);

        try (CloseableHttpResponse closeableHttpResponse = httpClient.execute(deleteRequest)) {
            if (closeableHttpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK ) {
                HttpEntity rEntity = closeableHttpResponse.getEntity();
                String responseString = EntityUtils.toString(rEntity, "UTF-8");
                throw new RuntimeException(String.format("There was a problem deleting the client.\n" +
                                "Response Status : %s .\nResponse Detail :%s."
                        , closeableHttpResponse.getStatusLine()
                        , responseString));
            }
        } catch (IOException io_ex) {
            throw new RuntimeException(io_ex);
        } finally {
            try {
                httpClient.close();
            }catch (IOException e) {
                LOGGER.error("Error closing HttpClient");
            }
        }
    }


    private CloseableHttpClient getAuthenticatedHttpClient() {

        HttpPost postRequest = new HttpPost("http://localhost:8080/hspc-reference-authorization/j_spring_security_check");
        postRequest.addHeader("Content-Type", "application/x-www-form-urlencoded");
        postRequest.addHeader("Connection", "Keep-Alive");

        try {
            List<NameValuePair> formData = new ArrayList<>();
            formData.add(new BasicNameValuePair("j_username", "admin"));
            formData.add(new BasicNameValuePair("j_password", "FH!R4a11"));
            formData.add(new BasicNameValuePair("submit", "Sign in"));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formData);
            postRequest.setEntity(entity);

        } catch (UnsupportedEncodingException uee_ex) {
            throw new RuntimeException(uee_ex);
        }

        SSLContext sslContext;
        try {
            sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).useSSL().build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
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

        CloseableHttpClient httpClient = builder.setRedirectStrategy(new DefaultRedirectStrategy() {

            public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context)  {
                boolean isRedirect=false;
                try {
                    isRedirect = super.isRedirected(request, response, context);
                } catch (ProtocolException e) {
                    e.printStackTrace();
                }
                if (!isRedirect) {
                    int responseCode = response.getStatusLine().getStatusCode();
                    if (responseCode == HttpServletResponse.SC_MOVED_PERMANENTLY ||
                            responseCode == HttpServletResponse.SC_MOVED_TEMPORARILY) {
                        return true;
                    }
                }
                return false;
            }
        }).build();

        try (CloseableHttpResponse closeableHttpResponse = httpClient.execute(postRequest)) {
            if (closeableHttpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                HttpEntity rEntity = closeableHttpResponse.getEntity();
                String responseString = EntityUtils.toString(rEntity, "UTF-8");
                throw new RuntimeException(String.format("Invalid Credentials\n" +
                                "Response Status : %s .\nResponse Detail :%s."
                        , closeableHttpResponse.getStatusLine()
                        , responseString));
            }
            return httpClient;
        } catch (IOException io_ex) {
            throw new RuntimeException(io_ex);
        }
    }

}
