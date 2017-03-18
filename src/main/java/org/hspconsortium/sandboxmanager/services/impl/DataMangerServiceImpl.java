package org.hspconsortium.sandboxmanager.services.impl;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
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
import org.hspconsortium.sandboxmanager.model.Sandbox;
import org.hspconsortium.sandboxmanager.model.SnapshotAction;
import org.hspconsortium.sandboxmanager.services.DataManagerService;
import org.hspconsortium.sandboxmanager.services.SandboxActivityLogService;
import org.hspconsortium.sandboxmanager.services.SandboxService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DataMangerServiceImpl implements DataManagerService {

    @Value("${hspc.platform.syntheticData.fhirServerUrl}")
    private String fhirServerUrl;

    private static Logger LOGGER = LoggerFactory.getLogger(SandboxServiceImpl.class.getName());

    private final SandboxService sandboxService;
    private final SandboxActivityLogService sandboxActivityLogService;

    @Inject
    public DataMangerServiceImpl(final SandboxService sandboxService,
                                 final SandboxActivityLogService sandboxActivityLogService) {
        this.sandboxService = sandboxService;
        this.sandboxActivityLogService = sandboxActivityLogService;
    }

    @Override
    public String importData(final Sandbox sandbox, final String bearerToken, final String count) throws UnsupportedEncodingException {

        String patientSearchJSON = queryFHIRServer("Patient?_count=" + count);
        JSONObject jsonObj = new JSONObject(patientSearchJSON);
        List<JSONObject> resources = getResourcesFromSearch(jsonObj);
        return getEverythingForResource(resources, sandbox, bearerToken);
    }

    @Override
    public String snapshot(final Sandbox sandbox, final String snapshotId, final SnapshotAction action, final String bearerToken) throws UnsupportedEncodingException {

        if (!snapshotId.matches("^[a-zA-Z0-9]+$")) {
            return "Snapshot ID must only contain alphanumeric characters";
        }
        if (!(snapshotId.length() < 20)) {
            return "Snapshot ID must be less than 20 characters";
        }

        if (snapshotSandboxFhirData(sandbox, snapshotId, action, bearerToken )) {
            List<String> ids = sandbox.getSnapshotIds();

            switch (action) {
                case Take:
                    ids.add(snapshotId);
                    sandbox.setSnapshotIds(ids);
                    sandboxService.save(sandbox);
                    break;
                case Restore:
                    break;
                case Delete:
                    ids.remove(snapshotId);
                    sandbox.setSnapshotIds(ids);
                    sandboxService.save(sandbox);
                    break;
                default:
                    throw new RuntimeException("Unknown sandbox command action: " + action);
            }

            return "SUCCESS";
        } else {
            return "FAILED";
        }
    }

    @Override
    public String reset(final Sandbox sandbox, final String bearerToken) throws UnsupportedEncodingException {

        return resetSandboxFhirData(sandbox, bearerToken ) ? "SUCCESS" : "FAILED";
    }

    private String getEverythingForResource(List<JSONObject> resources, Sandbox sandbox, String bearerToken) throws UnsupportedEncodingException {
        int i = 0;
        for (JSONObject resource : resources) {
            String resourceType = resource.getString("resourceType");
            String resourceId = resource.getString("id");
            String everything = queryFHIRServer(resourceType + "/" + resourceId + "/$everything");
            JSONObject everythingJsonObj = new JSONObject(everything);
            List<JSONObject> everythingResources = getResourcesFromSearch(everythingJsonObj);
            String bundleString = buildTransactionBundle(everythingResources);

            try {
                postFHIRBundle(sandbox, bundleString, bearerToken);
                i++;
            } catch (Exception e){
                // Continue to the next patient
            }
        }
        return "Successful " + i;
    }

    private String buildTransactionBundle(List<JSONObject> resources) {
        Set<String> fixupIDs = new HashSet<>();

        JSONObject transactionBundle = new JSONObject();
        JSONArray resourcesArray = new JSONArray();
        transactionBundle.put("resourceType", "Bundle");
        transactionBundle.put("type", "transaction");

        for (JSONObject resource : resources) {
            JSONObject entry = new JSONObject();

            String resourceType = resource.getString("resourceType");
            String resourceId = resource.getString("id");
            fixupIDs.add(resourceId);
//            resource.put("id","/SYNTHEA-" + resourceId);
            entry.put("resource", resource);

            JSONObject request = new JSONObject();
            request.put("method", "POST");
//            request.put("url", resourceType + "/SYNTHEA-" + resourceId);
            request.put("url", resourceType + "/" + resourceId);
            entry.put("request", request);
            resourcesArray.put(entry);
        }

        transactionBundle.put("entry", resourcesArray);
//        return fixupIDs(fixupIDs, transactionBundle.toString());
        return transactionBundle.toString();
    }

    private String fixupIDs(Set<String> fixupIDs, String bundle) {

//        StringBuilder sb = new StringBuilder();
        for (String id : fixupIDs) {
            bundle = bundle.replace(id, "SYNTHEA-" + id);
        }

        return bundle;
    }

    private List<JSONObject> getResourcesFromSearch(JSONObject jsonObject) {
        List<JSONObject> resources = new ArrayList<>();

        JSONArray entries = jsonObject.getJSONArray("entry");

        for (int i = 0; i < entries.length(); i++) {
            JSONObject entry = entries.getJSONObject(i);

            JSONObject resource = entry.getJSONObject("resource");
            resources.add(resource);
        }

        return resources;
    }

    private String queryFHIRServer(final String query)  {
        String url = this.fhirServerUrl + "/" + query;

        HttpGet getRequest = new HttpGet(url);
        getRequest.setHeader("Accept", "application/json");

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
                String responseString = EntityUtils.toString(rEntity, StandardCharsets.UTF_8);
                String errorMsg = String.format("There was a problem calling the source FHIR server.\n" +
                                "Response Status : %s .\nResponse Detail :%s. \nUrl: :%s",
                        closeableHttpResponse.getStatusLine(),
                        responseString,
                        url);
                LOGGER.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }

            HttpEntity httpEntity = closeableHttpResponse.getEntity();
            return EntityUtils.toString(httpEntity);
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

    private boolean snapshotSandboxFhirData(final Sandbox sandbox, final String snapshotId, final SnapshotAction action, final String bearerToken ) throws UnsupportedEncodingException {
        return postToSandbox(sandbox, "{\"action\": \"" + action.toString() +"\"}", "/sandbox/snapshot/" + snapshotId, bearerToken );
    }

    private boolean resetSandboxFhirData(final Sandbox sandbox, final String bearerToken ) throws UnsupportedEncodingException {
        return postToSandbox(sandbox, "{}", "/sandbox/reset", bearerToken );
    }

    private boolean postFHIRBundle(final Sandbox sandbox, final String jsonString, final String bearerToken ) throws UnsupportedEncodingException {
        return postToSandbox(sandbox, jsonString, "/data", bearerToken );
    }

    private boolean postToSandbox(final Sandbox sandbox, final String jsonString, final String requestStr, final String bearerToken ) throws UnsupportedEncodingException {
        String url = sandboxService.getSandboxApiURL(sandbox) + requestStr;

        HttpPost postRequest = new HttpPost(url);
        postRequest.addHeader("Content-Type", "application/json");
        if (jsonString != null) {
            StringEntity entity = new StringEntity(jsonString);

            postRequest.setEntity(entity);
        }
        postRequest.setHeader("Authorization", "BEARER " + bearerToken);

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

        try (CloseableHttpResponse closeableHttpResponse = httpClient.execute(postRequest)) {
            if (closeableHttpResponse.getStatusLine().getStatusCode() != 200) {
                HttpEntity rEntity = closeableHttpResponse.getEntity();
                String responseString = EntityUtils.toString(rEntity, StandardCharsets.UTF_8);
                String errorMsg = String.format("There was a problem posting to the sandbox.\n" +
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
