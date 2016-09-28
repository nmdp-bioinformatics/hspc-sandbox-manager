package org.hspconsortium.sandboxmanager.services.impl;

import org.hspconsortium.sandboxmanager.model.*;
import org.hspconsortium.sandboxmanager.repositories.LaunchScenarioRepository;
import org.hspconsortium.sandboxmanager.services.*;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class LaunchScenarioServiceImpl implements LaunchScenarioService {

    private final LaunchScenarioRepository repository;
    private final ContextParamsService contextParamsService;
    private final AppService appService;
    private final PatientService patientService;
    private final UserPersonaService userPersonaService;

    @Inject
    public LaunchScenarioServiceImpl(final LaunchScenarioRepository repository,
                                     final ContextParamsService contextParamsService, final AppService appService,
                                     final PatientService patientService, final UserPersonaService userPersonaService) {
        this.repository = repository;
        this.contextParamsService = contextParamsService;
        this.appService = appService;
        this.patientService = patientService;
        this.userPersonaService = userPersonaService;
    }

    @Override
    @Transactional
    public LaunchScenario save(final LaunchScenario launchScenario) {
        return repository.save(launchScenario);
    }

    @Override
    @Transactional
    public void delete(final int id) {
        repository.delete(id);
    }

    @Override
    @Transactional
    public void delete(final LaunchScenario launchScenario) {

        if (launchScenario.getApp().getAuthClient().getAuthDatabaseId() == null) {
            // This is an anonymous App created for a custom launch
            appService.delete(launchScenario.getApp());
        }

        List<ContextParams> contextParamsList = launchScenario.getContextParams();
        for (ContextParams contextParams : contextParamsList) {
            contextParamsService.delete(contextParams);
        }
        delete(launchScenario.getId());
    }

    @Override
    @Transactional
    public LaunchScenario create(final LaunchScenario launchScenario) {
        Sandbox sandbox = launchScenario.getSandbox();
        UserPersona userPersona = null;
        if (launchScenario.getUserPersona() != null) {
            userPersona = userPersonaService.findByFhirIdAndSandboxId(launchScenario.getUserPersona().getFhirId(), sandbox.getSandboxId());
        }
        if (userPersona == null && launchScenario.getUserPersona() != null) {
            userPersona = launchScenario.getUserPersona();
            userPersona.setSandbox(sandbox);
            userPersona = userPersonaService.save(launchScenario.getUserPersona());
        }
        launchScenario.setUserPersona(userPersona);

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

        return save(launchScenario);
    }

    @Override
    @Transactional
    public LaunchScenario update(final LaunchScenario launchScenario) {
        LaunchScenario updateLaunchScenario = getById(launchScenario.getId());
        if (updateLaunchScenario != null) {
            updateLaunchScenario.setLastLaunchSeconds(launchScenario.getLastLaunchSeconds());
            updateLaunchScenario.setDescription(launchScenario.getDescription());
            updateContextParams(updateLaunchScenario, launchScenario.getContextParams());
            if (launchScenario.getApp().getAuthClient().getAuthDatabaseId() == null) {
                // Create an anonymous App for a custom launch
                App app = appService.getById(launchScenario.getApp().getId());
                app.setLaunchUri(launchScenario.getApp().getLaunchUri());
                app = appService.save(app);
                updateLaunchScenario.setApp(app);
            }
            return save(updateLaunchScenario);
        }
        return null;
    }

    @Override
    public LaunchScenario updateContextParams(final LaunchScenario launchScenario, final List<ContextParams> newContextParams) {

        List<ContextParams> currentContextParams = launchScenario.getContextParams();
        List<ContextParams> removeContextParams = new ArrayList<>();
        for (ContextParams currentParam : currentContextParams) {
            boolean shouldRemove = true;
            for (ContextParams newParam : newContextParams) {
                if (currentParam.getName().equalsIgnoreCase(newParam.getName()) &&
                        currentParam.getValue().equalsIgnoreCase(newParam.getValue())) {
                    newParam.setId(currentParam.getId());
                    shouldRemove = false;
                }
            }
            if (shouldRemove) {
                removeContextParams.add(currentParam);
            }
        }
        for (ContextParams removeContextParam : removeContextParams) {
            contextParamsService.delete(removeContextParam);
        }
        launchScenario.setContextParams(newContextParams);
        return launchScenario;
    }

    @Override
    public Iterable<LaunchScenario> findAll(){
        return repository.findAll();
    }

    @Override
    public LaunchScenario getById(final int id) {
        return  repository.findOne(id);
    }

    @Override
    public List<LaunchScenario> findBySandboxId(final String sandboxId) {
        return  repository.findBySandboxId(sandboxId);
    }

    @Override
    public List<LaunchScenario> findByAppIdAndSandboxId(final int appId, final String sandboxId) {
        return  repository.findByAppIdAndSandboxId(appId, sandboxId);
    }

    @Override
    public List<LaunchScenario> findByUserPersonaIdAndSandboxId(final int userPersonaId, final String sandboxId) {
        return  repository.findByUserPersonaIdAndSandboxId(userPersonaId, sandboxId);
    }
}
