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
import org.hspconsortium.sandboxmanager.services.OAuthService;
import org.hspconsortium.sandboxmanager.services.SandboxInviteService;
import org.hspconsortium.sandboxmanager.services.SandboxService;
import org.hspconsortium.sandboxmanager.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/REST/sandboxinvite")
public class SandboxInviteController {
    private static Logger LOGGER = LoggerFactory.getLogger(SandboxInviteController.class.getName());

    private final SandboxInviteService sandboxInviteService;
    private final UserService userService;
    private final SandboxService sandboxService;
    private final OAuthService oAuthUserService;

    @Inject
    public SandboxInviteController(final SandboxInviteService sandboxInviteService, final UserService userService,
                                   final SandboxService sandboxService, final OAuthService oAuthUserService) {
        this.sandboxInviteService = sandboxInviteService;
        this.userService = userService;
        this.sandboxService = sandboxService;
        this.oAuthUserService = oAuthUserService;
    }

    @RequestMapping(method = RequestMethod.PUT, consumes = "application/json")
    @Transactional
    public @ResponseBody void createOrUpdateSandboxInvite(HttpServletRequest request, @RequestBody final SandboxInvite sandboxInvite) throws UnsupportedEncodingException{

        // Make sure the inviter has rights to this sandbox
        Sandbox sandbox = sandboxService.findBySandboxId(sandboxInvite.getSandbox().getSandboxId());
        checkUserAuthorization(request, sandbox.getUserRoles());
        // Check for an existing invite for this invitee
        List<SandboxInvite> sandboxInvites = sandboxInviteService.findInvitesByInviteeIdAndSandboxId(sandboxInvite.getInvitee().getLdapId(), sandboxInvite.getSandbox().getSandboxId());

        // Resend
        if (sandboxInvites.size() > 0 && (!isSandboxMember(sandbox, sandboxInvite.getInvitee().getLdapId()) || sandboxInvites.get(0).getStatus() != InviteStatus.ACCEPTED )) {
            SandboxInvite existingSandboxInvite = sandboxInvites.get(0);
            existingSandboxInvite.setStatus(sandboxInvite.getStatus());
            existingSandboxInvite.setStatus(InviteStatus.PENDING);

            sandboxInviteService.save(existingSandboxInvite);
            //TODO Send email
        } else if (sandboxInvites.size() == 0) { // Create

            boolean inUserRoles = false;
            for(UserRole userRole : sandbox.getUserRoles()) {
                if (userRole.getUser().getLdapId().equalsIgnoreCase(sandboxInvite.getInvitee().getLdapId())) {
                    inUserRoles = true;
                }
            }

            if (!inUserRoles) {  // Don't invite a user already in the sandbox
                sandboxInvite.setSandbox(sandbox);

                // Make sure the inviter is the authenticated user
                checkUserAuthorization(request, sandboxInvite.getInvitedBy().getLdapId());
                User invitedBy = userService.findByLdapId(sandboxInvite.getInvitedBy().getLdapId());

                sandboxInvite.setInvitedBy(invitedBy);
                sandboxInvite.setInviteTimestamp(new Timestamp(new Date().getTime()));
                sandboxInvite.setStatus(InviteStatus.PENDING);

                // Invitee may not exist, create if needed
                User invitee = userService.findByLdapId(sandboxInvite.getInvitee().getLdapId());
                if (invitee == null) {
                    invitee = userService.save(sandboxInvite.getInvitee());
                }
                sandboxInvite.setInvitee(invitee);
                sandboxInviteService.save(sandboxInvite);

                //TODO Send email
            }
        }

    }

    @RequestMapping(method = RequestMethod.GET, produces ="application/json", params = {"ldapId", "status"})
    public @ResponseBody
    @SuppressWarnings("unchecked")
    List<SandboxInvite> getSandboxInvitesByInvitee(HttpServletRequest request, @RequestParam(value = "ldapId") String ldapIdEncoded,
            @RequestParam(value = "status") InviteStatus status) throws UnsupportedEncodingException {
        String ldapId = java.net.URLDecoder.decode(ldapIdEncoded, "UTF-8");
        checkUserAuthorization(request, ldapId);
        if (status == null) {
            List<SandboxInvite> sandboxInvites = sandboxInviteService.findInvitesByInviteeId(ldapId);
            if (sandboxInvites != null) {
                return sandboxInvites;
            }
        } else {
            List<SandboxInvite> sandboxInvites = sandboxInviteService.findInvitesByInviteeIdAndStatus(ldapId, status);
            if (sandboxInvites != null) {
                return sandboxInvites;
            }
        }

        return Collections.EMPTY_LIST;
    }

    @RequestMapping(method = RequestMethod.GET, produces ="application/json", params = {"sandboxId", "status"})
    public @ResponseBody
    @SuppressWarnings("unchecked")
    List<SandboxInvite> getSandboxInvitesBySandbox(HttpServletRequest request, @RequestParam(value = "sandboxId") String sandboxId,
           @RequestParam(value = "status") InviteStatus status) throws UnsupportedEncodingException {
        Sandbox sandbox = sandboxService.findBySandboxId(sandboxId);
//        checkUserAuthorization(request, sandbox.getCreatedBy().getLdapId());
        checkUserAuthorization(request, sandbox.getUserRoles());

        if (status == null) {
            List<SandboxInvite> sandboxInvites = sandboxInviteService.findInvitesBySandboxId(sandboxId);
            if (sandboxInvites != null) {
                return sandboxInvites;
            }
        } else {
            List<SandboxInvite> sandboxInvites = sandboxInviteService.findInvitesBySandboxIdAndStatus(sandboxId, status);
            if (sandboxInvites != null) {
                return sandboxInvites;
            }
        }

        return Collections.EMPTY_LIST;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, params = {"status"})
    public @ResponseBody
    @SuppressWarnings("unchecked")
    void updateSandboxInvite(HttpServletRequest request, @PathVariable Integer id, @RequestParam(value = "status") InviteStatus status) throws UnsupportedEncodingException {
        SandboxInvite sandboxInvite = sandboxInviteService.getById(id);

        if (sandboxInvite.getStatus() == InviteStatus.PENDING && (status == InviteStatus.ACCEPTED || status == InviteStatus.REJECTED)) {

            String ldapId = sandboxInvite.getInvitee().getLdapId();
            // Only invitee can accept or reject
            checkUserAuthorization(request, ldapId);

            if (status == InviteStatus.REJECTED) {
                sandboxInvite.setStatus(InviteStatus.REJECTED);
                sandboxInviteService.save(sandboxInvite);
                return;
            }

            if (!isSandboxMember(sandboxInvite.getSandbox(), sandboxInvite.getInvitee().getLdapId())) {
                List<UserRole> userRoles = sandboxInvite.getSandbox().getUserRoles();
                userRoles.add(new UserRole(sandboxInvite.getInvitee(), Role.ADMIN));
                sandboxInvite.getSandbox().setUserRoles(userRoles);
                sandboxService.save(sandboxInvite.getSandbox());
            }

            List<Sandbox> sandboxes = sandboxInvite.getInvitee().getSandboxes();
            if (sandboxInvite.getInvitee().getName() == null || sandboxInvite.getInvitee().getName().isEmpty()) {
                sandboxInvite.getInvitee().setName(oAuthUserService.getOAuthUserName(request));
            }
            boolean hasSandbox = false;
            for(Sandbox sandbox : sandboxes) {
                if (sandbox.getSandboxId().equalsIgnoreCase(sandboxInvite.getSandbox().getSandboxId())) {
                    hasSandbox = true;
                }
            }
            if (!hasSandbox) {
                sandboxes.add(sandboxInvite.getSandbox());
                sandboxInvite.getInvitee().setSandboxes(sandboxes);
                userService.save(sandboxInvite.getInvitee());
            }
            sandboxInvite.setStatus(status);
            sandboxInviteService.save(sandboxInvite);
        } else if ((sandboxInvite.getStatus() == InviteStatus.PENDING || sandboxInvite.getStatus() == InviteStatus.REJECTED) && status == InviteStatus.REVOKED ) {

            List<UserRole> userRoles = sandboxInvite.getSandbox().getUserRoles();
            checkUserAuthorization(request, userRoles);
            sandboxInvite.setStatus(status);
            sandboxInviteService.save(sandboxInvite);
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

    private boolean isSandboxMember(Sandbox sandbox, String userId) {
        for(UserRole userRole : sandbox.getUserRoles()) {
            if (userRole.getUser().getLdapId().equalsIgnoreCase(userId)) {
                return true;
            }
        }
        return false;
    }

    private void checkUserAuthorization(HttpServletRequest request, String userId) {
        String oauthUserId = oAuthUserService.getOAuthUserId(request);

        if (!userId.equalsIgnoreCase(oauthUserId)) {
            throw new UnauthorizedException(String.format("Response Status : %s.\n" +
                    "Response Detail : User not authorized to perform this action."
                    , HttpStatus.SC_UNAUTHORIZED));
        }
    }

    private void checkUserAuthorization(HttpServletRequest request, List<UserRole> users) {
        String oauthUserId = oAuthUserService.getOAuthUserId(request);
        boolean userIsAuthorized = false;

        for(UserRole user : users) {
            if (user.getUser().getLdapId().equalsIgnoreCase(oauthUserId) && user.getRole() != Role.READONLY) {
                userIsAuthorized = true;
            }
        }

        if (!userIsAuthorized) {
            throw new UnauthorizedException(String.format("Response Status : %s.\n" +
                            "Response Detail : User not authorized to perform this action."
                    , HttpStatus.SC_UNAUTHORIZED));
        }
    }

}
