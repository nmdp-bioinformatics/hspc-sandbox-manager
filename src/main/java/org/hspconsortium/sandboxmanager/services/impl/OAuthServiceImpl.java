package org.hspconsortium.sandboxmanager.services.impl;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.hspconsortium.sandboxmanager.services.OAuthService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Service
public class OAuthServiceImpl implements OAuthService {

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

        CloseableHttpClient httpClient = HttpClients.custom().build();

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
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(entity);
                return (String)jsonObject.get("sub");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException io_ex) {
            throw new RuntimeException(io_ex);
        }
    }
}
