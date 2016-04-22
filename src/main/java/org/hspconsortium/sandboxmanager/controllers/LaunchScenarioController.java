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
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;

@RestController
public class LaunchScenarioController {

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
                                    final SandboxService sandboxService) {
        this.launchScenarioService = launchScenarioService;
        this.userService = userService;
        this.patientService = patientService;
        this.personaService = personaService;
        this.appService = appService;
        this.sandboxService = sandboxService;
    }

    @RequestMapping(value = "/launchScenario", method = RequestMethod.POST, consumes = "application/json", produces ="application/json")
    public @ResponseBody LaunchScenario createLaunchScenario(@RequestBody @Valid final LaunchScenario launchScenario) {
        User user = userService.findByLdapId(launchScenario.getOwner().getLdapId());
        if (user == null) {
            user = userService.save(launchScenario.getOwner());
        }
        launchScenario.setOwner(user);

        // A null sandbox is the HSPC sandbox
        Sandbox sandbox = null;
        if (launchScenario.getSandbox() != null) {
            sandbox = sandboxService.findBySandboxId(launchScenario.getSandbox().getSandboxId());
            launchScenario.setSandbox(sandbox);
        }

        Persona persona = null;
        if (sandbox == null) {
            persona = personaService.findByFhirId(launchScenario.getPersona().getFhirId());
        } else {
            persona = personaService.findByFhirIdAndSandboxId(launchScenario.getPersona().getFhirId(), sandbox.getSandboxId());
        }
        if (persona == null) {
            persona = launchScenario.getPersona();
            persona.setSandbox(sandbox);
            persona = personaService.save(launchScenario.getPersona());
        }
        launchScenario.setPersona(persona);

        if (launchScenario.getPatient() != null) {
            Patient patient = null;
            if (sandbox == null) {
                patient = patientService.findByFhirId(launchScenario.getPatient().getFhirId());
            } else {
                patient = patientService.findByFhirIdAndSandboxId(launchScenario.getPatient().getFhirId(), sandbox.getSandboxId());
            }
            if (patient == null) {
                patient = launchScenario.getPatient();
                patient.setSandbox(sandbox);
                patient = patientService.save(patient);
            }
            launchScenario.setPatient(patient);
        }

        App app = null;
        if (sandbox == null) {
            app = appService.findByClientId(launchScenario.getApp().getClient_id());
        } else {
            app = appService.findByClientIdAndSandboxId(launchScenario.getApp().getClient_id(), sandbox.getSandboxId());
        }
        if (app == null) {
            app = launchScenario.getApp();
            app.setSandbox(sandbox);
            app = appService.save(app);
        }
        launchScenario.setApp(app);

        return launchScenarioService.save(launchScenario);
    }

    @RequestMapping(value = "/launchScenario", method = RequestMethod.PUT, produces ="application/json")
    public @ResponseBody LaunchScenario updateLaunchScenario(@RequestBody @Valid final LaunchScenario launchScenario) {
        LaunchScenario updateLaunchScenario = launchScenarioService.getById(launchScenario.getId());
        updateLaunchScenario.setLastLaunchSeconds(launchScenario.getLastLaunchSeconds());
        return launchScenarioService.save(updateLaunchScenario);
    }

    @RequestMapping(value = "/launchScenario", method = RequestMethod.DELETE, produces ="application/json")
    public @ResponseBody void deleteLaunchScenario(@RequestBody @Valid final LaunchScenario launchScenario) {
        launchScenarioService.delete(launchScenario);
    }

    @RequestMapping(value = "/launchScenarios", method = RequestMethod.GET, produces ="application/json",
            params = {"id"})
    public @ResponseBody Iterable<LaunchScenario> getLaunchScenarios(@RequestParam(value = "id") String id) {
        String ownerId = null;
        try {
            ownerId = java.net.URLDecoder.decode(id, "UTF-8");
            return launchScenarioService.findByOwnerId(ownerId);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/launchScenarios", method = RequestMethod.GET, produces ="application/json",
            params = {"id", "sandboxId"})
    public @ResponseBody Iterable<LaunchScenario> getLaunchScenarios(@RequestParam(value = "id") String id, @RequestParam(value = "sandboxId") String sandboxId) {
        String ownerId = null;

        try {
            ownerId = java.net.URLDecoder.decode(id, "UTF-8");
            // A null sandbox is the HSPC sandbox
            if (sandboxId == null || sandboxId.isEmpty()) {
                return launchScenarioService.findByOwnerId(ownerId);
            } else {
                return launchScenarioService.findByOwnerIdAndSandboxId(ownerId, sandboxId);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
