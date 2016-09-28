/*
 * #%L
 *
 * %%
 * Copyright (C) 2014 - 2015 Healthcare Services Platform Consortium
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.hspconsortium.sandboxmanager.controllers;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

@RestController
@RequestMapping("/REST/util")
public class UtilController {
    private static Logger LOGGER = LoggerFactory.getLogger(UtilController.class.getName());

    @Value("${hspc.platform.authorization.smart.launchUrl}")
    private String authorizationServerLaunchEndpointURL;

    @Value("${hspc.platform.api.oauth2.clientId}")
    private String apiServerClientId;

    @Value("${hspc.platform.api.oauth2.clientSecret}")
    private String apiServerClientSecret;

//    @RequestMapping(value = "/profileUpdate", method = RequestMethod.POST, consumes = "application/json", produces ="application/json")
//    @Transactional
//    public @ResponseBody String profileUpdate(HttpServletRequest request, @RequestBody final String json) throws UnsupportedEncodingException{
//
//    }


    @RequestMapping(value = "/registerContext", method = RequestMethod.POST, consumes = "application/json", produces ="application/json")
    public void handleLaunchRequest(HttpServletRequest request, HttpServletResponse response, @RequestBody String jsonString) {
        HttpPost postRequest = new HttpPost(this.authorizationServerLaunchEndpointURL);
        postRequest.addHeader("Content-Type", "application/json");
        StringEntity entity = null;
        try {
            entity = new StringEntity(jsonString);
            postRequest.setEntity(entity);

            setAuthorizationHeader(postRequest, apiServerClientId, apiServerClientSecret);
        } catch (UnsupportedEncodingException uee_ex) {
            throw new RuntimeException(uee_ex);
        }

        CloseableHttpClient httpClient = HttpClients.custom().build();

        try (CloseableHttpResponse closeableHttpResponse = httpClient.execute(postRequest)) {
            if (closeableHttpResponse.getStatusLine().getStatusCode() != 200) {
                HttpEntity rEntity = closeableHttpResponse.getEntity();
                String responseString = EntityUtils.toString(rEntity, "UTF-8");
                throw new RuntimeException(String.format("There was a problem with the registration the Launch Context.\n" +
                                "Response Status : %s .\nResponse Detail :%s."
                        , closeableHttpResponse.getStatusLine()
                        , responseString));
            }
            response.setHeader("Content-Type", "application/json;charset=utf-8");
            response.getWriter().write(EntityUtils.toString(closeableHttpResponse.getEntity()));
        } catch (IOException io_ex) {
            throw new RuntimeException(io_ex);
        }
    }

    private static void setAuthorizationHeader(HttpRequest request, String clientId, String clientSecret) {
        String authHeader = String.format("%s:%s", clientId, clientSecret == null ? "" : clientSecret);
        String encoded = Base64.encode(authHeader.getBytes());
        request.addHeader("Authorization", String.format("Basic %s", encoded));
    }

}
