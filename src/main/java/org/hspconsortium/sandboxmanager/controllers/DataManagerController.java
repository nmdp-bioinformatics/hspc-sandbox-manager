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

import org.hspconsortium.sandboxmanager.model.*;
import org.hspconsortium.sandboxmanager.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping({"/REST/fhirdata"})
public class DataManagerController extends AbstractController {

    private static Logger LOGGER = LoggerFactory.getLogger(DataManagerController.class.getName());

    private final SandboxService sandboxService;
    private final UserService userService;
    private final DataManagerService dataManagerService;
    private final SandboxActivityLogService sandboxActivityLogService;

    @Inject
    public DataManagerController(final OAuthService oAuthService, final UserService userService,
                                 final SandboxService sandboxService, final DataManagerService dataManagerService,
                                 final SandboxActivityLogService sandboxActivityLogService) {
        super(oAuthService);
        this.userService = userService;
        this.sandboxActivityLogService = sandboxActivityLogService;
        this.sandboxService = sandboxService;
        this.dataManagerService = dataManagerService;
    }

//    @RequestMapping(value = "/import", method = RequestMethod.POST, params = {"sandboxId", "count"})
//    @Transactional
//    public @ResponseBody String importDataSet(final HttpServletRequest request, @RequestParam(value = "sandboxId") String sandboxId,
//                                              @RequestParam(value = "count") String count)  throws UnsupportedEncodingException {
//
//        User user = userService.findByLdapId(getSystemUserId(request));
//        checkUserAuthorization(request, user.getLdapId());
//        Sandbox sandbox = sandboxService.findBySandboxId(sandboxId);
//        checkUserSandboxRole(request, sandbox, Role.MANAGE_DATA);
//
//        return dataManagerService.importData(sandbox, oAuthService.getBearerToken(request), count);
//    }
//
//    @RequestMapping(value = "/reset", method = RequestMethod.POST, params = {"sandboxId"})
//    @Transactional
//    public @ResponseBody String reset(final HttpServletRequest request, @RequestParam(value = "sandboxId") String sandboxId)  throws UnsupportedEncodingException {
//
//        User user = userService.findByLdapId(getSystemUserId(request));
//        checkUserAuthorization(request, user.getLdapId());
//        Sandbox sandbox = sandboxService.findBySandboxId(sandboxId);
//        checkUserSandboxRole(request, sandbox, Role.MANAGE_DATA);
//
//        return dataManagerService.reset(sandbox, oAuthService.getBearerToken(request));
//    }

}
