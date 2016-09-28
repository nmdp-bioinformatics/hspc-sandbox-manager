package org.hspconsortium.sandboxmanager.services.impl;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
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
import org.hspconsortium.sandboxmanager.model.*;
import org.hspconsortium.sandboxmanager.repositories.SandboxRepository;
import org.hspconsortium.sandboxmanager.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.net.ssl.SSLContext;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Service
public class SandboxServiceImpl implements SandboxService {

    private static Logger LOGGER = LoggerFactory.getLogger(SandboxServiceImpl.class.getName());
    private final SandboxRepository repository;

    @Value("${hspc.platform.api.sandboxManagementEndpointURL}")
    private String sandboxManagementEndpointURL;

    @Value("${hspc.platform.api.oauthUserInfoEndpointURL}")
    private String oauthUserInfoEndpointURL;


    private final UserService userService;
    private final UserRoleService userRoleService;
    private final UserPersonaService userPersonaService;
    private final AppService appService;
    private final LaunchScenarioService launchScenarioService;
    private final PatientService patientService;
    private final PersonaService personaService;

    @Inject
    public SandboxServiceImpl(final SandboxRepository repository, final UserService userService,
                              final UserRoleService userRoleService, final AppService appService,
                              final UserPersonaService userPersonaService,
                              final LaunchScenarioService launchScenarioService,
                              final PatientService patientService, final PersonaService personaService) {
        this.repository = repository;
        this.userService = userService;
        this.userRoleService = userRoleService;
        this.userPersonaService = userPersonaService;
        this.appService = appService;
        this.launchScenarioService = launchScenarioService;
        this.patientService = patientService;
        this.personaService = personaService;
    }

    @Override
    public void delete(final int id) {
        repository.delete(id);
    }

    @Override
    @Transactional
    public void delete(final Sandbox sandbox, final String bearerToken) {

        if (callDeleteSandboxAPI(sandbox, bearerToken) ) {
            callDeleteSandboxAPI(sandbox, bearerToken);

            //delete launch scenarios, context params
            List<LaunchScenario> launchScenarios = launchScenarioService.findBySandboxId(sandbox.getSandboxId());
            for (LaunchScenario launchScenario : launchScenarios) {
                launchScenarioService.delete(launchScenario);
            }

            //delete all registered app, authClients, images
            List<App> apps = appService.findBySandboxId(sandbox.getSandboxId());
            for (App app : apps) {
                appService.delete(app);
            }

            //delete patient/personas for sandbox
            List<Patient> patients = patientService.findBySandboxId(sandbox.getSandboxId());
            for (Patient patient : patients) {
                patientService.delete(patient);
            }
            List<Persona> personas = personaService.findBySandboxId(sandbox.getSandboxId());
            for (Persona persona : personas) {
                personaService.delete(persona);
            }

            List<UserPersona> userPersonas = userPersonaService.findBySandboxId(sandbox.getSandboxId());
            for (UserPersona userPersona : userPersonas) {
                userPersonaService.delete(userPersona, bearerToken);
            }

            //remove user memberships
            removeAllMembers(sandbox);

            delete(sandbox.getId());
        }
    }

    @Override
    @Transactional
    public Sandbox create(final Sandbox sandbox, final User user, final String bearerToken) throws UnsupportedEncodingException {

        UserPersona userPersona = userPersonaService.findByLdapId(user.getLdapId());

        if (userPersona == null && callCreateSandboxAPI(sandbox, bearerToken)) {
            sandbox.setCreatedBy(user);
            Sandbox savedSandbox = save(sandbox);
            addMember(savedSandbox, user);
            return savedSandbox;
        }
        return null;
    }

    @Override
    @Transactional
    public Sandbox update(final Sandbox sandbox)  {
        Sandbox existingSandbox = findBySandboxId(sandbox.getSandboxId());
        existingSandbox.setName(sandbox.getName());
        existingSandbox.setDescription(sandbox.getDescription());
        return save(existingSandbox);
    }

    @Override
    @Transactional
    public void removeMember(final Sandbox sandbox, final User user) {
        if (user != null) {
            userService.removeSandbox(sandbox, user);

            List<UserRole> userRoles = sandbox.getUserRoles();
            Iterator<UserRole> iterator = userRoles.iterator();
            UserRole userRole = null;
            while (iterator.hasNext()) {
                userRole = iterator.next();
                if (userRole.getUser().getId().equals(user.getId())) {
                    iterator.remove();
                    break;
                }
            }
            if (userRole != null) {
                sandbox.setUserRoles(userRoles);
                save(sandbox);
                userRoleService.delete(userRole);
            }
        }
    }

    @Override
    @Transactional
    public void removeAllMembers(final Sandbox sandbox) {

        List<UserRole> userRoles = sandbox.getUserRoles();
        sandbox.setUserRoles(Collections.<UserRole>emptyList());
        save(sandbox);

        for(UserRole userRole : userRoles) {
            userService.removeSandbox(sandbox, userRole.getUser());
            userRoleService.delete(userRole);
        }
    }

    @Override
    @Transactional
    public void addMember(final Sandbox sandbox, final User user) {
        if (!isSandboxMember(sandbox, user)) {
            List<UserRole> userRoles = sandbox.getUserRoles();
            userRoles.add(new UserRole(user, Role.ADMIN));
            sandbox.setUserRoles(userRoles);
            userService.addSandbox(sandbox, user);
            save(sandbox);
        }
    }

    @Override
    public boolean isSandboxMember(final Sandbox sandbox, final User user) {
        for(UserRole userRole : sandbox.getUserRoles()) {
            if (userRole.getUser().getLdapId().equalsIgnoreCase(user.getLdapId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional
    public Sandbox save(final Sandbox sandbox) {
        return repository.save(sandbox);
    }

    @Override
    public Sandbox findBySandboxId(final String sandboxId) {
        return repository.findBySandboxId(sandboxId);
    }

    private boolean callCreateSandboxAPI(final Sandbox sandbox, final String bearerToken ) throws UnsupportedEncodingException{
        String url = this.sandboxManagementEndpointURL + "/" + sandbox.getSandboxId();

        HttpPut putRequest = new HttpPut(url);
        putRequest.addHeader("Content-Type", "application/json");
        StringEntity entity;

        String jsonString = "{\"teamId\": \"" + sandbox.getSandboxId() + "\"}";
        entity = new StringEntity(jsonString);
        putRequest.setEntity(entity);
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
                String errorMsg = String.format("There was a problem creating the sandbox.\n" +
                                "Response Status : %s .\nResponse Detail :%s. \nUrl: :%s",
                        closeableHttpResponse.getStatusLine(),
                        responseString,
                        url);
                LOGGER.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }

            return true;
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

    private boolean callDeleteSandboxAPI(final Sandbox sandbox, final String bearerToken ) {
        String url = this.sandboxManagementEndpointURL + "/" + sandbox.getSandboxId();

        HttpDelete deleteRequest = new HttpDelete(url);
        deleteRequest.addHeader("Authorization", "BEARER " + bearerToken);

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
                String errorMsg = String.format("There was a problem deleting the sandbox.\n" +
                                "Response Status : %s .\nResponse Detail :%s. \nUrl: :%s",
                        closeableHttpResponse.getStatusLine(),
                        responseString,
                        url);
                LOGGER.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }

            return true;
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
}


