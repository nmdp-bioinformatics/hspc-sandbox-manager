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

    @Inject
    public LaunchScenarioController(final LaunchScenarioService launchScenarioService,
                                    final PatientService patientService, final PersonaService personaService,
                                    final AppService appService, final UserService userService) {
        this.launchScenarioService = launchScenarioService;
        this.userService = userService;
        this.patientService = patientService;
        this.personaService = personaService;
        this.appService = appService;
    }

    @RequestMapping(value = "/launchScenario", method = RequestMethod.POST, consumes = "application/json", produces ="application/json")
    public @ResponseBody LaunchScenario createLaunchScenario(@RequestBody @Valid final LaunchScenario launchScenario) {
        User user = userService.findByLdapId(launchScenario.getOwner().getLdapId());
        if (user == null) {
            user = userService.save(launchScenario.getOwner());
        }
        launchScenario.setOwner(user);

        Persona persona = personaService.findByFhirId(launchScenario.getPersona().getFhirId());
        if (persona == null) {
            persona = personaService.save(launchScenario.getPersona());
        }
        launchScenario.setPersona(persona);

        if (launchScenario.getPatient() != null) {
            Patient patient = patientService.findByFhirId(launchScenario.getPatient().getFhirId());
            if (patient == null) {
                patient = patientService.save(launchScenario.getPatient());
            }
            launchScenario.setPatient(patient);
        }

        App app = appService.findByLaunchUri(launchScenario.getApp().getLaunch_uri());
        if (app == null) {
            app = appService.save(launchScenario.getApp());
        }
        launchScenario.setApp(app);

        return launchScenarioService.save(launchScenario);
    }

    @RequestMapping(value = "/launchScenario", method = RequestMethod.PUT, produces ="application/json")
    public @ResponseBody LaunchScenario updateLaunchScenario(@RequestBody @Valid final LaunchScenario launchScenario) {
        return launchScenarioService.save(launchScenario);
    }

    @RequestMapping(value = "/launchScenario", method = RequestMethod.DELETE, produces ="application/json")
    public @ResponseBody void deleteLaunchScenario(@RequestBody @Valid final LaunchScenario launchScenario) {
        launchScenarioService.delete(launchScenario);
    }

    @RequestMapping(value = "/launchScenarios", method = RequestMethod.GET, produces ="application/json",
            params = {"email"})
    public @ResponseBody Iterable<LaunchScenario> getLaunchScenarios(@RequestParam(value = "email") String email) {
        String ownerId = null;
        try {
            ownerId = java.net.URLDecoder.decode(email, "UTF-8");
            return launchScenarioService.findByOwnerId(ownerId);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }
}
