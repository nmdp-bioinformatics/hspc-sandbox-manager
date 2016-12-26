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
import org.hspconsortium.sandboxmanager.services.OAuthService;
import org.hspconsortium.sandboxmanager.services.SandboxActivityLogService;
import org.hspconsortium.sandboxmanager.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping({"/REST/user"})
public class UserController extends AbstractController {

    @Value("${hspc.platform.defaultSystemRoles}")
    private String[] defaultSystemRoles;

    private static Logger LOGGER = LoggerFactory.getLogger(UserController.class.getName());

    private final UserService userService;
    private final SandboxActivityLogService sandboxActivityLogService;

    @Inject
    public UserController(final OAuthService oAuthService, final UserService userService,
                          final SandboxActivityLogService sandboxActivityLogService) {
        super(oAuthService);
        this.userService = userService;
        this.sandboxActivityLogService = sandboxActivityLogService;
    }

    @RequestMapping(method = RequestMethod.GET, params = {"ldapId"})
    @Transactional
    public @ResponseBody User getUser(final HttpServletRequest request, @RequestParam(value = "ldapId") String ldapId) {

        checkUserAuthorization(request, ldapId);
        User user = userService.findByLdapId(ldapId);

        // Create User if needed (if it's the first login to the system)
        if (user == null) {
            user = new User();
            user.setCreatedTimestamp(new Timestamp(new Date().getTime()));
            user.setLdapId(ldapId);
            user.setName(oAuthService.getOAuthUserName(request));
            sandboxActivityLogService.systemUserCreated(null, user);

            Set<SystemRole> systemRoles = new HashSet<>();
            for (String roleName : defaultSystemRoles) {
                SystemRole role = SystemRole.valueOf(roleName);
                systemRoles.add(role);
                sandboxActivityLogService.systemUserRoleChange(null, user, role, true);
            }
            user.setSystemRoles(systemRoles);
            userService.save(user);
        }

        return userService.findByLdapId(ldapId);
    }

}