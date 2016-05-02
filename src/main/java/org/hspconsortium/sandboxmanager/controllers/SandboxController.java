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

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.hspconsortium.sandboxmanager.model.Sandbox;
import org.hspconsortium.sandboxmanager.model.User;
import org.hspconsortium.sandboxmanager.services.OAuthService;
import org.hspconsortium.sandboxmanager.services.SandboxService;
import org.hspconsortium.sandboxmanager.services.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/sandbox")
public class SandboxController {
    @Value("${hspc.platform.api.sandboxManagementEndpointURL}")
    private String sandboxManagementEndpointURL;

    @Value("${hspc.platform.api.oauthUserInfoEndpointURL}")
    private String oauthUserInfoEndpointURL;

    private final SandboxService sandboxService;
    private final UserService userService;
    private final OAuthService oAuthUserService;

    @Inject
    public SandboxController(final SandboxService sandboxService, final UserService userService,
                             final OAuthService oAuthUserService) {
        this.sandboxService = sandboxService;
        this.userService = userService;
        this.oAuthUserService = oAuthUserService;
    }

    @RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces ="application/json")
    public @ResponseBody Sandbox createSandbox(HttpServletRequest request, @RequestBody @Valid final Sandbox sandbox) throws UnsupportedEncodingException{

        checkUserAuthorization(request, sandbox.getCreatedBy().getLdapId());
        User user = userService.findByLdapId(sandbox.getCreatedBy().getLdapId());
        if (user == null) {
            user = userService.save(sandbox.getCreatedBy());
        }
        sandbox.setCreatedBy(user);
        List<User> users = new ArrayList<>();
        users.add(user);
        sandbox.setUsers(users);

        List<Sandbox> sandboxes = user.getSandboxes();
        sandboxes.add(sandbox);
        user.setSandboxes(sandboxes);

        HttpPut putRequest = new HttpPut(this.sandboxManagementEndpointURL + "/" + sandbox.getSandboxId());
        putRequest.addHeader("Content-Type", "application/json");
        StringEntity entity;

        String jsonString = "{\"teamId\": \"" + sandbox.getSandboxId() + "\"}";
        entity = new StringEntity(jsonString);
        putRequest.setEntity(entity);
        putRequest.setHeader("Authorization", "BEARER " + oAuthUserService.getBearerToken(request));

        CloseableHttpClient httpClient = HttpClients.custom().build();

        try (CloseableHttpResponse closeableHttpResponse = httpClient.execute(putRequest)) {
            if (closeableHttpResponse.getStatusLine().getStatusCode() != 200) {
                HttpEntity rEntity = closeableHttpResponse.getEntity();
                String responseString = EntityUtils.toString(rEntity, "UTF-8");
                throw new RuntimeException(String.format("There was a problem creating the sandbox.\n" +
                        "Response Status : %s .\nResponse Detail :%s."
                        , closeableHttpResponse.getStatusLine()
                        , responseString));
            }

           userService.save(user);
            return sandboxService.save(sandbox);
        } catch (IOException io_ex) {
            throw new RuntimeException(io_ex);
        }
    }

    @RequestMapping(method = RequestMethod.GET, produces ="application/json",
            params = {"id"})
    public @ResponseBody Sandbox getSandboxById(HttpServletRequest request, @RequestParam(value = "id") String id) {
        return sandboxService.findBySandboxId(id);
    }

    @RequestMapping(method = RequestMethod.GET, produces ="application/json",
            params = {"userId"})
    public @ResponseBody
    @SuppressWarnings("unchecked")
    List<Sandbox> getSandboxByOwner(HttpServletRequest request, @RequestParam(value = "userId") String userIdEncoded) throws UnsupportedEncodingException {
        String userId = java.net.URLDecoder.decode(userIdEncoded, "UTF-8");
        checkUserAuthorization(request, userId);
        User user = userService.findByLdapId(userId);
        if (user != null) {
            return user.getSandboxes();
        }

        return Collections.EMPTY_LIST;
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseBody
    @ResponseStatus(code = org.springframework.http.HttpStatus.UNAUTHORIZED)
    public void handleAuthorizationException(HttpServletResponse response, Exception e) throws IOException {
        response.getWriter().write(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(org.springframework.http.HttpStatus.BAD_REQUEST)
    public void handleException(HttpServletResponse response, Exception e) throws IOException {
        response.getWriter().write(e.getMessage());
    }

    private void checkUserAuthorization(HttpServletRequest request, String userId) {
        String oauthUserId = oAuthUserService.getOAuthUserId(request);

        if (!userId.equalsIgnoreCase(oauthUserId)) {
            throw new UnauthorizedException(String.format("Response Status : %s.\n" +
                    "Response Detail : User not authorized to perform this action."
                    , HttpStatus.SC_UNAUTHORIZED));
        }
    }
}
