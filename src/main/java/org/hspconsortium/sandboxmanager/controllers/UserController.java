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

import org.hspconsortium.sandboxmanager.model.SystemRole;
import org.hspconsortium.sandboxmanager.model.User;
import org.hspconsortium.sandboxmanager.model.UserPersona;
import org.hspconsortium.sandboxmanager.services.OAuthService;
import org.hspconsortium.sandboxmanager.services.SandboxActivityLogService;
import org.hspconsortium.sandboxmanager.services.UserPersonaService;
import org.hspconsortium.sandboxmanager.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;

@RestController
@RequestMapping({"/REST/user"})
public class UserController extends AbstractController {

    @Value("${hspc.platform.defaultSystemRoles}")
    private String[] defaultSystemRoles;

    private static Logger LOGGER = LoggerFactory.getLogger(UserController.class.getName());

    private final UserService userService;
    private final UserPersonaService userPersonaService;
    private final SandboxActivityLogService sandboxActivityLogService;

    private static Semaphore semaphore = new Semaphore(1);

    @Inject
    public UserController(final OAuthService oAuthService, final UserService userService,
                          final SandboxActivityLogService sandboxActivityLogService,
                          final UserPersonaService userPersonaService) {
        super(oAuthService);
        this.userService = userService;
        this.userPersonaService = userPersonaService;
        this.sandboxActivityLogService = sandboxActivityLogService;
    }

    @RequestMapping(method = RequestMethod.GET, params = {"ldapId"})
    @Transactional
    public @ResponseBody
    User getUser(final HttpServletRequest request, @RequestParam(value = "ldapId") String ldapId) {
        checkUserAuthorization(request, ldapId);
        String oauthUsername = oAuthService.getOAuthUserName(request);

        try {
            semaphore.acquire();
            createUserIfNotExists(ldapId, oauthUsername);
        } catch (InterruptedException e) {
            LOGGER.error("User create thread interrupted.", e);
        } catch(Throwable e) {
            LOGGER.error("Exception handling the creation of a user.", e);
        } finally {
            // thread will be released in the event of an exception or successful user return
            semaphore.release();
        }

        return userService.findByLdapId(ldapId);
    }

    @RequestMapping(value = "/acceptterms", method = RequestMethod.POST, params = {"ldapId", "termsId"})
    @Transactional
    public void acceptTermsOfUse(final HttpServletRequest request, @RequestParam(value = "ldapId") String ldapId,
                                 @RequestParam(value = "termsId") String termsId) {

        checkUserAuthorization(request, ldapId);
        User user = userService.findByLdapId(ldapId);
        userService.acceptTermsOfUse(user, termsId);
    }

    private void createUserIfNotExists(String ldapId, String oauthUsername) {
        User user = userService.findByLdapId(ldapId);

        // Create User if needed (if it's the first login to the system)
        if (user == null) {
            UserPersona userPersona = userPersonaService.findByLdapId(ldapId);
            if (userPersona != null) {
                //This is a user persona. A user persona cannot be a sandbox user also
                return;
            }

            user = new User();
            user.setCreatedTimestamp(new Timestamp(new Date().getTime()));
            user.setLdapId(ldapId);
            user.setName(oauthUsername);
            user.setHasAcceptedLatestTermsOfUse(false);
            sandboxActivityLogService.systemUserCreated(null, user);

            Set<SystemRole> systemRoles = new HashSet<>();
            for (String roleName : defaultSystemRoles) {
                SystemRole role = SystemRole.valueOf(roleName);
                systemRoles.add(role);
                sandboxActivityLogService.systemUserRoleChange(user, role, true);
            }
            user.setSystemRoles(systemRoles);
            userService.save(user);
        } else if (user.getName() == null || user.getName().isEmpty() || !user.getName().equalsIgnoreCase(oauthUsername)) {

            Set<SystemRole> curSystemRoles = user.getSystemRoles();
            if (curSystemRoles.size() == 0) {
                Set<SystemRole> systemRoles = new HashSet<>();
                for (String roleName : defaultSystemRoles) {
                    SystemRole role = SystemRole.valueOf(roleName);
                    systemRoles.add(role);
                    sandboxActivityLogService.systemUserRoleChange(user, role, true);
                }
                user.setSystemRoles(systemRoles);
            }
            // Set or Update Name
            user.setName(oauthUsername);
            userService.save(user);
        }
    }
}
