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

import org.apache.http.HttpStatus;
import org.hspconsortium.sandboxmanager.model.*;
import org.hspconsortium.sandboxmanager.services.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping({"/app"})
public class AppRegistrationController {
    private static Logger LOGGER = LoggerFactory.getLogger(AppRegistrationController.class.getName());

    private final AppService appService;
    private final AuthClientService authClientService;
    private final OAuthService oAuthService;
    private final SandboxService sandboxService;
    private final ImageService imageService;

    @Inject
    public AppRegistrationController(final AppService appService, final OAuthService oAuthService,
                                     final AuthClientService authClientService,
                                     final SandboxService sandboxService,
                                     final ImageService imageService) {
        this.appService = appService;
        this.oAuthService = oAuthService;
        this.authClientService = authClientService;
        this.sandboxService = sandboxService;
        this.imageService = imageService;
    }

    @RequestMapping(method = RequestMethod.POST)
    @Transactional
    public @ResponseBody App createApp(HttpServletRequest request, @RequestBody App app) throws IOException {

        Sandbox sandbox = sandboxService.findBySandboxId(app.getSandbox().getSandboxId());
        checkUserAuthorization(request, sandbox.getCreatedBy().getLdapId());
        app.setSandbox(sandbox);
        app.setLogo(null);

        String entity = oAuthService.postOAuthClient(app.getClientJSON());
        try {
            JSONObject jsonObject = new JSONObject(entity);
            app.getAuthClient().setAuthDatabaseId((Integer)jsonObject.get("id"));
            app.getAuthClient().setClientId((String)jsonObject.get("clientId"));
            app.getAuthClient().setClientName((String)jsonObject.get("clientName"));
            AuthClient authClient = authClientService.save(app.getAuthClient());
            app.setAuthClient(authClient);
            return appService.save(app);
        } catch (JSONException e) {
            LOGGER.error("JSON Error reading entity: " + entity, e);
            throw new RuntimeException(e);
        }
    }

    @RequestMapping(method = RequestMethod.GET, params = {"sandboxId"})
    public @ResponseBody List<App> getApps(HttpServletRequest request, @RequestParam(value = "sandboxId") String sandboxId) {
        Sandbox sandbox = sandboxService.findBySandboxId(sandboxId);
        checkUserAuthorization(request, sandbox.getCreatedBy().getLdapId());
        return appService.findBySandboxId(sandboxId);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces ="application/json")
    public @ResponseBody App getApp(HttpServletRequest request, @PathVariable Integer id) {

        App app = appService.getById(id);
        checkUserAuthorization(request, app.getSandbox().getCreatedBy().getLdapId());

        if (app.getAuthClient().getAuthDatabaseId() != null) {
            String clientJSON = oAuthService.getOAuthClient(app.getAuthClient().getAuthDatabaseId());
            app.setClientJSON(clientJSON);
        }
        return app;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces ="application/json")
    @Transactional
    public @ResponseBody void deleteApp(HttpServletRequest request, @PathVariable Integer id) {

        App app = appService.getById(id);
        checkUserAuthorization(request, app.getSandbox().getCreatedBy().getLdapId());
        Integer authDatabaseId = app.getAuthClient().getAuthDatabaseId();
        appService.delete(app);
        if (authDatabaseId != null) {
            oAuthService.deleteOAuthClient(authDatabaseId);
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces ="application/json")
    @Transactional
    public @ResponseBody App updateApp(HttpServletRequest request, @PathVariable Integer id, @RequestBody App app) {

        App existingApp = appService.getById(id);
        checkUserAuthorization(request, app.getSandbox().getCreatedBy().getLdapId());
        if (existingApp == null || existingApp.getId().intValue() != id.intValue()) {
            throw new RuntimeException(String.format("Response Status : %s.\n" +
                            "Response Detail : App Id doesn't match Id in JSON body."
                    , HttpStatus.SC_BAD_REQUEST));
        }

        String entity = oAuthService.putOAuthClient(existingApp.getAuthClient().getAuthDatabaseId(), app.getClientJSON());

        try {
            JSONObject jsonObject = new JSONObject(entity);
            existingApp.getAuthClient().setClientName((String)jsonObject.get("clientName"));
            existingApp.getAuthClient().setLogoUri(app.getLogoUri());
            authClientService.save(existingApp.getAuthClient());
        } catch (JSONException e) {
            LOGGER.error("JSON Error reading entity: " + entity, e);
            throw new RuntimeException(e);
        }
        existingApp.setLaunchUri(app.getLaunchUri());
        existingApp.setLogoUri(app.getLogoUri());
        return appService.save(existingApp);
    }


    @RequestMapping(value = "/{id}/image", method = RequestMethod.GET, produces ={
            "image/gif", "image/png", "image/jpg", "image/jpeg"
    })
    public @ResponseBody void getFullImage(HttpServletResponse response, @PathVariable Integer id) {

        App app = appService.getById(id);
        try {
            response.setHeader("Content-Type", app.getLogo().getContentType());
            response.getOutputStream().write(app.getLogo().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @RequestMapping(value = "/{id}/image", method = RequestMethod.POST, consumes = {"multipart/form-data"} )
    @Transactional
    public @ResponseBody void putFullImage(HttpServletRequest request, @PathVariable Integer id, @RequestParam("file") MultipartFile file) {

        App app = appService.getById(id);
        checkUserAuthorization(request, app.getSandbox().getCreatedBy().getLdapId());

        String clientJSON = oAuthService.getOAuthClient(app.getAuthClient().getAuthDatabaseId());
        try {
            JSONObject jsonObject = new JSONObject(clientJSON);
            jsonObject.put("logoUri", request.getRequestURL().toString());
            oAuthService.putOAuthClient(app.getAuthClient().getAuthDatabaseId(), jsonObject.toString());
        } catch (JSONException e) {
            LOGGER.error("JSON Error reading entity: " + clientJSON, e);
            throw new RuntimeException(e);
        }

        try {
            Image image = new Image();
            image.setBytes(file.getBytes());
            image.setContentType(file.getContentType());
            app.setLogo(image);
            app.setLogoUri(request.getRequestURL().toString());
            app.getAuthClient().setLogoUri(request.getRequestURL().toString());
            appService.save(app);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseBody
    @ResponseStatus(code = org.springframework.http.HttpStatus.UNAUTHORIZED)
    public void handleAuthorizationException(HttpServletResponse response, Exception e) throws IOException {
        response.getWriter().write(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
    public void handleException(HttpServletResponse response, Exception e) throws IOException {
        response.getWriter().write(e.getMessage());
    }

    private void checkUserAuthorization(HttpServletRequest request, String userId) {
        String oauthUserId = oAuthService.getOAuthUserId(request);

        if (!userId.equalsIgnoreCase(oauthUserId)) {
            throw new UnauthorizedException(String.format("Response Status : %s.\n" +
                            "Response Detail : User not authorized to perform this action."
                    , HttpStatus.SC_UNAUTHORIZED));
        }
    }

}
