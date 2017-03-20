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
import org.hspconsortium.sandboxmanager.services.AdminService;
import org.hspconsortium.sandboxmanager.services.OAuthService;
import org.hspconsortium.sandboxmanager.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

@RestController
@RequestMapping("/REST/admin")
public class AdminController extends AbstractController {
    private static Logger LOGGER = LoggerFactory.getLogger(AdminController.class.getName());

    private final UserService userService;
    private final AdminService adminService;

    @Inject
    public AdminController(final UserService userService, final OAuthService oAuthService,
                           final AdminService adminService) {
        super(oAuthService);
        this.userService = userService;
        this.adminService = adminService;
    }

    @RequestMapping(method = RequestMethod.GET, produces ="application/json", params = {"interval"})
    public @ResponseBody String getSandboxStatistics(HttpServletRequest request, @RequestParam(value = "interval") String intervalDays) throws UnsupportedEncodingException {
        User user = userService.findByLdapId(getSystemUserId(request));
        checkUserSystemRole(user, SystemRole.ADMIN);
        return adminService.getSandboxStatistics(intervalDays);
    }

}
