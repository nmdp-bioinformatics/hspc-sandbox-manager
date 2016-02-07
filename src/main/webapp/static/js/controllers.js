'use strict';

angular.module('sandManApp.controllers', []).controller('navController',[
    "$rootScope", "$scope", "appsSettings", "fhirApiServices", "userServices", "patientDetails", "oauth2", "launchScenarios", "$location", "$state",
    function($rootScope, $scope, appsSettings, fhirApiServices, userServices, patientDetails, oauth2, launchScenarios, $location, $state) {

        $scope.showing = {
            signout: false,
            signin: true,
            loading: false,
            searchloading: false,
            navBar: false,
            largeSidebar: true
        };
        $scope.messages = [];

        $rootScope.$on('message-notify', function(event, messages){
            $scope.messages = messages;
            $rootScope.$digest();
        });

        $rootScope.$on("$stateChangeStart", function(event, toState, toParams, fromState, fromParams){
            if (toState.authenticate && typeof window.fhirClient === "undefined"){
                // User isn’t authenticated
                $scope.signin();
                event.preventDefault();
            }
            if (toState.scenarioBuilderStep && launchScenarios.getBuilder().persona === "") {
                $state.go('launch-scenarios', {});
                event.preventDefault();
            }
        });

        $scope.signin = function() {
            appsSettings.getSettings().then(function(settings){
                oauth2.authorize(settings);
            });
        };

        $rootScope.$on('signed-in', function(){
            $scope.oauthUser = userServices.getOAuthUser();
            userServices.getFhirProfileUser().then(function(persona){
                $scope.persona = persona;
                $rootScope.$digest();
            });
            $scope.showing.signin = false;
            $scope.showing.signout = true;
            $scope.showing.navBar = true;
            $state.go('launch-scenarios', {});
        });

        $rootScope.$on('hide-nav', function(){
            $scope.showing.navBar = false;
        });

        $scope.signout = function() {
            delete $rootScope.user;
            fhirApiServices.clearClient();
            oauth2.logout().then(function(){
                oauth2.login();
            });
        };

        $scope.manageUserAccount = function() {
            window.open("https://sandbox.hspconsortium.org/pwm/private/");
        };

    }]).controller("StartController",
        function(fhirApiServices){
            fhirApiServices.initClient();
    }).controller("LoginController",
    function($rootScope, $scope, oauth2, appsSettings, fhirApiServices){

        if (sessionStorage.tokenResponse && !fhirApiServices.clientInitialized()) {
            // access token is available, so sign-in now
            appsSettings.getSettings().then(function(settings){
                oauth2.authorize(settings);
            });
        } else if (fhirApiServices.clientInitialized()) {
            $rootScope.$emit('signed-in');
        } else {
            oauth2.login();
        }

    }).controller("SideBarController",
    function($rootScope, $scope){

        var sideBarStates = ['launch-scenarios','users', 'patients', 'practitioners'];

        $rootScope.$on('$stateChangeStart', function(event, toState, toParams, fromState, fromParams){
            if ( sideBarStates.indexOf(toState.name) > -1) {
                $scope.selected = toState.name;
            }
        });

        $scope.selected = "";
        $scope.select = function(selection){
            $scope.selected = selection;
        };

        $scope.toggleSize = function() {
            $scope.showing.largeSidebar = !$scope.showing.largeSidebar;
        };

    }).controller("PatientViewController",
    function($scope){
        $scope.showing = {patientDetail: false,
            noPatientContext: true,
            createPatient: true,
            patientDataManager: false,
            selectForScenario: false,
            searchloading: true
        };

        $scope.page = {
            title: ""
        };

        $scope.selected = {
            selectedPatient: {},
            patientSelected: false
        }

    }).controller("PatientDetailController",
    function($scope, $rootScope, $state, launchScenarios, patientDetails, launchApp){

        $scope.patientHelper = patientDetails;

        if ($state.current.name === 'patients') {
            $scope.showing.patientDataManager = true;
        }

        if ($state.current.name === 'patient-view') {
            $scope.showing.selectForScenario = true;
        }

        $scope.setPatient = function(p){
            if (launchScenarios.getBuilder().persona === '') {
                launchScenarios.setPersona(
                    {fhirId: p.id,
                        resource: p.resourceType,
                        fullUrl: p.fullUrl,
                        name: patientDetails.name(p)});
                launchScenarios.setPatient(
                    {fhirId: p.id,
                        resource: p.resourceType,
                        name: patientDetails.name(p)});
                $state.go('apps', {source: 'patient', action: 'choose'});
//                $state.go($state.current, {source: 'patient'}, {reload: true});
            } else {
                launchScenarios.setPatient(
                    {fhirId: p.id,
                        resource: p.resourceType,
                        name: patientDetails.name(p)});
                $state.go('apps', {source: 'practitioner-patient', action: 'choose'});
            }
        };

        $scope.launchPatientDataManager = function(patient){
            launchApp.launchPatientDataManager(patient);
        };

    }).controller("PatientSearchController",
    function($scope, $rootScope, $state, $stateParams, fhirApiServices, patientDetails, launchScenarios) {

        var source = $stateParams.source;

        if (source === 'patient') {
            $scope.page.title = "Select the Patient Context";
            $scope.showing.noPatientContext =  true;
            $scope.showing.createPatient =  false;
        } else if (source === 'persona') {
            $scope.page.title = "Select the Patient Persona";
            $scope.showing.noPatientContext =  false;
            $scope.showing.createPatient =  false;
        } else if ($state.current.name === 'resolve') {
            $scope.showing.noPatientContext =  false;
            $scope.showing.createPatient =  false;
            $rootScope.$emit('hide-nav');
        } else { // Patient View
            $scope.showing.noPatientContext =  false;
            $scope.showing.createPatient =  true;
        }

        $scope.onSelected = $scope.onSelected || function(p){
            $scope.selected.selectedPatient = p;
            $scope.selected.patientSelected = true;
            $scope.showing.patientDetail = true;
        };

        $scope.skipPatient = function(){
            launchScenarios.setPatient(
                {fhirId: 0,
                    resource: "None",
                    name: "None"});
            $state.go('apps', {source: 'practitioner', action: 'choose'});
        };

        $scope.mayLoadMore = true;
        $scope.patients = [];
        $scope.genderglyph = {"female" : "&#9792;", "male": "&#9794;"};
        $scope.searchterm = "";
        $scope.patientHelper = patientDetails;
        var lastQueryResult;

        $rootScope.$on('set-loading', function(){
            $scope.showing.searchloading = true;
        });

        /** Checks if the patient list div is (almost) fully visible on screen and if so loads more patients. */
        $scope.loadMoreIfNeeded = function() {
            if (!$scope.mayLoadMore) {
                return;
            }

            // Normalize scrollTop to account for variations in browser behavior (NJS 2015-03-04)
            var scrollTop = (document.documentElement.scrollTop > document.body.scrollTop) ? document.documentElement.scrollTop : document.body.scrollTop;

            var list = $('#patient-results');
            if (list.offset().top + list.height() - 200 - scrollTop <= window.innerHeight) {
                $scope.mayLoadMore = false;
                $scope.loadMoreIfHasMore();
            }
        };

        $scope.loadMoreIfHasMore = function() {
            if ($scope.hasNext()) {
                $scope.loadMore();
            }
        };

        $scope.loadMore = function() {
            $scope.showing.searchloading = true;
            fhirApiServices.getNextOrPrevPage("nextPage", lastQueryResult).then(function(p, queryResult){
                lastQueryResult = queryResult;
                p.forEach(function(v) { $scope.patients.push(v) }, p);
                $scope.showing.searchloading = false;
                $scope.mayLoadMore = true;
                $scope.loadMoreIfNeeded();
                $rootScope.$digest();
            });
        };

        $scope.select = function(i){
            $scope.onSelected($scope.patients[i]);
        };

        $scope.hasNext = function(){
            return fhirApiServices.hasNext(lastQueryResult);
        };

        $scope.$watch("searchterm", function(){
            var tokens = [];
            ($scope.searchterm || "").split(/\s/).forEach(function(t){
                tokens.push(t.toLowerCase());
            });
            $scope.tokens = tokens;
            if ($scope.getMore !== undefined) {
                $scope.getMore();
            }
        });

        var loadCount = 0;
        var search = _.debounce(function(thisLoad){
            fhirApiServices.queryResourceInstances("Patient", undefined, $scope.tokens, [['given','asc'],['family','asc']])
                .then(function(p, queryResult){
                    lastQueryResult = queryResult;
                    if (thisLoad < loadCount) {   // not sure why this is needed (pp)
                        return;
                    }
                    $scope.patients = p;
                    $scope.showing.searchloading = false;
                    $scope.mayLoadMore = true;
                    $scope.loadMoreIfNeeded();
                    $rootScope.$digest();
                });
        }, 300);

        $scope.getMore = function(){
            $scope.showing.searchloading = true;
            search(++loadCount);
        };

    }).controller("PractitionerViewController",
    function($scope){
        $scope.showing = {
            practitionerDetail: false,
            selectForScenario: false,
            createPractitioner: false,
            searchloading: true
        };

        $scope.selected = {
            selectedPractitioner: {},
            practitionerSelected: false
        }

    }).controller("PractitionerDetailController",
    function($scope, $rootScope, $state, launchScenarios, patientDetails){

        $scope.practitionerHelper = patientDetails;

        if ($state.current.name === 'practitioner-view') {
            $scope.showing.selectForScenario = true;
        }

        $scope.practitionerSpecialty = function() {
            try {
                return $scope.selected.selectedPractitioner.practitionerRole[0].specialty[0].coding[0].display;
            }
            catch(err) {
                return false;
            }
        };

        $scope.practitionerRole = function() {
            try {
                return $scope.selected.selectedPractitioner.practitionerRole[0].role.coding[0].display;
            }
            catch(err) {
                return false;
            }
        };

        $scope.setPractitioner = function(p){
            launchScenarios.setPersona(
                {fhirId: p.id,
                    resource: p.resourceType,
                    fullUrl: p.fullUrl,
                    name: patientDetails.name(p)});
            $state.go('patient-view', {source: 'patient'});
        };
    }).controller("PractitionerSearchController",
    function($scope, $rootScope, $state, $stateParams, fhirApiServices, patientDetails) {

        $scope.onSelected = function(p){
            $scope.selected.selectedPractitioner = p;
            $scope.selected.practitionerSelected = true;
            $scope.showing.practitionerDetail = true;
        };

        if ($state.current.name === 'practitioners') {
            $scope.showing.createPractitioner =  true;
        }


        $scope.mayLoadMore = true;
        $scope.practitioners = [];
        $scope.searchterm = "";
        $scope.practitionerHelper = patientDetails;
        var lastQueryResult;

        $rootScope.$on('set-loading', function(){
            $scope.showing.searchloading = true;
        });

        /** Checks if the patient list div is (almost) fully visible on screen and if so loads more patients. */
        $scope.loadMoreIfNeeded = function() {
            if (!$scope.mayLoadMore) {
                return;
            }

            // Normalize scrollTop to account for variations in browser behavior (NJS 2015-03-04)
            var scrollTop = (document.documentElement.scrollTop > document.body.scrollTop) ? document.documentElement.scrollTop : document.body.scrollTop;

            var list = $('#practitioner-results');
            if (list.offset().top + list.height() - 200 - scrollTop <= window.innerHeight) {
                $scope.mayLoadMore = false;
                $scope.loadMoreIfHasMore();
            }
        };

        $scope.loadMoreIfHasMore = function() {
            if ($scope.hasNext()) {
                $scope.loadMore();
            }
        };

        $scope.loadMore = function() {
            $scope.showing.searchloading = true;
            fhirApiServices.getNextOrPrevPage("nextPage", lastQueryResult).then(function(p, queryResult){
                lastQueryResult = queryResult;
                p.forEach(function(v) { $scope.practitioners.push(v) }, p);
                $scope.showing.searchloading = false;
                $scope.mayLoadMore = true;
                $scope.loadMoreIfNeeded();
                $rootScope.$digest();
            });
        };

        $scope.select = function(i){
            $scope.onSelected($scope.practitioners[i]);
        };

        $scope.hasNext = function(){
            return fhirApiServices.hasNext(lastQueryResult);
        };

        $scope.$watch("searchterm", function(){
            var tokens = [];
            ($scope.searchterm || "").split(/\s/).forEach(function(t){
                tokens.push(t.toLowerCase());
            });
            $scope.tokens = tokens;
            if ($scope.getMore !== undefined) {
                $scope.getMore();
            }
        });

        var loadCount = 0;
        var search = _.debounce(function(thisLoad){
            fhirApiServices.queryResourceInstances("Practitioner", undefined, $scope.tokens, [['given','asc'],['family','asc']])
                .then(function(p, queryResult){
                    lastQueryResult = queryResult;
                    if (thisLoad < loadCount) {   // not sure why this is needed (pp)
                        return;
                    }
                    $scope.practitioners = p;
                    $scope.showing.searchloading = false;
                    $scope.mayLoadMore = true;
                    $scope.loadMoreIfNeeded();
                    $rootScope.$digest();
                });
        }, 300);

        $scope.getMore = function(){
            $scope.showing.searchloading = true;
            search(++loadCount);
        };

    }).controller("LaunchScenariosController",
    function($rootScope, $scope, $state, launchScenarios, launchApp, userServices, descriptionBuilder){
        $scope.showing = {detail: false, addingContext: false};
        $scope.selectedScenario = {};
        launchScenarios.getLaunchScenarios();
        launchScenarios.clearBuilder();
        launchScenarios.getBuilder().owner = userServices.oauthUser();

        $scope.launch = function(scenario){
            userServices.updateProfile(scenario.persona);
            scenario.lastLaunchSeconds = new Date().getTime();
            launchScenarios.updateLaunchScenario(scenario);

            if (scenario.app.launch_uri === undefined){
                if (scenario.persona.resource === "Patient") {
                    $state.go('apps', {source: 'patient'});
                } else if (scenario.persona.resource === "Practitioner"){
                    if (scenario.patient.name === "None") {
                        $state.go('apps', {source: 'practitioner'});
                    } else {
                        $state.go('apps', {source: 'practitioner-patient'});
                    }
                }
            } else if (scenario.patient.name === 'None'){
                launchApp.launch(scenario.app);
            } else {
                launchApp.launch(scenario.app, scenario.patient);
            }
        };

        $scope.delete = function(scenario){
            launchScenarios.deleteLaunchScenario(scenario);
            $scope.selectedScenario = {};
            $scope.showing.detail = false;
        };

        $rootScope.$on('recent-selected', function(event, arg){
            $scope.showing.detail = true;
            $scope.selectedScenario = arg;
            $scope.desc = descriptionBuilder.launchScenarioDescription($scope.selectedScenario);
            launchScenarios.setSelectedScenario(arg);
        });

        $rootScope.$on('full-selected', function(event, arg){
            $scope.showing.detail = true;
            $scope.selectedScenario = arg;
            $scope.desc = descriptionBuilder.launchScenarioDescription($scope.selectedScenario);
            launchScenarios.setSelectedScenario(arg);
        });

    }).controller("ContextParamController",
    function($scope, launchScenarios){

        $scope.selectedContext = {};
        $scope.contextSelected = false;
        $scope.contextName = "";
        $scope.contextValue = "";
        $scope.contextNameIsValid = false;
        $scope.contextValueIsValid = false;

        $scope.toggleAddingContext = function() {
            $scope.showing.addingContext = !$scope.showing.addingContext;
        };

        $scope.$watchGroup(['contextName', 'contextValue'], function() {
            $scope.contextNameIsValid = $scope.contextName.trim() !== "";
            $scope.contextValueIsValid = $scope.contextValue.trim() !== "";
        });

        $scope.contextIsValid = function() {
            return $scope.contextNameIsValid && $scope.contextValueIsValid;
        };

        $scope.saveContextParam = function() {
            if ($scope.contextNameIsValid && $scope.contextValueIsValid){
                $scope.selectedScenario.contextParams.push({name: $scope.contextName, value: $scope.contextValue});
                launchScenarios.updateLaunchScenario($scope.selectedScenario);
                $scope.contextName = "";
                $scope.contextValue = "";
                $scope.showing.addingContext = false;
            }
        };

        $scope.delete = function() {
            $scope.selectedScenario.contextParams = $scope.selectedScenario.contextParams.filter(function( obj ) {
                return (obj !== $scope.selectedContext );
            });
            launchScenarios.updateLaunchScenario($scope.selectedScenario);
            $scope.selectedContext = {};
            $scope.contextSelected = false;
        };

        $scope.cancel = function() {
            $scope.contextName = "";
            $scope.contextValue = "";
            $scope.showing.addingContext = false;
        };

        $scope.selectContext = function(contextItem){
            // Toggle selection
            if ($scope.selectedContext === contextItem) {
                $scope.selectedContext = {};
                $scope.contextSelected = false;
            } else {
                $scope.selectedContext = contextItem;
                $scope.contextSelected = true;
            }
        };

    }).controller("RecentTableCtrl",
    function($rootScope, $scope, launchScenarios){
        $scope.selectedScenario = '';
        $scope.launchScenarioList = [];
        $scope.fullTable = false;

        $scope.scenarioSelected = function(scenario) {
            $scope.selectedScenario = scenario;
            $rootScope.$emit('recent-selected', $scope.selectedScenario)
        };

        $rootScope.$on('launch-scenario-list-update', function(){
            $scope.launchScenarioList = launchScenarios.getRecentLaunchScenarioList();
            $rootScope.$digest();
        });

        $rootScope.$on('full-selected', function(){
            $scope.selectedScenario = '';
        });

    }).controller("FullTableCtrl",
    function($rootScope, $scope, launchScenarios){
        $scope.selectedScenario = '';
        $scope.launchScenarioList = [];
        $scope.fullTable = true;

        $scope.scenarioSelected = function(scenario) {

            $scope.selectedScenario = scenario;
            $rootScope.$emit('full-selected', $scope.selectedScenario);
        };

        $rootScope.$on('launch-scenario-list-update', function(){
            $scope.launchScenarioList = launchScenarios.getFullLaunchScenarioList();
            $rootScope.$digest();
        });

        $rootScope.$on('recent-selected', function(){
            $scope.selectedScenario = '';
        });

    }).controller("AppsViewController", function($rootScope, $scope, $state, $stateParams, apps, customFhirApp, launchApp, launchScenarios, $uibModal) {
        $scope.all_user_apps = [];
        var source = $stateParams.source;
        var action = $stateParams.action;

        if (source === 'patient') {
            $scope.title = "Apps a Patient Can Launch";
            $scope.name = launchScenarios.getBuilder().persona.name;
            apps.getPatientApps.success(function(apps){
                $scope.all_user_apps = apps;
            });
        } else if (source === 'practitioner') {
            $scope.title = "Apps a Practitioner Can Launch Without a Patient Context";
            $scope.name = launchScenarios.getBuilder().persona.name;
            apps.getPractitionerApps.success(function(apps){
                $scope.all_user_apps = apps;
            });
        } else {
            $scope.title = "Apps a Practitioner Can Launch With a Patient Context";
            $scope.name = " Practitioner: " + launchScenarios.getBuilder().persona.name +
                " with Patient: " + launchScenarios.getBuilder().patient.name;
            apps.getPractitionerPatientApps.success(function(apps){
                $scope.all_user_apps = apps;
            });
        }

        $scope.launch = function launch(app){

            // choose for the launch scenario
            if (action === 'choose') {
                launchScenarios.setApp(app);
                openModalDialog(launchScenarios.getBuilder());
            } else {  // Launch
                if (source === 'patient' || source === 'practitioner-patient') {
                    launchApp.launch(app, launchScenarios.getSelectedScenario().patient);
                } else {
                    launchApp.launch(app);
                }
            }
        };

        function openModalDialog(scenario) {

            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'static/js/templates/launchScenarioModal.html',
                controller: 'ModalInstanceCtrl',
                size:'lg',
                resolve: {
                    getScenario: function () {
                        return scenario;
                    }
                }
            });

            modalInstance.result.then(function (scenario) {
                scenario.lastLaunchSeconds = new Date().getTime();
                launchScenarios.addFullLaunchScenarioList(scenario);
                $state.go('launch-scenarios', {});
            }, function () {
            });
        }

        $scope.customapp = customFhirApp.get();

        $scope.launchCustom = function launchCustom(){
            customFhirApp.set($scope.customapp);
            $scope.launch({
                client_id: $scope.customapp.id,
                launch_uri: $scope.customapp.url,
                client_name: "Custom App",
                logo_uri: "http://www.hl7.org/implement/standards/fhir/assets/images/fhir-logo-www.png"
            });
        };

    }).controller('ModalInstanceCtrl',['$scope', '$uibModalInstance', "getScenario",
    function ($scope, $uibModalInstance, getScenario) {

        $scope.scenario = getScenario;

        $scope.saveLaunchScenario = function (scenario) {
            $uibModalInstance.close(scenario);
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    }]).controller('ConfirmModalInstanceCtrl',['$scope', '$uibModalInstance', 'getSettings',
    function ($scope, $uibModalInstance, getSettings) {

        $scope.title = (getSettings.title !== undefined) ? getSettings.title : "";
        $scope.ok = (getSettings.ok !== undefined) ? getSettings.ok : "Yes";
        $scope.cancel = (getSettings.cancel !== undefined) ? getSettings.cancel : "No";
        $scope.text = (getSettings.text !== undefined) ? getSettings.text : "Continue?";
        var callback = (getSettings.callback !== undefined) ? getSettings.callback : null;

        $scope.confirm = function (result) {
            $uibModalInstance.close(result);
            callback(result);
        };
    }]).controller('CreateNewPatientCtrl', function($scope, $uibModal, fhirApiServices) {
        var now = new Date();
        now.setMilliseconds(0);
        now.setSeconds(0);

        $scope.master = {
            resourceType: "Patient",
            active: true,
            name:[
                {given:[], family:[], text:""}
            ],
            birthDate: now
        };

        $scope.open = function () {

            $scope.newPatient = angular.copy($scope.master);

            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'static/js/templates/patientCreateModal.html',
                controller: 'CreatePatientModalInstanceCtrl',
                size:'md',
                resolve: {
                    modalPatient: function () {
                        return $scope.newPatient;
                    }
                }
            });

            modalInstance.result.then(function (modalPatient) {
                fhirApiServices.create(modalPatient);
            }, function () {
            });
        };

    }).controller('CreatePatientModalInstanceCtrl', function ($scope, $uibModalInstance, modalPatient) {

        $scope.modalPatient = modalPatient;

        $scope.isGivenNameValid = function() {
            return $scope.modalPatient.name[0].given != null && $scope.modalPatient.name[0].given != "";
        };

        $scope.isFamilyNameValid = function() {
            return $scope.modalPatient.name[0].family != null && $scope.modalPatient.name[0].family != "";
        };

        $scope.isGenderValid = function() {
            return $scope.modalPatient.gender != null;
        };

        $scope.isBirthDateValid = function() {
            return $scope.modalPatient.birthDate != null;
        };

        $scope.isPatientValid = function() {
            return $scope.isGivenNameValid() && $scope.isFamilyNameValid() && $scope.isGenderValid() && $scope.isBirthDateValid();
        };

        $scope.createPatient = function () {
            if ($scope.isPatientValid()) {
                $scope.modalPatient.name[0].text = $scope.modalPatient.name[0].given + " " + $scope.modalPatient.name[0].family;
                $uibModalInstance.close($scope.modalPatient);
                    console.log("successful response from patientSearch.create", arguments);
            } else {
                console.log("sorry not valid", arguments);
            }
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    }).controller('CreateNewPractitionerCtrl', function($scope, $uibModal, fhirApiServices) {
        var now = new Date();
        now.setMilliseconds(0);
        now.setSeconds(0);

        $scope.master = {
            resourceType: "Practitioner",
            active: true,
            name:{given:[], family:[], text:""},
            practitionerRole: [
                {specialty: [{coding: [{display: ""}] }],
                role: {coding: [{display: ""}] }}
            ]
        };

        $scope.open = function () {

            $scope.newPractitioner = angular.copy($scope.master);

            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'static/js/templates/practitionerCreateModal.html',
                controller: 'CreatePractitionerModalInstanceCtrl',
                size:'md',
                resolve: {
                    modalPractitioner: function () {
                        return $scope.newPractitioner;
                    }
                }
            });

            modalInstance.result.then(function (modalPractitioner) {
                fhirApiServices.create(modalPractitioner);
            }, function () {
            });
        };

    }).controller('CreatePractitionerModalInstanceCtrl', function ($scope, $uibModalInstance, modalPractitioner) {

        $scope.modalPractitioner = modalPractitioner;

        $scope.isGivenNameValid = function() {
            return $scope.modalPractitioner.name.given != null && $scope.modalPractitioner.name.given != "";
        };

        $scope.isFamilyNameValid = function() {
            return $scope.modalPractitioner.name.family != null && $scope.modalPractitioner.name.family != "";
        };

        $scope.isPractitionerValid = function() {
            return $scope.isGivenNameValid() && $scope.isFamilyNameValid();
        };

        $scope.createPractitioner = function () {
            if ($scope.isPractitionerValid()) {
                $scope.modalPractitioner.name.text = $scope.modalPractitioner.name.given + " " + $scope.modalPractitioner.name.family;
                $uibModalInstance.close($scope.modalPractitioner);
                console.log("successful response from patientSearch.create", arguments);
            } else {
                console.log("sorry not valid", arguments);
            }
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    }).controller("BindContextController",
    function($scope, fhirApiServices, $stateParams, oauth2, tools) {

        $scope.showing = {
            noPatientContext: true,
            createPatient: false,
            searchloading: true
        };

        $scope.selected = {
            selectedPatient: {},
            patientSelected: false
        };

        if (fhirApiServices.clientInitialized()) {
            // all is good
            $scope.showing.content = true;
        } else {
            // need to complete authorization cycle
            oauth2.login();
        }

        $scope.clientName = decodeURIComponent($stateParams.clientName)
            .replace(/\+/, " ");

        $scope.onSelected = $scope.onSelected || function(p){
            var pid = p.id;
            var client_id = tools.decodeURLParam($stateParams.endpoint, "client_id");

            fhirApiServices
                .registerContext({ client_id: client_id}, {patient: pid})
                .then(function(c){
                    var to = decodeURIComponent($stateParams.endpoint);
                    to = to.replace(/scope=/, "launch="+c.launch_id+"&scope=");
                    return window.location = to;
                });
        };
    });

