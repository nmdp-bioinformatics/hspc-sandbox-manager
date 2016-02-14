/*
 * #%L
 * hspc-reference-api
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


import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.net.URISyntaxException;

@RestController
public class UserEndpoint {

    @Value("${hspc.platform.user-support.key}")
    private String userSupportKey;

    @Value("${hspc.platform.user-support.endpoint}")
    private String userSupportEndpoint;

    @RequestMapping(value = "/User", method = RequestMethod.POST)
    public void userServices(HttpServletResponse response) throws URISyntaxException {
        response.addHeader("Location", this.userSupportEndpoint);
        response.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
    }
}
