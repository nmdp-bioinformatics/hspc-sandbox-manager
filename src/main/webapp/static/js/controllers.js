'use strict';

angular.module('sandManApp.controllers', []).controller('navController',[
    "$rootScope", "$scope", "appsSettings", "fhirApiServices", "userServices", "oauth2", "sandboxManagement", "$location", "$state", "tools",
    function($rootScope, $scope, appsSettings, fhirApiServices, userServices, oauth2, sandboxManagement, $location, $state, tools) {

        $scope.size = {
            navBarHeight: 60,
            footerHeight: 60,
            sandboxBarHeight: 0
        };

        $scope.showing = {
            signout: false,
            signin: true,
            slimBlueBar: false,
            progress: false,
            loading: false,
            searchloading: false,
            navBar: true,
            sideNavBar: false,
            largeSidebar: true,
            start: false
        };

        $scope.title = {blueBarTitle: "The Healthcare Innovation Ecosystem"};
        $scope.messages = [];
        $scope.dashboard = {
            sandboxes: [],
            sandbox: {}
        };

        $rootScope.$on('message-notify', function(event, messages){
            $scope.messages = messages;
            $rootScope.$digest();
        });

        $rootScope.$on("$stateChangeStart", function(event, toState, toParams, fromState, fromParams){
            if (toState.authenticate && typeof fhirApiServices.fhirClient() === "undefined"){
                // User isn’t authenticated
                if (!window.location.hash.startsWith("#/after-auth")) {
                    $scope.signin();
                }
                event.preventDefault();
            } else if (toState.needsSandbox && !sandboxManagement.hasSandbox()){
                // User can't go to a page which requires a sandbox without a sandbox
                $scope.showing.navBar = false;
                $scope.showing.sideNavBar = false;
                $state.go('create-sandbox', {});
                event.preventDefault();
            } else if (toState.name == "progress" && !sandboxManagement.creatingSandbox()){
//                $scope.signin();
                event.preventDefault();
            } else if (toState.scenarioBuilderStep && sandboxManagement.getScenarioBuilder().persona === "") {
                $state.go('launch-scenarios', {});
                event.preventDefault();
            }
        });

        $scope.signin = function() {
            $state.go('login', {});
        };

        $rootScope.$on('signed-in', function(event, arg){
            var canceledSandboxCreate = (arg !== undefined && arg === 'cancel-sandbox-create');

            userServices.getOAuthUserFromServer().then(function(){
                $scope.oauthUser = userServices.getOAuthUser();
                $scope.showing.signin = false;
                $scope.showing.signout = true;
                getSandboxes();

                if (canceledSandboxCreate) {
                    $scope.dashboard();
                } else {
                    appsSettings.getSettings().then(function(settings){
                        
                        //Initial sign in with no sandbox specified
                        if (fhirApiServices.fhirClient().server.serviceUrl === settings.defaultServiceUrl) {
                            $scope.dashboard();
                        } else {
                            sandboxManagement.getSandboxById().then(function(sandboxExists){
                                if (sandboxExists) {
                                    $scope.title.blueBarTitle = sandboxManagement.getSandbox().name;
                                    sandboxSignIn();
                                } else {
                                    // $state.go('404', {});
                                    $scope.dashboard();
                                }
                            });
                        }
                    });
                }

            });

        });

        function sandboxSignIn() {
            $scope.showing.signin = false;
            $scope.showing.signout = true;
            $scope.showing.navBar = true;
            $scope.showing.sideNavBar = true;
            $scope.showing.slimBlueBar = true;
            $scope.size.sandboxBarHeight = 30;
            $state.go('launch-scenarios', {});
        }

        $rootScope.$on('hide-nav', function(){
            $scope.showing.navBar = false;
            $scope.showing.sideNavBar = false;
        });

        $scope.signout = function() {
            fhirApiServices.clearClient();
            userServices.clearOAuthUser();
            $scope.showing.signin = true;
            $scope.showing.signout = false;
            $scope.showing.navBar = true;
            $scope.showing.sideNavBar = false;
            oauth2.logout();
        };

        $scope.selectSandbox = function(sandbox) {
            if (sandboxManagement.getSandbox().sandboxId !== sandbox.sandboxId) {
                window.location.href = appsSettings.getSandboxUrlSettings().sandboxManagerRootUrl + "/" + sandbox.sandboxId
            }
        };

        $scope.createSandbox = function () {
            $state.go('create-sandbox', {});
        };

        $scope.dashboard = function() {
            window.location.href = appsSettings.getSandboxUrlSettings().sandboxManagerRootUrl + "/#/dashboard-view";
        };

        $scope.manageUserAccount = function() {
            userServices.userSettings();
        };

        $rootScope.$on('refresh-sandboxes', function(){
            getSandboxes();
        });


        function getSandboxes() {
            sandboxManagement.getUserSandboxesByUserId().then(function (sandboxesExists) {
                if (sandboxesExists) {
                    $scope.showing.signin = false;
                    $scope.showing.signout = true;
                    $scope.dashboard.sandboxes = sandboxManagement.getSandboxes();
                    $rootScope.$digest();
                }
            });
        }

        // $scope.$on('$viewContentLoaded', function(){
        if (fhirApiServices.clientInitialized()) {
            // $rootScope.$emit('signed-in');
        } else if (sessionStorage.tokenResponse) {
            fhirApiServices.initClient();
        } else if (sessionStorage.hspcAuthorized && !window.location.hash.startsWith("#/after-auth")) {
            oauth2.login();
        }
        // });

    }]).controller("AfterAuthController", // After auth
        function(fhirApiServices){
            fhirApiServices.initClient();
    }).controller("404Controller",
        function(){

    }).controller("ErrorController",
    function($scope, errorService){
        $scope.errorMessage = errorService.getErrorMessage();

    }).controller("StartController",
    function($scope, $state, $timeout, userServices){
        $scope.showing.navBar = true;
        $scope.showing.sideNavBar = false;
        $scope.showing.start = !sessionStorage.hspcAuthorized;

        $scope.signin = function() {
            $state.go('login', {});
        };
        $scope.signup = function() {
            userServices.createUser();
        };

    }).controller("DashboardViewController",
    function($scope, $rootScope, $state, userServices, sandboxManagement, sandboxInviteServices, appsSettings){
        $scope.showing.navBar = true;
        $scope.showing.sideNavBar = false;
        $scope.showing.slimBlueBar = false;
        $scope.size.sandboxBarHeight = 0;
        $scope.sandboxInvites = [];
        $scope.title.blueBarTitle = "Dashboard";

        getSandboxInvites();

        $scope.selectSandbox = function(sandbox) {
            window.location.href = appsSettings.getSandboxUrlSettings().sandboxManagerRootUrl + "/" + sandbox.sandboxId
        };

        $scope.updateSandboxInvite = function (sandboxInvite, status) {
            sandboxInviteServices.updateSandboxInvite(sandboxInvite, status).then(function () {
                getSandboxInvites();
                $rootScope.$emit('refresh-sandboxes');

            });
        };

        function getSandboxInvites() {
            sandboxInviteServices.getSandboxInvitesByLdapId("PENDING").then(function (results) {
                $scope.sandboxInvites = results;
            });
        }

    }).controller("SandboxUserViewController",
    function($scope, $rootScope, sandboxManagement, sandboxInviteServices, userServices, $uibModal){
        $scope.userRoles = sandboxManagement.getSandbox().userRoles;
        $scope.sandboxInvites = [];
        $scope.newUserEmail = "";
        $scope.validEmail = false;

        getSandboxInvites();

        $scope.showDelete = function (ldapId) {
            if (sandboxManagement.getSandbox().createdBy.ldapId.toLowerCase() === userServices.getOAuthUser().ldapId.toLowerCase()) {
                return sandboxManagement.getSandbox().createdBy.ldapId.toLowerCase() !== ldapId.toLowerCase();
            }
            return false
        };

        $scope.removeUser = function (ldapId) {
            sandboxManagement.removeUserFromSandboxByUserId(ldapId).then(function () {
                sandboxManagement.getSandboxById().then(function(){
                    $scope.userRoles = sandboxManagement.getSandbox().userRoles;
                });
            });
        };

        $scope.revokeInvite = function (invite) {
            sandboxInviteServices.updateSandboxInvite(invite, "REVOKED").then(function () {
                getSandboxInvites();
            });
        };

        $scope.resendInvite = function (ldapId) {
            sandboxInviteServices.createSandboxInvite(ldapId).then(function () {
                getSandboxInvites();
            });
        };

        $scope.sendInvite = function () {
            sandboxInviteServices.createSandboxInvite($scope.newUserEmail).then(function () {
                getSandboxInvites();
            });
        };

        $scope.$watch('newUserEmail', function() {
            $scope.validEmail = validateEmail($scope.newUserEmail);
        });

        function validateEmail(email) {
            var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
            return re.test(email);
        }
        
        function getSandboxInvites() {
            sandboxInviteServices.getSandboxInvitesBySandboxId("PENDING").then(function (results) {
                $scope.sandboxInvites = results;
                sandboxInviteServices.getSandboxInvitesBySandboxId("REJECTED").then(function (results) {
                    angular.forEach(results, function (invite) {
                        $scope.sandboxInvites.push(invite);
                    });
                });
            });
        }
        
    }).controller("FutureController",
    function(){

    }).controller("DataManagerController",
    function($scope, fhirApiServices, $uibModal, $filter){

        $scope.showing = {results: false};
        $scope.bundleResults = "";

        $scope.upload = function (bundle){

            var modalProgress = openModalProgressDialog();
            fhirApiServices.createBundle(bundle).then(function (results) {
                $scope.bundleResults = $filter('json')(results);
                $scope.showing.results = true;
                // modalProgress.dismiss();
            }, function(results) {
                $scope.bundleResults = results;
                $scope.showing.results = true;
                // modalProgress.dismiss();
            });
        };

        function openModalProgressDialog() {
            return $uibModal.open({
                animation: true,
                templateUrl: 'static/js/templates/progressModal.html',
                controller: 'ProgressModalCtrl',
                size: 'sm',
                resolve: {
                    getTitle: function () {
                        return "Importing...";
                    }
                }
            });
        }

    }).controller("CreateSandboxController",
    function($rootScope, $scope, $state, sandboxManagement, tools, appsSettings){

        $scope.showing.navBar = true;
        $scope.showing.sideNavBar = false;
        $scope.showing.slimBlueBar = false;
        $scope.isIdValid = false;
        $scope.showError = false;
        $scope.isNameValid = true;
        $scope.tempSandboxId = "<sandbox id>";
        $scope.sandboxName = "";
        $scope.sandboxId = "";
        $scope.sandboxDesc = "";
        $scope.createEnabled = true;
        $scope.title.blueBarTitle = "Create Sandbox";

        $scope.baseUrl = appsSettings.getSandboxUrlSettings().sandboxManagerRootUrl;

        $scope.$watchGroup(['sandboxId', 'sandboxName'], function() {
            $scope.validateId($scope.sandboxId).then(function(valid){
                $scope.isIdValid = valid;
                $scope.showError = !$scope.isIdValid && ($scope.sandboxId !== "" && $scope.sandboxId !== undefined);
                $scope.isNameValid = $scope.validateName($scope.sandboxName);
                $scope.createEnabled = ($scope.isIdValid && $scope.isNameValid);
            });
        });

        $scope.validateId = function(id) {
            var deferred = $.Deferred();

            $scope.invalidMessage = "ID Not Available";
            if ($scope.tempSandboxId !== id ) {
                $scope.tempSandboxId = id;
                if (id !== undefined && id !== "" && id.length <= 20 && /^[a-zA-Z0-9]*$/.test(id)) {
                    tools.checkForSandboxById(id).then(function(sandbox){
                        deferred.resolve(sandbox === undefined || sandbox === "");
                    });
                } else {
                    $scope.tempSandboxId = "<sandbox id>";
                    $scope.invalidMessage = "ID Is Invalid";
                    deferred.resolve(false);
                }
            } else {
                deferred.resolve($scope.isIdValid);
            }
            return deferred;

        };

        $scope.validateName = function(name) {
            if (name !== undefined && name !== "") {
                if (name.length > 50) {
                    return false;
                }
            }
            return true;
        };

        $scope.cancel = function() {
            $rootScope.$emit('signed-in', 'cancel-sandbox-create');
        };

        $scope.createSandbox = function() {
            sandboxManagement.setCreatingSandbox(true);
            $scope.showing.progress = true;
            if ($scope.sandboxName === undefined || $scope.sandboxName === "") {
                $scope.sandboxName = $scope.sandboxId;
            }
            sandboxManagement.createSandbox({sandboxId: $scope.sandboxId, sandboxName: $scope.sandboxName, description: $scope.sandboxDesc}).then(function(sandbox){
                sandboxManagement.setCreatingSandbox(false);
                $scope.showing.progress = false;
                $rootScope.$emit('sandbox-created');
            }).fail(function() {
                    sandboxManagement.setCreatingSandbox(false);
                    $state.go('error', {});
            });

            $state.go('progress', {});
        };

    }).controller("LoginController",
    function($rootScope, $scope, $state, oauth2, fhirApiServices, sandboxManagement){

        if (fhirApiServices.clientInitialized()) {
            $rootScope.$emit('signed-in');
        } else {
            oauth2.login();
        }

    }).controller("SideBarController",
    function($rootScope, $scope){

        var sideBarStates = ['launch-scenarios','users', 'patients', 'practitioners', 'manage-apps'];

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
            patientSelected: false,
            patientResources: [],
            chartConfig: {}
        };

    }).controller("PatientDetailController",
    function($scope, $rootScope, $state, sandboxManagement, $filter, launchApp){

        if ($state.current.name === 'patients') {
            $scope.showing.patientDataManager = true;
        }

        if ($state.current.name === 'patient-view') {
            $scope.showing.selectForScenario = true;
        }

        $scope.setPatient = function(p){
            if (sandboxManagement.getScenarioBuilder().persona === '') {
                sandboxManagement.getScenarioBuilder().persona =
                    {
                        fhirId: p.id,
                        resource: p.resourceType,
                        fullUrl: p.fullUrl,
                        name: $filter('nameGivenFamily')(p)
                    };

                sandboxManagement.getScenarioBuilder().patient =
                    {
                        fhirId: p.id,
                        resource: p.resourceType,
                        name: $filter('nameGivenFamily')(p)
                    };
                $state.go('apps', {source: 'patient', action: 'choose'});
//                $state.go($state.current, {source: 'patient'}, {reload: true});
            } else {
                sandboxManagement.getScenarioBuilder().patient =
                    {
                        fhirId: p.id,
                        resource: p.resourceType,
                        name: $filter('nameGivenFamily')(p)
                    };
                $state.go('apps', {source: 'practitioner-patient', action: 'choose'});
            }
        };

        $scope.launchPatientDataManager = function(patient){
            launchApp.launchPatientDataManager(patient);
        };

    }).controller("PatientSearchController",
    function($scope, $rootScope, $state, $filter, $stateParams, fhirApiServices, sandboxManagement, patientResources) {

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
            $scope.showing.navBar = false;
            $scope.showing.sideNavBar = false;
            $rootScope.$emit('hide-nav');
        } else { // Patient View
            $scope.showing.noPatientContext =  false;
            $scope.showing.createPatient =  true;
        }

        var resourcesNames = [];
        var resourceCounts = [];

        function emptyArray(array){
            while (array.length > 0) {
                array.pop();
            }
        }

        $scope.selected.chartConfig = {
            options: {
                chart: {
                    type: 'bar'
                },
                legend: {
                    enabled: false
                }
            },
            xAxis: {
                categories: resourcesNames,
//                lineWidth: 0,
//                minorGridLineWidth: 0,
//                lineColor: 'transparent',
//                minorTickLength: 0,
//                tickLength: 0,
                title: {
                    text: null
                }
            },
            yAxis: {
                min: 0,
                labels: {
                    overflow: 'justify'
                },
//                lineWidth: 0,
//                minorGridLineWidth: 0,
//                lineColor: 'transparent',
//                minorTickLength: 0,
//                tickLength: 0,
                title: {
                    text: null
                }
            },series: [{
                type: 'bar',
                name: "Resource Count",
                data: resourceCounts,
                dataLabels: {
                    enabled: true
                },
                color: '#00AEEF'
            }],
            subtitle: {
                text: null
            },
            title: {
                text: null
            },
            credits: {
                enabled: false
            }
        };

        $scope.onSelected = $scope.onSelected || function(p){
            if ($scope.selected.selectedPatient !== p) {
                $scope.selected.selectedPatient = p;
                $scope.selected.patientSelected = true;
                $scope.showing.patientDetail = true;

                patientResources.getSupportedResources().done(function(resources){
                    $scope.selected.patientResources = [];
                    for (var i = 0; i < resources.length; i++) {
                        var query = {};
                        query[resources[i].patientSearch] = "Patient/"+ p.id;
                        fhirApiServices.queryResourceInstances(resources[i].resourceType, query, undefined, undefined, 1)
                            .then(function(resource, queryResult){
                                $scope.selected.patientResources.push({resourceType: queryResult.config.type, count: queryResult.data.total});
                                $scope.selected.patientResources = $filter('orderBy')($scope.selected.patientResources, "resourceType");

                                emptyArray(resourcesNames);
                                emptyArray(resourceCounts);
                                angular.forEach($scope.selected.patientResources, function (resource) {
                                    resourcesNames.push(resource.resourceType);
                                    resourceCounts.push(parseInt(resource.count));
                                });

                                $rootScope.$digest();
                            });
                    }
                });
            }
        };

        $scope.skipPatient = function(){
            sandboxManagement.getScenarioBuilder().patient =
                {
                    fhirId: 0,
                    resource: "None",
                    name: "None"
                };
            $state.go('apps', {source: 'practitioner', action: 'choose'});
        };

        $scope.mayLoadMore = true;
        $scope.patients = [];
        $scope.genderglyph = {"female" : "&#9792;", "male": "&#9794;"};
        $scope.searchterm = "";
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
            fhirApiServices.queryResourceInstances("Patient", undefined, $scope.tokens, [['family','asc'],['given','asc']])
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

        $rootScope.$on('patient-created', function(){
            $scope.getMore();
        });

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
    function($scope, $rootScope, $state, $filter, sandboxManagement){

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
            sandboxManagement.getScenarioBuilder().persona =
                {
                    fhirId: p.id,
                    resource: p.resourceType,
                    fullUrl: p.fullUrl,
                    name: $filter('nameGivenFamily')(p)
                };
            $state.go('patient-view', {source: 'patient'});
        };
    }).controller("PractitionerSearchController",
    function($scope, $rootScope, $state, $stateParams, fhirApiServices) {

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
            fhirApiServices.queryResourceInstances("Practitioner", undefined, $scope.tokens, [['family','asc'],['given','asc']])
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

        $rootScope.$on('practitioner-created', function(){
            $scope.getMore();
        });

    }).controller("LaunchScenariosController",
    function($rootScope, $scope, $state, sandboxManagement, launchApp, userServices, descriptionBuilder){
        $scope.showing = {detail: false, addingContext: false};
        $scope.isCustom = false;
        $scope.selectedScenario = {};
        sandboxManagement.getSandboxLaunchScenarios();
        sandboxManagement.clearScenarioBuilder();
        sandboxManagement.getScenarioBuilder().owner = userServices.getOAuthUser();

        $scope.launch = function(scenario){
            scenario.lastLaunchSeconds = new Date().getTime();
            sandboxManagement.updateLaunchScenario(scenario);

            if (scenario.patient.name === 'None'){
                launchApp.launch(scenario.app, undefined, scenario.contextParams, scenario.persona);
            } else {
                launchApp.launch(scenario.app, scenario.patient, scenario.contextParams, scenario.persona);
            }
        };

        $scope.launchPatientDataManager = function(patient){
            launchApp.launchPatientDataManager(patient);
        };

        $scope.delete = function(scenario){
            sandboxManagement.deleteLaunchScenario(scenario);
            $scope.selectedScenario = {};
            $scope.showing.detail = false;
        };

        $rootScope.$on('recent-selected', function(event, arg){
            $scope.showing.detail = true;
            $scope.selectedScenario = arg;
            $scope.isCustom = ($scope.selectedScenario.app.authClient.authDatabaseId === null);
            $scope.desc = descriptionBuilder.launchScenarioDescription($scope.selectedScenario);
            sandboxManagement.setSelectedScenario(arg);
        });

        $rootScope.$on('full-selected', function(event, arg){
            $scope.showing.detail = true;
            $scope.selectedScenario = arg;
            $scope.isCustom = ($scope.selectedScenario.app.authClient.authDatabaseId === null);
            $scope.desc = descriptionBuilder.launchScenarioDescription($scope.selectedScenario);
            sandboxManagement.setSelectedScenario(arg);
        });

    }).controller("ContextParamController",
    function($scope, sandboxManagement){

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
                sandboxManagement.updateLaunchScenario($scope.selectedScenario);
                $scope.contextName = "";
                $scope.contextValue = "";
                $scope.showing.addingContext = false;
            }
        };

        $scope.delete = function() {
            $scope.selectedScenario.contextParams = $scope.selectedScenario.contextParams.filter(function( obj ) {
                return (obj !== $scope.selectedContext );
            });
            sandboxManagement.updateLaunchScenario($scope.selectedScenario);
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
    function($rootScope, $scope, sandboxManagement){
        $scope.selectedScenario = '';
        $scope.launchScenarioList = [];
        $scope.fullTable = false;

        $scope.scenarioSelected = function(scenario) {
            $scope.selectedScenario = scenario;
            $rootScope.$emit('recent-selected', $scope.selectedScenario)
        };

        $rootScope.$on('launch-scenario-list-update', function(){
            $scope.launchScenarioList = sandboxManagement.getRecentLaunchScenarioList();
            $rootScope.$digest();
        });

        $rootScope.$on('full-selected', function(){
            $scope.selectedScenario = '';
        });

    }).controller("FullTableCtrl",
    function($rootScope, $scope, sandboxManagement){
        $scope.selectedScenario = '';
        $scope.launchScenarioList = [];
        $scope.fullTable = true;

        $scope.scenarioSelected = function(scenario) {

            $scope.selectedScenario = scenario;
            $rootScope.$emit('full-selected', $scope.selectedScenario);
        };

        $rootScope.$on('launch-scenario-list-update', function(){
            $scope.launchScenarioList = sandboxManagement.getFullLaunchScenarioList();
            $rootScope.$digest();
        });

        $rootScope.$on('recent-selected', function(){
            $scope.selectedScenario = '';
        });

    }).controller("AppPickerController", function($rootScope, $scope, $state, $stateParams, appRegistrationServices, appsService, customFhirApp, launchApp, sandboxManagement, $uibModal) {
        $scope.all_user_apps = [];
        var source = $stateParams.source;
        var action = $stateParams.action;

        $scope.title =  "Registered Apps";
        $scope.showCustomApp = true;

        appsService.getSampleApps().done(function(patientApps){
            $scope.all_user_apps = angular.copy(appRegistrationServices.getAppList());
            for (var i=0; i < patientApps.length; i++) {
                if (patientApps[i]["isDefault"] !== undefined) {
                    $scope.all_user_apps.push(angular.copy(patientApps[i]));
                }
            }
        });

        $scope.select = function launch(app){

            // choose for the launch scenario
            if (action === 'choose') {
                sandboxManagement.getScenarioBuilder().app = app;
                openModalDialog(sandboxManagement.getScenarioBuilder());
            } else {  // Launch
                if (source === 'patient' || source === 'practitioner-patient') {
                    launchApp.launch(app, sandboxManagement.getSelectedScenario().patient);
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

            modalInstance.result.then(function (result) {
                var scenario = result.scenario;
                if (result.launch) {
                    if (scenario.patient.name === 'None'){
                        launchApp.launch(scenario.app, undefined, scenario.contextParams, scenario.persona);
                    } else {
                        launchApp.launch(scenario.app, scenario.patient, scenario.contextParams, scenario.persona);
                    }
                } else {
                    sandboxManagement.addFullLaunchScenarioList(scenario);
                }
                $state.go('launch-scenarios', {});
            }, function () {
            });
        }

        // get from localStorage
        $scope.customapp = customFhirApp.get();

        $scope.launchCustom = function launchCustom(){
            //set localStorage
            customFhirApp.set($scope.customapp);
            $scope.select({
                launchUri: $scope.customapp.url,
                authClient: {clientName: "Custom App",
                             clientId:$scope.customapp.id,
                             isCustom: true
                            }
            });
        };

    }).controller('ModalInstanceCtrl',['$scope', '$uibModalInstance', "getScenario",
    function ($scope, $uibModalInstance, getScenario) {

        $scope.scenario = getScenario;

        $scope.saveLaunchScenario = function (scenario, launch) {
            var result = {
                scenario: scenario,
                launch: launch
            };
            $uibModalInstance.close(result);
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    }]).controller('ProgressModalCtrl',['$scope', '$uibModalInstance', "getTitle",
    function ($scope, $uibModalInstance, getTitle) {

        $scope.title = getTitle;

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
    }]).controller('CreateNewPatientCtrl', function($scope, $rootScope, $uibModal, fhirApiServices) {
        var now = new Date();
        now.setMilliseconds(0);
        now.setSeconds(0);

        $scope.master = {
            resourceType: "Patient",
            active: true,
            name:[
                {given:[], family:[], text:""}
            ],
            birthDateTime: now
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
                // capture the date only for the birthDate value
                modalPatient.birthDate = modalPatient.birthDateTime.toISOString().substring(0, 10);
                // todo support storing the birthDateTime in the extention when HSPC supports it
                fhirApiServices.createResourceInstance(modalPatient);
                $rootScope.$emit('patient-created');
            }, function () {
            });
        };

    }).controller('CreatePatientModalInstanceCtrl', function ($scope, $filter, $uibModalInstance, modalPatient) {

        $scope.modalPatient = modalPatient;

        $scope.isGivenNameValid = function() {
            return $scope.modalPatient.name[0].given[0] != null && $scope.modalPatient.name[0].given[0] != "";
        };

        $scope.isFamilyNameValid = function() {
            return $scope.modalPatient.name[0].family[0] != null && $scope.modalPatient.name[0].family[0] != "";
        };

        $scope.isGenderValid = function() {
            return $scope.modalPatient.gender != null;
        };

        $scope.isBirthDateValid = function() {
            return $scope.modalPatient.birthDateTime != null;
        };

        $scope.isPatientValid = function() {
            return $scope.isGivenNameValid() && $scope.isFamilyNameValid() && $scope.isGenderValid() && $scope.isBirthDateValid();
        };

        $scope.createPatient = function () {
            if ($scope.isPatientValid()) {
                $scope.modalPatient.name[0].text = $filter('nameGivenFamily')($scope.modalPatient);
                $uibModalInstance.close($scope.modalPatient);
            } else {
                console.log("sorry not valid", arguments);
            }
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    }).controller('CreateNewPractitionerCtrl', function($scope, $rootScope, $uibModal, fhirApiServices) {
        var now = new Date();
        now.setMilliseconds(0);
        now.setSeconds(0);

        $scope.master = {
            resourceType: "Practitioner",
            active: true,
            name:{given:[], family:[], text:"", suffix:[]},
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
                fhirApiServices.createResourceInstance(modalPractitioner);
                $rootScope.$emit('practitioner-created');
            }, function () {
            });
        };

    }).controller('CreatePractitionerModalInstanceCtrl', function ($scope, $filter, $uibModalInstance, modalPractitioner) {

        $scope.modalPractitioner = modalPractitioner;

        $scope.isGivenNameValid = function() {
            return $scope.modalPractitioner.name.given[0] != null && $scope.modalPractitioner.name.given[0] != "";
        };

        $scope.isFamilyNameValid = function() {
            return $scope.modalPractitioner.name.family[0] != null && $scope.modalPractitioner.name.family[0] != "";
        };

        $scope.isPractitionerValid = function() {
            return $scope.isGivenNameValid() && $scope.isFamilyNameValid();
        };

        $scope.createPractitioner = function () {
            if ($scope.isPractitionerValid()) {
                $scope.modalPractitioner.name.text = $filter('nameGivenFamily')($scope.modalPractitioner);
                $uibModalInstance.close($scope.modalPractitioner);
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
    }).controller("AppsController", function($scope, $rootScope, $state, appRegistrationServices, sandboxManagement, $uibModal) {

    $scope.all_user_apps = [];
    $scope.galleryOffset = 246;
    $scope.canDelete = false;
    $scope.showCustomApp = false;

    $scope.showing = {appDetail: false};

    $scope.selected = {
        selectedApp: {}
    };
    $scope.clientJSON = {};

    appRegistrationServices.getSandboxApps();

    $rootScope.$on('app-list-update', function () {
        $scope.all_user_apps = appRegistrationServices.getAppList();
        $rootScope.$digest();
    });

    $scope.registration = function () {
        var modalInstance = $uibModal.open({
            animation: true,
            templateUrl: 'static/js/templates/registerAppModal.html',
            controller: 'AppRegistrationModalCtrl',
            size: 'lg'

        });

        modalInstance.result.then(function (app) {
            var modalProgress = openModalProgressDialog();
            appRegistrationServices.createSandboxApp(app).then(function (result) {
                modalProgress.dismiss();
            }, function(err) {
                modalProgress.dismiss();
                $state.go('error', {});
            });
        });
    };

    function openModalProgressDialog() {
        return $uibModal.open({
            animation: true,
            templateUrl: 'static/js/templates/progressModal.html',
            controller: 'ProgressModalCtrl',
            size: 'sm',
            resolve: {
                getTitle: function () {
                    return "Saving...";
                }
            }
        });
    }
    
    $scope.select = function (app) {
        canDeleteApp(app.id);
        $scope.selected.selectedApp = app;
        $scope.showing.appDetail = true;
        delete $scope.clientJSON.logo;
        if (app.clientJSON) {
            $scope.clientJSON = app.clientJSON;
        } else {
            delete $scope.clientJSON.logoUri;
        }
        appRegistrationServices.getSandboxApp(app.id).then(function (resultApp) {
            $scope.galleryOffset = 80;
            $scope.selected.selectedApp.clientJSON = JSON.parse(resultApp.clientJSON);
            $scope.clientJSON = $scope.selected.selectedApp.clientJSON;
            $scope.clientJSON.launchUri = $scope.selected.selectedApp.launchUri;

            $rootScope.$digest();
        });
    };

    function canDeleteApp(appId){
        sandboxManagement.getLaunchScenarioByApp(appId).then(function (launchScenarios) {
            $scope.canDelete = !(launchScenarios.length > 0);
            $rootScope.$digest();
        });
    }

    $scope.updateFile = function(files) {

        $scope.myFile = files[0];

        var reader = new FileReader();
        reader.onload = function (e) {
            $scope.clientJSON.logo = e.target.result;
            $rootScope.$digest();
        };
        var url = reader.readAsDataURL(files[0]);
    };

    $scope.save = function (){
        $scope.selected.selectedApp.logo = $scope.myFile;
        var updateClientJSON = angular.copy($scope.clientJSON);
        delete updateClientJSON.logo;
        if( Object.prototype.toString.call( updateClientJSON.redirectUris ) !== '[object Array]' &&
                typeof updateClientJSON.redirectUris !== 'undefined') {
            updateClientJSON.redirectUris = updateClientJSON.redirectUris.split(',');
        }
        if( Object.prototype.toString.call( updateClientJSON.scope ) !== '[object Array]' &&
            typeof updateClientJSON.scope !== 'undefined') {
            updateClientJSON.scope = updateClientJSON.scope.split(',');
        }

        $scope.selected.selectedApp.clientJSON = updateClientJSON;
        $scope.selected.selectedApp.launchUri = updateClientJSON.launchUri;
        var modalProgress = openModalProgressDialog();
        appRegistrationServices.updateSandboxApp($scope.selected.selectedApp).then(function (result) {
            modalProgress.dismiss();
        }, function(err) {
            modalProgress.dismiss();
            $state.go('error', {});
        });
    };

    $scope.delete = function (){
        $scope.showing.appDetail = false;
        $uibModal.open({
            animation: true,
            templateUrl: 'static/js/templates/confirmModal.html',
            controller: 'ConfirmModalInstanceCtrl',
            resolve: {
                getSettings: function () {
                    return {
                        title:"Delete " + $scope.selected.selectedApp.authClient.clientName,
                        ok:"Yes",
                        cancel:"Cancel",
                        type:"confirm-error",
                        text:"Are you sure you want to delete?",
                        callback:function(result){ //setting callback
                            if (result == true) {
                                appRegistrationServices.deleteSandboxApp($scope.selected.selectedApp.id).then(function () {
                                    $scope.selected.selectedApp = {};
                                });
                            }
                        }
                    };
                }
            }
        });
        // appRegistrationServices.deleteSandboxApp($scope.selected.selectedApp.id).then(function () {
        //     $scope.selected.selectedApp = {};
        // });
    };

}).controller('AppRegistrationModalCtrl',function ($scope, $rootScope, sandboxManagement, $uibModalInstance) {

    $scope.clientType = "Public Client";
    // $scope.clientTypes = ["Confidential Client", "Public Client", "Backend Service"];
    $scope.clientTypes = ["Public Client", "Confidential Client"];

    $scope.clientJSON = {};

    $scope.sandboxName = sandboxManagement.getSandbox().name;

    $scope.uploadFile = function(files) {

        $scope.myFile = files[0];

        var reader = new FileReader();
        reader.onload = function (e) {
            $scope.clientJSON.logo = e.target.result;
            $rootScope.$digest();
        };
        var url = reader.readAsDataURL(files[0]);
    };

    $scope.$watchGroup(['clientJSON.clientName', 'clientJSON.launchUri', 'clientJSON.redirectUris'], function() {
            $scope.createEnabled = valueSet($scope.clientJSON.launchUri) && valueSet($scope.clientJSON.clientName);
    });

    function valueSet(value) {
        return (typeof value !== 'undefined' && value !== '');
    }

    $scope.registerApp = function (clientJSON) {

        if( Object.prototype.toString.call( clientJSON.redirectUris ) !== '[object Array]' &&
            typeof clientJSON.redirectUris !== 'undefined' ) {
            clientJSON.redirectUris = clientJSON.redirectUris.split(',');
        }
        if ($scope.clientType !== "Backend Service") {
            clientJSON.grantTypes = [ "authorization_code" ];
        } else {
            clientJSON.grantTypes = [ "client_credentials" ];
        }

        if ($scope.clientType !== "Public Client") {
            clientJSON.tokenEndpointAuthMethod = "SECRET_BASIC";
        } else {
            clientJSON.tokenEndpointAuthMethod = "NONE";
        }

        // Just adding some default scopes to start with
        clientJSON.scope = ["launch","patient/*.*","profile","openid"];

        var authClient = {
            clientName: clientJSON.clientName
        };

        var newApp = {
            launchUri: clientJSON.launchUri,
            logo: $scope.myFile,
            authClient: authClient
        };
        delete clientJSON.logo;
        newApp.clientJSON = clientJSON;
        $uibModalInstance.close(newApp);
    };

        $scope.cancel = function () {
            $uibModalInstance.dismiss();
        };
    }).controller('ProgressCtrl',['$rootScope', '$scope', '$state', '$timeout',
    function ($rootScope, $scope, $state, $timeout) {

        $scope.createProgress = 0;
        $scope.showing.navBar = false;
        $scope.showing.sideNavBar = false;

        var messageNum = 0;
        var messages = [
            "Create apps for practitioners that launch within an EHR, smart phone, tablet, or web browser",
            "Create apps for patients and their related persons that launch from a smart phone, tablet, web browser, or personal computer",
            "Create backend services that interact directly with HSPC Platforms",
            "Verify your app follows the SMART security and launch context standard",
            "Run your app against your very own FHIR server",
            "Test your apps by creating various launch scenarios",
            "Create practitioners, patients, and clinical data",
            "Verify that your app is HSPC compliant"
        ];
        updateProgress();
        fadeMessage();

        $rootScope.$on('sandbox-created', function(){
            $scope.createProgress = 100;
            $timeout(function() {
                $rootScope.$emit('signed-in');
            },500);
        });

        function fadeMessage(){
            $timeout(function() {
                $scope.message = messages[messageNum];
                $scope.showMessage = true;
                // Loading done here - Show message for 3 more seconds.
                $timeout(function() {
                    $scope.showMessage = false;
                    messageNum++;
                    if (messageNum <= 7) {
                        fadeMessage();
                    }
                },3000);
            }, 500);
        }

        function updateProgress(){
            $scope.createProgress += 0.333;   // Progress .333% at a time
            if ($scope.createProgress < 95) {  // If it hits 95%, hold there
                $timeout(updateProgress, 100);  // Wake up every tenth of a second and progress
            }
        }

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
    }]);

