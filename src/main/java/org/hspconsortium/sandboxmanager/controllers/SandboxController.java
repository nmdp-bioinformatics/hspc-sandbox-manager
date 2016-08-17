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

import org.hspconsortium.sandboxmanager.model.Sandbox;
import org.hspconsortium.sandboxmanager.model.SandboxInvite;
import org.hspconsortium.sandboxmanager.model.User;
import org.hspconsortium.sandboxmanager.services.OAuthService;
import org.hspconsortium.sandboxmanager.services.SandboxInviteService;
import org.hspconsortium.sandboxmanager.services.SandboxService;
import org.hspconsortium.sandboxmanager.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/REST/sandbox")
public class SandboxController extends AbstractController {
    private static Logger LOGGER = LoggerFactory.getLogger(SandboxController.class.getName());

    private final SandboxService sandboxService;
    private final UserService userService;
    private final SandboxInviteService sandboxInviteService;

    @Inject
    public SandboxController(final SandboxService sandboxService, final UserService userService,
                             final SandboxInviteService sandboxInviteService, final OAuthService oAuthService) {
        super(oAuthService);
        this.sandboxService = sandboxService;
        this.userService = userService;
        this.sandboxInviteService = sandboxInviteService;
    }

    @RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces ="application/json")
    @Transactional
    public @ResponseBody Sandbox createSandbox(HttpServletRequest request, @RequestBody final Sandbox sandbox) throws UnsupportedEncodingException{

        Sandbox existingSandbox = sandboxService.findBySandboxId(sandbox.getSandboxId());
        if (existingSandbox != null) {
            return existingSandbox;
        }

        LOGGER.info("Creating sandbox: " + sandbox.getName());
        checkUserAuthorization(request, sandbox.getCreatedBy().getLdapId());
        User user = userService.findByLdapId(sandbox.getCreatedBy().getLdapId());

        // Create User if needed or set User name
        if (user == null) {
            user = userService.save(sandbox.getCreatedBy());
        } else if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(oAuthService.getOAuthUserName(request));
            user = userService.save(user);
        }

        return sandboxService.create(sandbox, user, oAuthService.getBearerToken(request));
    }

    @RequestMapping(method = RequestMethod.GET, params = {"lookUpId"})
    public @ResponseBody String checkForSandboxById(@RequestParam(value = "lookUpId")  String id) {
        Sandbox sandbox = sandboxService.findBySandboxId(id);
        return (sandbox == null) ? null : sandbox.getSandboxId();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces ="application/json")
    public @ResponseBody Sandbox getSandboxById(HttpServletRequest request, @PathVariable String id) {
        Sandbox sandbox = sandboxService.findBySandboxId(id);
        checkUserAuthorization(request, sandbox.getUserRoles());
        return sandbox;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces ="application/json")
    @Transactional
    public void deleteSandboxById(HttpServletRequest request, @PathVariable String id) {
        Sandbox sandbox = sandboxService.findBySandboxId(id);
        //Only the Sandbox creator can delete the sandbox right now
        checkUserAuthorization(request, sandbox.getCreatedBy().getLdapId());
        //delete sandbox invites
        List<SandboxInvite> invites = sandboxInviteService.findInvitesBySandboxId(sandbox.getSandboxId());
        for (SandboxInvite invite : invites) {
            sandboxInviteService.delete(invite);
        }

        sandboxService.delete(sandbox, oAuthService.getBearerToken(request));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces ="application/json")
    @Transactional
    public void updateSandboxById(HttpServletRequest request, @PathVariable String id, @RequestBody final Sandbox sandbox) {
        Sandbox existingSandbox = sandboxService.findBySandboxId(id);
        checkUserAuthorization(request, existingSandbox.getUserRoles());
        sandboxService.update(sandbox);
    }

    @RequestMapping(method = RequestMethod.GET, produces ="application/json", params = {"userId"})
    public @ResponseBody
    @SuppressWarnings("unchecked")
    List<Sandbox> getSandboxesByMember(HttpServletRequest request, @RequestParam(value = "userId") String userIdEncoded) throws UnsupportedEncodingException {
        String userId = java.net.URLDecoder.decode(userIdEncoded, "UTF-8");
        checkUserAuthorization(request, userId);
        User user = userService.findByLdapId(userId);
        if (user != null) {
            return user.getSandboxes();
        }

        return Collections.EMPTY_LIST;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = "application/json", params = {"removeUserId"})
    @Transactional
    public void removeSandboxUser(HttpServletRequest request, @PathVariable String id, @RequestParam(value = "removeUserId") String userIdEncoded) throws UnsupportedEncodingException {
        Sandbox sandbox = sandboxService.findBySandboxId(id);
        //Only the Sandbox creator can remove a user right now
        checkUserAuthorization(request, sandbox.getCreatedBy().getLdapId());
        String removeUserId = java.net.URLDecoder.decode(userIdEncoded, "UTF-8");

        User user = userService.findByLdapId(removeUserId);
        sandboxService.removeMember(sandbox, user);
    }
}
