//package org.hspconsortium.sandboxmanager.migrate;
//
//import com.google.gson.Gson;
//import org.apache.http.HttpEntity;
//import org.apache.http.auth.AuthenticationException;
//import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.config.Registry;
//import org.apache.http.config.RegistryBuilder;
//import org.apache.http.conn.HttpClientConnectionManager;
//import org.apache.http.conn.socket.ConnectionSocketFactory;
//import org.apache.http.conn.socket.PlainConnectionSocketFactory;
//import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
//import org.apache.http.conn.ssl.SSLContexts;
//import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClientBuilder;
//import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
//import org.apache.http.util.EntityUtils;
//import org.hspconsortium.platform.messaging.model.user.SandboxUserInfo;
//import org.hspconsortium.sandboxmanager.SandboxManagerApplication;
//import org.hspconsortium.sandboxmanager.model.LaunchScenario;
//import org.hspconsortium.sandboxmanager.model.Persona;
//import org.hspconsortium.sandboxmanager.model.UserPersona;
//import org.hspconsortium.sandboxmanager.services.LaunchScenarioService;
//import org.hspconsortium.sandboxmanager.services.PersonaService;
//import org.hspconsortium.sandboxmanager.services.SandboxService;
//import org.hspconsortium.sandboxmanager.services.UserPersonaService;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.test.SpringApplicationConfiguration;
//import org.springframework.test.annotation.Rollback;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import org.springframework.test.context.web.WebAppConfiguration;
//
//import javax.net.ssl.SSLContext;
//import java.io.IOException;
//import java.io.UnsupportedEncodingException;
//import java.security.KeyManagementException;
//import java.security.KeyStoreException;
//import java.security.NoSuchAlgorithmException;
//
//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(classes = {SandboxManagerApplication.class})
//@WebAppConfiguration
//public class MigrateOldLaunchScenarios {
//
//    @Autowired
//    LaunchScenarioService launchScenarioService;
//
//    @Autowired
//    UserPersonaService userPersonaService;
//
//    @Autowired
//    PersonaService personaService;
//
//    @Autowired
//    SandboxService sandboxService;
//
//    @Value("${hspc.platform.messaging.sandboxUserEndpointURL}")
//    private String sandboxUserEndpointURL;
//
//    @Test
//    @Rollback(false)
//    public void testMigrate() throws IOException, AuthenticationException {
//        Iterable<LaunchScenario> scenarioList = launchScenarioService.findAll();
//        for (LaunchScenario launchScenario : scenarioList) {
////            LaunchScenario newLS = launchScenarioService.getById(launchScenario.getId());
//            Persona persona = launchScenario.getPersona();
//            if (persona != null) {
//                UserPersona userPersona = userPersonaService.findByFhirIdAndSandboxId(persona.getFhirId(), persona.getSandbox().getSandboxId());
////                if (userPersona == null ){
////                    userPersona = createUserPersona(persona);
////                    userPersona = userPersonaService.create(userPersona, "");
////                    userPersona.setSandbox(persona.getSandbox());
////                    userPersona = userPersonaService.save(userPersona);
////                }
//                userPersona.setSandbox(launchScenario.getSandbox());
//                launchScenario.setUserPersona(userPersona);
////                launchScenario.setSandbox(launchScenario.getSandbox());
//                launchScenario.setPersona(null);
//
//                launchScenarioService.save(launchScenario);
////                personaService.delete(persona);
//            }
//        }
//
//        Iterable<Persona> personaList = personaService.findAll();
//        for (Persona persona : personaList) {
//            personaService.delete(persona);
//        }
//
//    }
//
//    private UserPersona createUserPersona(Persona persona) throws UnsupportedEncodingException {
//        UserPersona userPersona = new UserPersona();
//
//        String[] nameSegments = persona.getName().split(" ");
//        String userId = nameSegments[0] + "@" + persona.getSandbox().getSandboxId();
//        SandboxUserInfo sandboxUserInfo = getSandboxUserInfo(userId, "");
////        int num = 0;
////        while (sandboxUserInfo != null) {
////            num++;
////            String checkId = nameSegments[0] + num + "@" + persona.getSandbox().getSandboxId();
////            sandboxUserInfo = getSandboxUserInfo(checkId, "");
////            if (sandboxUserInfo == null) {
////                userId = checkId;
////            }
////        }
//
//        userPersona.setLdapId(userId);
//        userPersona.setLdapName(persona.getName());
//        userPersona.setPassword("password");
//        userPersona.setFhirId(persona.getFhirId());
//        userPersona.setFhirName(persona.getName());
//        userPersona.setResource(persona.getResource());
//        userPersona.setResourceUrl(persona.getResource() + "/" + persona.getFhirId());
////        userPersona.setSandbox(persona.getSandbox());
//
//        return userPersona;
//    }
//
//    private SandboxUserInfo getSandboxUserInfo(final String userId, final String bearerToken) throws UnsupportedEncodingException {
//        String url = this.sandboxUserEndpointURL + "/" + userId;
//
//        HttpGet getRequest = new HttpGet(url);
//        getRequest.setHeader("Accept", "application/json");
//        getRequest.setHeader("Authorization", "BEARER " + bearerToken);
//
//        SSLContext sslContext = null;
//        try {
//            sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).useSSL().build();
//        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
//            throw new RuntimeException(e);
//        }
//        HttpClientBuilder builder = HttpClientBuilder.create();
//        SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
//        builder.setSSLSocketFactory(sslConnectionFactory);
//        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
//                .register("https", sslConnectionFactory)
//                .register("http", new PlainConnectionSocketFactory())
//                .build();
//        HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(registry);
//        builder.setConnectionManager(ccm);
//
//        CloseableHttpClient httpClient = builder.build();
//
//        try (CloseableHttpResponse closeableHttpResponse = httpClient.execute(getRequest)) {
//            if (closeableHttpResponse.getStatusLine().getStatusCode() != 200) {
//                HttpEntity rEntity = closeableHttpResponse.getEntity();
//                String responseString = EntityUtils.toString(rEntity, "UTF-8");
//                String errorMsg = String.format("There was a problem getting the sandbox user.\n" +
//                                "Response Status : %s .\nResponse Detail :%s. \nUrl: :%s",
//                        closeableHttpResponse.getStatusLine(),
//                        responseString,
//                        url);
////                throw new RuntimeException(errorMsg);
//                return null;
//            }
//
//            HttpEntity httpEntity = closeableHttpResponse.getEntity();
//            return fromJson(EntityUtils.toString(httpEntity));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } finally {
//            try {
//                httpClient.close();
//            }catch (IOException e) {
//            }
//        }
//    }
//
//    private SandboxUserInfo fromJson(String json) {
//        Gson gson = new Gson();
//        return gson.fromJson(json, SandboxUserInfo.class);
//    }
//
//
//}
