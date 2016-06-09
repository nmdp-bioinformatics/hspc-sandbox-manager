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

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class RedirectController {

//    @RequestMapping({
//            "/launch-scenarios",
//            "/login",
//            "/after-auth",
//            "/create-sandbox",
//            "/tracks/{id:\\w+}",
//            "/apps",
//            "/patients",
//            "/practitioners",
//            "/users",
//            "/app-gallery",
//            "/start",
//            "/resolve"
//    })

//    @RequestMapping({"/test/**"})
//public void router(HttpServletRequest request, HttpServletResponse response, @PathVariable String sandboxId) {
//    response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
//    response.setHeader("Location", "http://localhost:8080/hspc-sandbox-manager/");
//}

//    @RequestMapping({"/{sandboxId}/{route}/**"})
//    public void router(HttpServletRequest request, HttpServletResponse response, @PathVariable String sandboxId
//            , @PathVariable String route) {
//        response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
////        response.setHeader("Location", "http://localhost:8080/hspc-sandbox-manager/" + sandboxId + "/" + route);
//        response.setHeader("Location", "http://localhost:8080/hspc-sandbox-manager/");
//    }
}
