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
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

abstract class AbstractController {
    final OAuthService oAuthService;

    @Inject
    public AbstractController(final OAuthService oAuthService) {
        this.oAuthService = oAuthService;
    }

    // Check that the userId matches the authorized user in the request
    void checkUserAuthorization(final HttpServletRequest request, String userId) {
        String oauthUserId = oAuthService.getOAuthUserId(request);

        if (!userId.equalsIgnoreCase(oauthUserId)) {
            throw new UnauthorizedException(String.format("Response Status : %s.\n" +
                            "Response Detail : User not authorized to perform this action."
                    , HttpStatus.SC_UNAUTHORIZED));
        }
    }

    // Return userId of the authorized user in the request
    String getSystemUserId(final HttpServletRequest request) {
        return oAuthService.getOAuthUserId(request);
    }

    void checkCreatedByIsCurrentUserAuthorization(final HttpServletRequest request, String createdBySbmUserId) {
        checkUserAuthorization(request, createdBySbmUserId);
    }

    String checkSandboxUserReadAuthorization(final HttpServletRequest request, final Sandbox sandbox) {
        return checkSandboxMember(sandbox, oAuthService.getOAuthUserId(request));
    }

    String checkSandboxUserCreateAuthorization(final HttpServletRequest request, final Sandbox sandbox) {
        return checkSandboxUserNotReadOnlyAuthorization(request, sandbox);
    }

    String checkSandboxUserModifyAuthorization(final HttpServletRequest request, final Sandbox sandbox, final AbstractSandboxItem abstractSandboxItem) {

        //Fast fail for non-sandbox members
        String oauthUserId = checkSandboxUserReadAuthorization(request, sandbox);

        if (abstractSandboxItem.getVisibility() == Visibility.PRIVATE) {
            if (abstractSandboxItem.getCreatedBy().getSbmUserId().equalsIgnoreCase(oauthUserId)) {
                return oauthUserId;
            }
        } else { // Item is PUBLIC
            if (sandbox.getVisibility() == Visibility.PRIVATE) {
                return checkSandboxUserNotReadOnlyAuthorization(request, sandbox);
            } else { // Sandbox is PUBLIC
                if (checkUserHasSandboxRole(request, sandbox, Role.ADMIN)) {
                    return oauthUserId;
                }
            }
        }
        throw new UnauthorizedException(String.format("Response Status : %s.\n" +
                        "Response Detail : User not authorized to perform this action."
                , HttpStatus.SC_UNAUTHORIZED));
    }

    String checkSystemUserCanModifySandboxAuthorization(final HttpServletRequest request, final Sandbox sandbox, final User user) {
        String oauthUserId = oAuthService.getOAuthUserId(request);

        // If the sandbox is PRIVATE, only the creator can modify (currently). If the sandbox is PUBLIC, a system sandbox creator can modify.
        if ((sandbox.getVisibility() == Visibility.PRIVATE && sandbox.getCreatedBy().getSbmUserId().equalsIgnoreCase(oauthUserId)) ||
                (user.getSbmUserId().equalsIgnoreCase(oauthUserId) && checkUserHasSystemRole(user, SystemRole.CREATE_SANDBOX))) {
            return oauthUserId;
        }
        throw new UnauthorizedException(String.format("Response Status : %s.\n" +
                        "Response Detail : User not authorized to perform this action."
                , HttpStatus.SC_UNAUTHORIZED));
    }

    void checkUserSystemRole(final User user, final SystemRole role) {
        if (!checkUserHasSystemRole(user, role)) {

            throw new UnauthorizedException(String.format("Response Status : %s.\n" +
                            "Response Detail : User not authorized to perform this action."
                    , HttpStatus.SC_UNAUTHORIZED));
        }
    }

    void checkUserSandboxRole(final HttpServletRequest request, final Sandbox sandbox, final Role role) {
        if (!checkUserHasSandboxRole(request, sandbox, role)) {

            throw new UnauthorizedException(String.format("Response Status : %s.\n" +
                            "Response Detail : User not authorized to perform this action."
                    , HttpStatus.SC_UNAUTHORIZED));
        }
    }

    Visibility getDefaultVisibility(final User user, final Sandbox sandbox) {

        // For a PRIVATE sandbox, non-readonly user's default visibility is PUBLIC.
        // For a PUBLIC sandbox, only ADMIN's have default visibility of PUBLIC.
        if ((sandbox.getVisibility() == Visibility.PRIVATE && !checkUserHasSandboxRole(user.getSbmUserId(), sandbox, Role.READONLY)) ||
            checkUserHasSandboxRole(user.getSbmUserId(), sandbox, Role.ADMIN)) {
            return Visibility.PUBLIC;
        }
        return Visibility.PRIVATE;
    }

    private String checkSandboxUserNotReadOnlyAuthorization(final HttpServletRequest request, final Sandbox sandbox) {

        String oauthUserId = oAuthService.getOAuthUserId(request);
        if (!checkUserHasSandboxRole(oauthUserId, sandbox, Role.READONLY)) {
            return oauthUserId;
        }

        throw new UnauthorizedException(String.format("Response Status : %s.\n" +
                        "Response Detail : User not authorized to perform this action."
                , HttpStatus.SC_UNAUTHORIZED));
    }

    private String checkSandboxMember(final Sandbox sandbox, final String sbmUserId) {
        for(UserRole userRole : sandbox.getUserRoles()) {
            if (userRole.getUser().getSbmUserId().equalsIgnoreCase(sbmUserId)) {
                return sbmUserId;
            }
        }
        throw new UnauthorizedException(String.format("Response Status : %s.\n" +
                        "Response Detail : User not authorized to perform this action."
                , HttpStatus.SC_UNAUTHORIZED));
    }

    private boolean checkUserHasSystemRole(final User user, final SystemRole role) {
        for(SystemRole systemRole : user.getSystemRoles()) {
            if (systemRole == role) {
                return true;
            }
        }
        return false;
    }

    private boolean checkUserHasSandboxRole(final HttpServletRequest request, final Sandbox sandbox, final Role role) {
        String oauthUserId = oAuthService.getOAuthUserId(request);
        return checkUserHasSandboxRole(oauthUserId, sandbox, role);
    }

    private boolean checkUserHasSandboxRole(final String oauthUserId, final Sandbox sandbox, final Role role) {
        for(UserRole userRole : sandbox.getUserRoles()) {
            if (userRole.getUser().getSbmUserId().equalsIgnoreCase(oauthUserId) && userRole.getRole() == role) {
                return true;
            }
        }
        return false;
    }

}
