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
import org.hspconsortium.sandboxmanager.services.*;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/REST/launchScenario")
public class LaunchScenarioController extends AbstractController  {

    private final LaunchScenarioService launchScenarioService;
    private final UserService userService;
    private final PatientService patientService;
    private final PersonaService personaService;
    private final AppService appService;
    private final SandboxService sandboxService;

    @Inject
    public LaunchScenarioController(final LaunchScenarioService launchScenarioService,
                                    final PatientService patientService, final PersonaService personaService,
                                    final AppService appService, final UserService userService,
                                    final SandboxService sandboxService, final OAuthService oAuthService) {
        super(oAuthService);
        this.launchScenarioService = launchScenarioService;
        this.userService = userService;
        this.patientService = patientService;
        this.personaService = personaService;
        this.appService = appService;
        this.sandboxService = sandboxService;
    }

    @RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces ="application/json")
    @Transactional
    public @ResponseBody LaunchScenario createLaunchScenario(HttpServletRequest request, @RequestBody final LaunchScenario launchScenario) {

        Sandbox sandbox = sandboxService.findBySandboxId(launchScenario.getSandbox().getSandboxId());
        checkUserAuthorization(request, sandbox.getUserRoles());
        launchScenario.setSandbox(sandbox);

        checkUserAuthorization(request, launchScenario.getCreatedBy().getLdapId());
        User user = userService.findByLdapId(launchScenario.getCreatedBy().getLdapId());
        if (user == null) {
            user = userService.save(launchScenario.getCreatedBy());
        }
        launchScenario.setCreatedBy(user);

        Persona persona = null;
        if (launchScenario.getPersona() != null) {
            persona = personaService.findByFhirIdAndSandboxId(launchScenario.getPersona().getFhirId(), sandbox.getSandboxId());
        }
        if (persona == null && launchScenario.getPersona() != null) {
            persona = launchScenario.getPersona();
            persona.setSandbox(sandbox);
            persona = personaService.save(launchScenario.getPersona());
        }
        launchScenario.setPersona(persona);

        if (launchScenario.getPatient() != null) {
            Patient patient = patientService.findByFhirIdAndSandboxId(launchScenario.getPatient().getFhirId(), sandbox.getSandboxId());
            if (patient == null) {
                patient = launchScenario.getPatient();
                patient.setSandbox(sandbox);
                patient = patientService.save(patient);
            }
            launchScenario.setPatient(patient);
        }

        if (launchScenario.getApp().getAuthClient().getAuthDatabaseId() == null) {
            // Create an anonymous App for a custom launch
            launchScenario.getApp().setSandbox(sandbox);
            App app = appService.save(launchScenario.getApp());
            launchScenario.setApp(app);
        } else {
            App app = appService.findByLaunchUriAndClientIdAndSandboxId(launchScenario.getApp().getLaunchUri(), launchScenario.getApp().getAuthClient().getClientId(), sandbox.getSandboxId());
            launchScenario.setApp(app);
        }

        return launchScenarioService.save(launchScenario);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces ="application/json")
    @Transactional
    public @ResponseBody LaunchScenario updateLaunchScenario(HttpServletRequest request, @PathVariable Integer id, @RequestBody final LaunchScenario launchScenario) {
        if (id.intValue() != launchScenario.getId().intValue()) {
            throw new RuntimeException(String.format("Response Status : %s.\n" +
                            "Response Detail : Launch Scenario Id doesn't match Id in JSON body."
                    , HttpStatus.SC_BAD_REQUEST));
        }
        Sandbox sandbox = sandboxService.findBySandboxId(launchScenario.getSandbox().getSandboxId());
        checkUserAuthorization(request, sandbox.getUserRoles());
        LaunchScenario updateLaunchScenario = launchScenarioService.getById(launchScenario.getId());
        if (updateLaunchScenario != null) {
            updateLaunchScenario.setLastLaunchSeconds(launchScenario.getLastLaunchSeconds());
            updateLaunchScenario.setContextParams(launchScenario.getContextParams());
            updateLaunchScenario.setDescription(launchScenario.getDescription());
            return launchScenarioService.save(updateLaunchScenario);
        }
        return null;
    }

    @RequestMapping(method = RequestMethod.GET, produces ="application/json", params = {"appId"})
    public @ResponseBody Iterable<LaunchScenario> getLaunchScenariosForApp(HttpServletRequest request,
                   @RequestParam(value = "appId") int appId) {

        App app = appService.getById(appId);
        checkUserAuthorization(request, app.getSandbox().getUserRoles());

        return launchScenarioService.findByAppIdAndSandboxId(app.getId(), app.getSandbox().getSandboxId());
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces ="application/json")
    @Transactional
    public @ResponseBody void deleteLaunchScenario(HttpServletRequest request, @PathVariable Integer id) {
        LaunchScenario launchScenario = launchScenarioService.getById(id);
        Sandbox sandbox = sandboxService.findBySandboxId(launchScenario.getSandbox().getSandboxId());
        checkUserAuthorization(request, sandbox.getUserRoles());
        if (launchScenario.getApp().getAuthClient().getAuthDatabaseId() == null) {
            // This is an anonymous App created for a custom launch
            appService.delete(launchScenario.getApp());
        }
        launchScenarioService.delete(launchScenario.getId());
    }

    @RequestMapping(method = RequestMethod.GET, produces = "application/json", params = {"sandboxId"})
    public @ResponseBody Iterable<LaunchScenario> getLaunchScenarios(HttpServletRequest request,
        @RequestParam(value = "sandboxId") String sandboxId) throws UnsupportedEncodingException{

        if (sandboxId != null && isSandboxMember(request, sandboxService.findBySandboxId(sandboxId))) {
            return launchScenarioService.findBySandboxId(sandboxId);
        }
        return Collections.EMPTY_LIST;
    }
}
