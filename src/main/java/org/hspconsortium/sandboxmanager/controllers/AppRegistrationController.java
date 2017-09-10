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
import org.hspconsortium.sandboxmanager.services.AppService;
import org.hspconsortium.sandboxmanager.services.OAuthService;
import org.hspconsortium.sandboxmanager.services.SandboxService;
import org.hspconsortium.sandboxmanager.services.UserService;
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
@RequestMapping({"/REST/app"})
public class AppRegistrationController extends AbstractController {
    private static Logger LOGGER = LoggerFactory.getLogger(AppRegistrationController.class.getName());

    private final AppService appService;
    private final SandboxService sandboxService;
    private final UserService userService;

    @Inject
    public AppRegistrationController(final AppService appService, final OAuthService oAuthService,
                                     final SandboxService sandboxService, final UserService userService) {
        super(oAuthService);
        this.appService = appService;
        this.sandboxService = sandboxService;
        this.userService = userService;
    }

    @RequestMapping(method = RequestMethod.POST)
    @Transactional
    public @ResponseBody App createApp(final HttpServletRequest request, @RequestBody App app) {
        Sandbox sandbox = sandboxService.findBySandboxId(app.getSandbox().getSandboxId());
        String sbmUserId = checkSandboxUserCreateAuthorization(request, sandbox);
        checkCreatedByIsCurrentUserAuthorization(request, app.getCreatedBy().getSbmUserId());

        app.setSandbox(sandbox);
        User user = userService.findBySbmUserId(sbmUserId);
        app.setVisibility(getDefaultVisibility(user, sandbox));
        app.setCreatedBy(user);
        return appService.create(app);
    }

    @RequestMapping(method = RequestMethod.GET, params = {"sandboxId"})
    public @ResponseBody List<App> getApps(final HttpServletRequest request, @RequestParam(value = "sandboxId") String sandboxId) {
        Sandbox sandbox = sandboxService.findBySandboxId(sandboxId);
        String sbmUserId = checkSandboxUserReadAuthorization(request, sandbox);
        return appService.findBySandboxIdAndCreatedByOrVisibility(sandboxId, sbmUserId, Visibility.PUBLIC);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces ="application/json")
    public @ResponseBody App getApp(final HttpServletRequest request, @PathVariable Integer id) {
        try {
            App app = appService.getById(id);
            checkSandboxUserReadAuthorization(request, app.getSandbox());
            return appService.getClientJSON(app);
        } catch (Throwable e) {
            // not being handled by global exception handler?
            LOGGER.error("Error retrieving app", e);
            throw new RuntimeException(e);
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces ="application/json")
    @Transactional
    public @ResponseBody void deleteApp(final HttpServletRequest request, @PathVariable Integer id) {

        App app = appService.getById(id);
        checkSandboxUserModifyAuthorization(request, app.getSandbox(), app);
        appService.delete(app);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces ="application/json")
    @Transactional
    public @ResponseBody App updateApp(final HttpServletRequest request, @PathVariable Integer id, @RequestBody App app) {

        App existingApp = appService.getById(id);
        if (existingApp == null || existingApp.getId().intValue() != id.intValue()) {
            throw new RuntimeException(String.format("Response Status : %s.\n" +
                            "Response Detail : App Id doesn't match Id in JSON body."
                    , HttpStatus.SC_BAD_REQUEST));
        }
        checkSandboxUserModifyAuthorization(request, existingApp.getSandbox(), existingApp);
        return appService.update(app);
    }


    @RequestMapping(value = "/{id}/image", method = RequestMethod.GET, produces ={
            "image/gif", "image/png", "image/jpg", "image/jpeg"
    })
    public @ResponseBody void getFullImage(final HttpServletResponse response, @PathVariable Integer id) {

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
    public @ResponseBody void putFullImage(final HttpServletRequest request, @PathVariable Integer id, @RequestParam("file") MultipartFile file) {

        App app = appService.getById(id);
        checkSandboxUserModifyAuthorization(request, app.getSandbox(), app);
        app.setLogoUri(request.getRequestURL().toString());
        appService.save(app);
        try {
            Image image = new Image();
            image.setBytes(file.getBytes());
            image.setContentType(file.getContentType());
            appService.updateAppImage(app, image);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
