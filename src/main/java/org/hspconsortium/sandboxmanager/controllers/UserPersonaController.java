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

import org.apache.http.cookie.Cookie;
import org.hspconsortium.sandboxmanager.model.LaunchScenario;
import org.hspconsortium.sandboxmanager.model.Sandbox;
import org.hspconsortium.sandboxmanager.model.UserPersona;
import org.hspconsortium.sandboxmanager.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/REST/userPersona")
public class UserPersonaController extends AbstractController {
    private static Logger LOGGER = LoggerFactory.getLogger(UserPersonaController.class.getName());

    private final SandboxService sandboxService;
    private final UserService userService;
    private final UserPersonaService userPersonaService;
    private final OAuthClientService oAuthClientService;

    @Inject
    public UserPersonaController(final SandboxService sandboxService, final UserPersonaService userPersonaService,
                                 final UserService userService,
                                 final OAuthService oAuthService, final OAuthClientService oAuthClientService) {
        super(oAuthService);
        this.sandboxService = sandboxService;
        this.userService = userService;
        this.userPersonaService = userPersonaService;
        this.oAuthClientService = oAuthClientService;
    }

    @RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces ="application/json")
    @Transactional
    public @ResponseBody UserPersona createUserPersona(HttpServletRequest request, @RequestBody final UserPersona userPersona) throws UnsupportedEncodingException{

        Sandbox sandbox = sandboxService.findBySandboxId(userPersona.getSandbox().getSandboxId());
        checkUserAuthorization(request, sandbox.getUserRoles());
        userPersona.setSandbox(sandbox);
        return userPersonaService.create(userPersona, oAuthService.getBearerToken(request));
    }

    @RequestMapping(method = RequestMethod.PUT, consumes = "application/json", produces ="application/json")
    @Transactional
    public @ResponseBody UserPersona updateUserPersona(HttpServletRequest request, @RequestBody final UserPersona userPersona) throws UnsupportedEncodingException{

        Sandbox sandbox = sandboxService.findBySandboxId(userPersona.getSandbox().getSandboxId());
        checkUserAuthorization(request, sandbox.getUserRoles());
        return userPersonaService.update(userPersona, oAuthService.getBearerToken(request));
    }

    @RequestMapping(method = RequestMethod.GET, produces = "application/json", params = {"sandboxId"})
    @SuppressWarnings("unchecked")
    public @ResponseBody Iterable<UserPersona> getSandboxUserPersona(HttpServletRequest request,
                                                                     @RequestParam(value = "sandboxId") String sandboxId) throws UnsupportedEncodingException{

        String oauthUserId = oAuthService.getOAuthUserId(request);
        if (sandboxId != null && sandboxService.isSandboxMember(sandboxService.findBySandboxId(sandboxId),
                userService.findByLdapId(oauthUserId))) {
            return userPersonaService.findBySandboxId(sandboxId);
        }
        return Collections.EMPTY_LIST;
    }

   @RequestMapping(method = RequestMethod.GET, params = {"lookUpId"})
    public @ResponseBody String checkForUserPersonaById(@RequestParam(value = "lookUpId")  String id) {
        UserPersona userPersona = userPersonaService.findByLdapId(id);
        return (userPersona == null) ? null : userPersona.getLdapId();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces ="application/json")
    @Transactional
    public void deleteSandboxUserPersona(HttpServletRequest request, @PathVariable Integer id) {
        UserPersona userPersona = userPersonaService.getById(id);
        checkUserAuthorization(request, userPersona.getSandbox().getUserRoles());

        userPersonaService.delete(userPersona, oAuthService.getBearerToken(request));
    }

}
