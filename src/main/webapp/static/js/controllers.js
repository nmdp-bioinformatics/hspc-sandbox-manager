'use strict';

angular.module('sandManApp.controllers', []).controller('navController',[
    "$rootScope", "$scope", "appsSettings", "fhirApiServices", "userServices", "patientDetails", "oauth2", "$location", "$state",
    function($rootScope, $scope, appsSettings, fhirApiServices, userServices, patientDetails, oauth2, $location, $state) {

        $scope.showing = {
            signout: false,
            signin: true,
            loading: false,
            searchloading: false,
            navBar: false
        };
        $scope.messages = [];

        $rootScope.$on('message-notify', function(event, messages){
            $scope.messages = messages;
        });

        $rootScope.$on("$stateChangeStart", function(event, toState, toParams, fromState, fromParams){
            if (toState.authenticate && typeof window.fhirClient === "undefined"){
                // User isn’t authenticated
                $scope.signin();
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

        $scope.signout = function() {
            delete $rootScope.user;
            fhirApiServices.clearClient();
            oauth2.logout().then(function(){
                oauth2.login();
            });
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
        }

    }).controller("PatientSearchController",
    function($scope, $rootScope, $state, $stateParams, fhirApiServices, userServices, patientDetails, launchScenarios, launchApp, apps) {

        var source = $stateParams.source;
        $scope.showing = {patientDetail: false,
            noPatientContext: true,
            createPatient: true,
            searchloading: true};


        if (source === 'patient') {
            $scope.title = "Select the Patient Context";
            $scope.showing.noPatientContext =  true;
            $scope.showing.createPatient =  false;
        } else if (source === 'persona') {
            $scope.title = "Select the Patient Persona";
            $scope.showing.noPatientContext =  false;
            $scope.showing.createPatient =  false;
        } else {
            $scope.showing.noPatientContext =  false;
            $scope.showing.createPatient =  true;
        }

        $scope.onSelected = function(p){
            $scope.selectedPatient = p;
            $scope.patientSelected = true;
            $scope.showing.patientDetail =  true;
        };

        $scope.setPatient = function(p){
            if (launchScenarios.getBuilder().persona === '') {
                launchScenarios.setPersona(
                    {fhirId: $scope.selectedPatient.id,
                     resource: $scope.selectedPatient.resourceType,
                        fullUrl: $scope.selectedPatient.fullUrl,
                        name: patientDetails.name($scope.selectedPatient)});
                launchScenarios.setPatient(
                    {fhirId: $scope.selectedPatient.id,
                        resource: $scope.selectedPatient.resourceType,
                        name: patientDetails.name($scope.selectedPatient)});
                $state.go('apps', {source: 'patient', action: 'choose'});
//                $state.go($state.current, {source: 'patient'}, {reload: true});
            } else {
                launchScenarios.setPatient(
                    {fhirId: $scope.selectedPatient.id,
                        resource: $scope.selectedPatient.resourceType,
                        name: patientDetails.name($scope.selectedPatient)});
                $state.go('apps', {source: 'practitioner-patient', action: 'choose'});
            }
        };

        $scope.skipPatient = function(){
            launchScenarios.setPatient(
                {fhirId: 0,
                    resource: "None",
                    name: "None"});
            $state.go('apps', {source: 'practitioner', action: 'choose'});
        };

        $scope.launchPatientDataManager = function(patient){
            launchApp.launchPatientDataManager(patient);
        };

        $scope.mayLoadMore = true;
        $scope.patients = [];
        $scope.genderglyph = {"female" : "&#9792;", "male": "&#9794;"};
        $scope.searchterm = "";
        $scope.patientHelper = patientDetails;
        $scope.selectedPatient = {};
        $scope.patientSelected = false;
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

        $scope.updateProfile = function(){
            userServices.updateProfile($scope.selectedPatient);
        };
    }).controller("PractitionerSearchController",
    function($scope, $rootScope, $state, $stateParams, fhirApiServices, userServices, patientDetails, launchScenarios) {

        $scope.showing = {patientDetail: false};

        $scope.onSelected = function(p){
            $scope.selectedPatient = p;
            $scope.patientSelected = true;
            $scope.showing.patientDetail =  true;
        };

        $scope.setPatient = function(p){
            launchScenarios.setPersona(
                {fhirId: $scope.selectedPatient.id,
                    resource: $scope.selectedPatient.resourceType,
                    fullUrl: $scope.selectedPatient.fullUrl,
                    name: patientDetails.name($scope.selectedPatient)});
            $state.go('patient-view', {source: 'patient'});
        };

        $scope.showing = {searchloading: true};
        $scope.mayLoadMore = true;
        $scope.patients = [];
        $scope.genderglyph = {"female" : "&#9792;", "male": "&#9794;"};
        $scope.searchterm = "";
        $scope.patientHelper = patientDetails;
        $scope.selectedPatient = {};
        $scope.patientSelected = false;
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

            var list = $('#user-results');
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
            fhirApiServices.queryResourceInstances("Practitioner", undefined, $scope.tokens, [['given','asc'],['family','asc']])
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

        $scope.updateProfile = function(){
            userServices.updateProfile($scope.selectedPatient);
        };
    }).controller("LaunchScenariosController",
    function($rootScope, $scope, $state, launchScenarios, launchApp, userServices){
        $scope.showing = {detail: false};
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
            launchScenarios.setSelectedScenario(arg);
        });

        $rootScope.$on('full-selected', function(event, arg){
            $scope.showing.detail = true;
            $scope.selectedScenario = arg;
            launchScenarios.setSelectedScenario(arg);
        });

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

    }).controller("PractitionerDetailsController",
    function(fhirApiServices){

    }).controller("PractitionerDetailsController",
    function(fhirApiServices){

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
                launch_uri: $scope.customapp.url
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
    }]).controller('CreateNewPatient', function($scope, $uibModal) {
        var now = new Date();
        now.setMilliseconds(0);
        now.setSeconds(0);

        var text =
            '{"resourceType":"Patient",'
                + '"active":"true",'
                + '"name":['
                + '{"given":"", "family":"", "text":""}'
                + ']}';

        $scope.master = JSON.parse(text);
        $scope.master.birthDate = now;

        $scope.newPatient = {};

        $scope.reset = function() {
            $scope.newPatient = angular.copy($scope.master);
        };

        $scope.reset();


        $scope.open = function () {
            var modalInstance = $uibModal.open({
                animation: $scope.animationsEnabled,
                templateUrl: 'createPatientModal',
                controller: 'CreatePatientModalInstanceCtrl',
                resolve: {
                    modalPatient: function () {
                        return $scope.newPatient;
                    }
                }
            });

            modalInstance.result.then(function (modalPatient) {
                $scope.newPatient = modalPatient;
                // do something with new patient?
                // reset for future calls to create
                $scope.reset();
            }, function () { // dismissed
                $scope.reset();
            });
        };
    }).controller('CreatePatientModalInstanceCtrl', function ($scope, $uibModalInstance, modalPatient, fhirApiServices) {

        $scope.modalPatient = modalPatient;

        function changeClass(elementId, className) {
            if (elementId != null) {
                var element = document.getElementById(elementId);
                if (element != null) {
                    element.className = className;
                    $scope.isSaveDisabled = true;
                }
            }
        }

        function isGivenNameValid() {
            return modalPatient.name[0].given != null && modalPatient.name[0].given != "";
        }

        function isFamilyNameValid() {
            return modalPatient.name[0].family != null && modalPatient.name[0].family != "";
        }

        function isGenderValid() {
            return modalPatient.gender != null;
        }

        function isBirthDateValid() {
            return modalPatient.birthDate != null;
        }

        function isPatientValid() {
            return isGivenNameValid() && isFamilyNameValid() && isGenderValid() && isBirthDateValid();
        }

        function enableDisableCreatePatientButton() {
            isPatientValid()
                ? changeClass("createPatientButton", "btn btn-basic")
                : changeClass("createPatientButton", "btn btn-basic disabled");
        }

        function toggleFormControl(isValid, formControlId, formIconId) {
            if (isValid) {
                changeClass(formControlId, "form-group has-feedback");
                changeClass(formIconId, "glyphicon glyphicon-ok form-control-feedback");
            } else {
                changeClass(formControlId, "form-group has-error has-feedback");
                changeClass(formIconId, "glyphicon glyphicon-remove form-control-feedback");
            }
        }

        $scope.$watchGroup(['modalPatient.name[0].given', 'modalPatient.name[0].family', 'modalPatient.gender', 'modalPatient.birthDate'], function() {
            toggleFormControl(isGivenNameValid(), "givenNameHolder", "givenNameIcon");
            toggleFormControl(isFamilyNameValid(), "familyNameHolder", "familyNameIcon");
            toggleFormControl(isGenderValid(), "genderHolder", "genderIcon");
            toggleFormControl(isBirthDateValid(), "birthDateHolder", "birthDateIcon");

            enableDisableCreatePatientButton();
        });

        $scope.createPatient = function () {
            if (isPatientValid()) {
                $uibModalInstance.close();
                $scope.modalPatient.name[0].text = $scope.modalPatient.name[0].given + " " + $scope.modalPatient.name[0].family;
                if (fhirApiServices.create($scope.modalPatient)) {
                    $uibModalInstance.close($scope.modalPatient);
                    console.log("successful response from patientSearch.create", arguments);
                } else {
                    console.log("unsuccessful response from patientSearch.create", arguments);
                }
            } else {
                console.log("sorry not valid", arguments);
            }
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    });

