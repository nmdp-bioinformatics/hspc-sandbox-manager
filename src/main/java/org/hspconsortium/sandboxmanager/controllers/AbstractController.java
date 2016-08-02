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
import org.hspconsortium.sandboxmanager.model.Role;
import org.hspconsortium.sandboxmanager.model.Sandbox;
import org.hspconsortium.sandboxmanager.model.UserRole;
import org.hspconsortium.sandboxmanager.services.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

abstract class AbstractController {
    final OAuthService oAuthService;

    @Inject
    public AbstractController(final OAuthService oAuthService) {
        this.oAuthService = oAuthService;
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

    boolean isSandboxMember(Sandbox sandbox, String userId) {
        for(UserRole userRole : sandbox.getUserRoles()) {
            if (userRole.getUser().getLdapId().equalsIgnoreCase(userId)) {
                return true;
            }
        }
        return false;
    }

    boolean isSandboxMember(HttpServletRequest request, Sandbox sandbox) {
        String oauthUserId = oAuthService.getOAuthUserId(request);
        for(UserRole userRole : sandbox.getUserRoles()) {
            if (userRole.getUser().getLdapId().equalsIgnoreCase(oauthUserId)) {
                return true;
            }
        }
        return false;
    }


    void checkUserAuthorization(HttpServletRequest request, String userId) {
        String oauthUserId = oAuthService.getOAuthUserId(request);

        if (!userId.equalsIgnoreCase(oauthUserId)) {
            throw new UnauthorizedException(String.format("Response Status : %s.\n" +
                            "Response Detail : User not authorized to perform this action."
                    , HttpStatus.SC_UNAUTHORIZED));
        }
    }

    void checkUserAuthorization(HttpServletRequest request, List<UserRole> users) {
        String oauthUserId = oAuthService.getOAuthUserId(request);
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
