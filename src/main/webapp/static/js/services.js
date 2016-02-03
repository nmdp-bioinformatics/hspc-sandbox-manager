'use strict';

angular.module('sandManApp.services', [])
    .factory('oauth2', function($rootScope, $location, appsSettings) {

        var authorizing = false;

        return {
            authorizing: function(){
                return authorizing;
            },
            authorize: function(s){
                // window.location.origin does not exist in some non-webkit browsers
                if (!window.location.origin) {
                    window.location.origin = window.location.protocol + "//"
                        + window.location.hostname
                        + (window.location.port ? ':' + window.location.port: '');
                }

                var thisUri = window.location.origin + window.location.pathname;
                var thisUrl = thisUri.replace(/\/+$/, "/");

                var client = {
                    "client_id": "sand_man",
                    "redirect_uri": thisUrl,
                    "scope":  "smart/orchestrate_launch user/*.* profile openid"
                };
                authorizing = true;
                FHIR.oauth2.authorize({
                    client: client,
                    server: s.serviceUrl,
                    from: $location.url()
                }, function (err) {
                    authorizing = false;
                    $rootScope.$emit('error', err);
//                    $rootScope.$emit('set-loading');
//                    $rootScope.$emit('clear-client');
//                    var loc = "/ui/select-patient";
//                    if ($location.url() !== loc) {
//                        $location.url(loc);
//                    }
//                    $rootScope.$digest();
                });
            },
            logout: function(){
                var deferred = $.Deferred();
                appsSettings.getSettings().then(function(settings){
                    $.ajax({
                        url: settings.oauthLogoutUrl
                }).done(function(){
                            deferred.resolve();
                        }).fail(function(){
                        });
                });
                return deferred;
            },
            login: function(){
                var that = this;
                appsSettings.getSettings().then(function(settings){
                    that.authorize(settings);
                });
            }
        };

    }).factory('fhirApiServices', function (oauth2, appsSettings, patientDetails, $rootScope, $location) {

        /**
         *
         *      FHIR SERVICE API CALLS
         *
         **/

        var fhirClient;

        function getQueryParams(url) {
            var index = url.lastIndexOf('?');
            if (index > -1){
                url = url.substring(index+1);
            }
            var urlParams;
            var match,
                pl     = /\+/g,  // Regex for replacing addition symbol with a space
                search = /([^&=]+)=?([^&]*)/g,
                decode = function (s) { return decodeURIComponent(s.replace(pl, " ")); },
                query  = url;

            urlParams = {};
            while (match = search.exec(query))
                urlParams[decode(match[1])] = decode(match[2]);
            return urlParams;
        }

        return {
            clearClient: function(){
                fhirClient = null;
                sessionStorage.clear();
            },
            fhirClient: function(){
                return fhirClient;
            },
            clientInitialized: function(){
                return (fhirClient !== undefined && fhirClient !== null);
            },
            initClient: function(){
                var params = getQueryParams($location.url());
                if (params.code){
                    delete sessionStorage.tokenResponse;
                    FHIR.oauth2.ready(params, function(newSmart){
                        if (newSmart && newSmart.state && newSmart.state.from !== undefined){
                            $location.url(newSmart.state.from);
                            fhirClient = newSmart;
                            window.fhirClient = fhirClient;
                            $rootScope.$emit('signed-in');
                            $rootScope.$digest();
                        }
                    });
                } else {
                    appsSettings.getSettings().then(function(settings){
                        oauth2.authorize(settings);
                    });
                }
            },
            hasNext: function(lastSearch) {
                var hasLink = false;
                if (lastSearch  === undefined) {
                    return false;
                } else {
                    lastSearch.data.link.forEach(function(link) {
                        if (link.relation == "next") {
                            hasLink = true;
                        }
                    });
                }
                return hasLink;
            },
            getNextOrPrevPage: function(direction, lastSearch) {
                var deferred = $.Deferred();
                $.when(fhirClient.api[direction]({bundle: lastSearch.data}))
                    .done(function(pageResult){
                        var resources = [];
                        if (pageResult.data.entry) {
                            pageResult.data.entry.forEach(function(entry){
                                resources.push(entry.resource);
                            });
                        }
                        deferred.resolve(resources, pageResult);
                    });
                return deferred;
            },
            queryResourceInstances: function(resource, searchValue, tokens, sort) {
                var deferred = $.Deferred();

                var searchParams = {type: resource, count: 50};
                searchParams.query = {};
                if (searchValue !== undefined) {
                    searchParams.query = searchValue;
                }
                if (typeof sort !== 'undefined' ) {
                    searchParams.query['$sort'] = sort;
                }
                if (typeof sort !== 'undefined' ) {
                    searchParams.query['name'] = tokens;
                }

                $.when(fhirClient.api.search(searchParams))
                    .done(function(resourceSearchResult){
                        var resourceResults = [];
                        if (resourceSearchResult.data.entry) {
                            resourceSearchResult.data.entry.forEach(function(entry){
                                entry.resource.fullUrl = entry.fullUrl;
                                resourceResults.push(entry.resource);
                            });
                        }
                        deferred.resolve(resourceResults, resourceSearchResult);
                    });
                return deferred;
            },
            create: function(newPatient){
                var req =fhirClient.authenticated({
                    url: fhirClient.server.serviceUrl + '/Patient',
                    type: 'POST',
                    contentType: "application/json",
                    data: JSON.stringify(newPatient)
                });

                $.ajax(req)
                    .done(function(){
                        console.log("Patient created!", arguments);
//                        $route.reload();
                    })
                    .fail(function(){
                        console.log("Failed to create patient", arguments);
                    });

                return true;
            },
            registerContext: function(app, params){
                var deferred = $.Deferred();

                var req = fhirClient.authenticated({
                    url: fhirClient.server.serviceUrl + '/_services/smart/Launch',
                    type: 'POST',
                    contentType: "application/json",
                    data: JSON.stringify({
                        client_id: app.client_id,
                        parameters:  params
                    })
                });

                $.ajax(req)
                    .done(deferred.resolve)
                    .fail(deferred.reject);

                return deferred;
            }
        }
    }).factory('launchScenarios', function($rootScope, $location, $filter, appsSettings, userServices, notification) {

        var scenarioBuilder = {
            owner: userServices.getOAuthUser(),
            description: '',
            persona: '',
            patient: '',
            app: ''
        };

        var selectedScenario;
        var recentLaunchScenarioList = [];
        var fullLaunchScenarioList = [];

        function orderByLastLaunch() {
            if(fullLaunchScenarioList){
                fullLaunchScenarioList = $filter('orderBy')(fullLaunchScenarioList, "lastLaunchSeconds", true);
                recentLaunchScenarioList = [];
                for (var i=0; i < fullLaunchScenarioList.length && i < 3; i++) {
                    recentLaunchScenarioList.push(fullLaunchScenarioList[i]);
                }
            }
        }

        return {
            clearBuilder: function() {
                scenarioBuilder = {
                    description: '',
                    persona: '',
                    patient: '',
                    app: ''
                };
            },
            getBuilder: function() {
                return scenarioBuilder;
            },
            setDescription: function(desc) {
                scenarioBuilder.description = desc;
            },
            setPersona: function(persona) {
                scenarioBuilder.persona = persona;
            },
            setPatient: function(patient) {
                scenarioBuilder.patient = patient;
            },
            setApp: function(app) {
                scenarioBuilder.app = app;
            },
            setSelectedScenario: function(scenario) {
                selectedScenario = scenario;
            },
            getSelectedScenario: function() {
                return selectedScenario;
            },
            getFullLaunchScenarioList: function() {
                return fullLaunchScenarioList;
            },
            addFullLaunchScenarioList: function(launchScenario) {
                this.addLaunchScenario(angular.copy(launchScenario));
                this.clearBuilder();
            },
            getRecentLaunchScenarioList: function() {
                return recentLaunchScenarioList;
            },
            addLaunchScenario: function(launchScenario){
                var that = this;
                appsSettings.getSettings().then(function(settings){
                    $.ajax({
                        url: settings.baseUrl + "/launchScenario",
                        type: 'POST',
                        data: JSON.stringify(launchScenario),
                        contentType: "application/json"
                    }).done(function(result){
                            that.getLaunchScenarios();
                            notification.message("Launch Scenario Created");
                        }).fail(function(){
                        notification.message({ type:"error", text: "Failed to Create Launch Scenario" });
                    });
                });
            },
            updateLaunchScenario: function(launchScenario){
                var that = this;
                appsSettings.getSettings().then(function(settings){
                    $.ajax({
                        url: settings.baseUrl + "/launchScenario",
                        type: 'PUT',
                        data: JSON.stringify(launchScenario),
                        contentType: "application/json"
                    }).done(function(result){
                            that.getLaunchScenarios();
                        }).fail(function(){
                        });
                });
            },
            deleteLaunchScenario: function(launchScenario){
                var that = this;
                appsSettings.getSettings().then(function(settings){
                    $.ajax({
                        url: settings.baseUrl + "/launchScenario",
                        type: 'DELETE',
                        data: JSON.stringify(launchScenario),
                        contentType: "application/json"
                    }).done(function(result){
                            notification.message("Launch Scenario Deleted");
                            that.getLaunchScenarios();
                        }).fail(function(){
                        });
                });
            },
            getLaunchScenarios: function() {
                var deferred = $.Deferred();
                appsSettings.getSettings().then(function(settings){
                    $.ajax({
                        url: settings.baseUrl + "/launchScenarios/" + userServices.oauthUser().ldapId,
                        type: 'GET',
                        contentType: "application/json"
                    }).done(function(launchScenarioList){
                            fullLaunchScenarioList = [];
                            if (launchScenarioList) {
                                launchScenarioList.forEach(function(launchScenario){
                                    fullLaunchScenarioList.push(launchScenario);
                                });
                                orderByLastLaunch();
                                $rootScope.$emit('launch-scenario-list-update');
                            }
                            deferred.resolve();
                        }).fail(function(){
                        });
                });
                return deferred;
            }

        }

    }).factory('userServices', function($rootScope, fhirApiServices, patientDetails, appsSettings) {
        var persona = {};
        var oauthUser = {};

        return {
            persona: function(){
                return persona;
            },
            oauthUser: function(){
                return oauthUser;
            },
            updateProfile: function(selectedUser){
                appsSettings.getSettings().then(function(settings){

                    $.ajax({
                        url: settings.profile_update_uri,
                        type: 'POST',
                        data: JSON.stringify({
                            user_id: oauthUser.ldapId,
                            profile_url: selectedUser.fullUrl
                        }),
                        contentType: "application/json"
                    }).done(function(result){
                            $rootScope.$digest();
                        }).fail(function(){
                        });
                });
            },
            getFhirProfileUser: function() {
                var deferred = $.Deferred();
                if (fhirApiServices.fhirClient().userId === null ||
                    typeof fhirApiServices.fhirClient().userId === "undefined"){
                    deferred.resolve(null);
                    return deferred;
                }
                var historyIndex = fhirApiServices.fhirClient().userId.lastIndexOf("/_history");
                var userUrl = fhirApiServices.fhirClient().userId;
                if (historyIndex > -1 ){
                    userUrl = fhirApiServices.fhirClient().userId.substring(0, historyIndex);
                }
                var userIdSections = userUrl.split("/");

                $.when(fhirApiServices.fhirClient().api.read({type: userIdSections[userIdSections.length-2], id: userIdSections[userIdSections.length-1]}))
                    .done(function(userResult){

                        var user = {name:""};
                        user.name = patientDetails.name(userResult.data);
                        user.id  = patientDetails.id(userResult.data);
                        persona = user;
                        persona.fullUrl = userResult.config.url;
                        deferred.resolve(user);
                    });
                return deferred;
            },
            getOAuthUser: function() {
                if (fhirApiServices.fhirClient().tokenIdPayload === null ||
                    typeof fhirApiServices.fhirClient().tokenIdPayload === "undefined"){
                    oauthUser = null;
                } else {
                    var payload = fhirApiServices.fhirClient().tokenIdPayload;
                    oauthUser.ldapId = payload.sub;
                    oauthUser.name = payload.displayName;
                    oauthUser.email = payload.email;
                }
                return oauthUser;
            }
        };
    }).factory('patientDetails', function() {
        return {
            id: function(p){
                return p.id;
            },
            name: function(p){
                if (p.resourceType === "Patient") {
                    var patientName = p && p.name && p.name[0];
                    if (!patientName) return null;

                    return patientName.given.join(" ") + " " + patientName.family.join(" ");
                } else {
                    var practitionerName = p && p.name;
                    if (!practitionerName) return null;

                    var practitioner =  practitionerName.given.join(" ") + " " + practitionerName.family.join(" ");
                    if (practitionerName.suffix) {
                        practitioner = practitioner + " " + practitionerName.suffix.join(", ");
                    }
                    return practitioner;
                }
            }
        };
    }).factory('customFhirApp', function() {

        var app = localStorage.customFhirApp ?
            JSON.parse(localStorage.customFhirApp) : {id: "", url: ""};

        return {
            get: function(){return app;},
            set: function(app){
                localStorage.customFhirApp = JSON.stringify(app);
            }
        };

    }).factory('launchApp', function($rootScope, fhirApiServices, apps, random) {

        return {
            /* Hack to get around the window popup behavior in modern web browsers
             (The window.open needs to be synchronous with the click even to
             avoid triggering  popup blockers. */

            launch: function(app, patientContext) {
                var key = random(32);
                window.localStorage[key] = "requested-launch";
                var appWindow = window.open('launch.html?'+key, '_blank');

                var params = {};
                if (patientContext !== undefined) {
                    params = {patient: patientContext.fhirId}
                }

                fhirApiServices
                    .registerContext(app, params)
                    .done(function(c){
                        console.log(fhirApiServices.fhirClient());
                        window.localStorage[key] = JSON.stringify({
                            app: app,
                            iss: fhirApiServices.fhirClient().server.serviceUrl,
                            context: c
                        });
                    }).fail( function(err){
                        console.log("Could not register launch context: ", err);
                        appWindow.close();
                        //                    $rootScope.$emit('reconnect-request');
                        $rootScope.$emit('error', 'Could not register launch context (see console)');
                        $rootScope.$digest();
                    });
            },
            launchPatientDataManager: function(patient){
                var that = this;
                apps.getPractitionerPatientApps.success(function(patientApps){
                    var pdm;
                    for (var i=0; i < patientApps.length; i++) {
                        if (patientApps[i]["client_id"] == "patient_data_manager") {
                            pdm = patientApps[i];
                        }
                    }
                    if (patient.fhirId === undefined){
                        patient.fhirId = patient.id;
                    }
                    that.launch(pdm, patient);
                });
            }
    }

    }).factory('random', function() {
        var chars = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
        return function randomString(length) {
            var result = '';
            for (var i = length; i > 0; --i) {
                result += chars[Math.round(Math.random() * (chars.length - 1))];
            }
            return result;
        }
    }).factory('notification', function($rootScope) {
        var messages = [];

        return {
            message: function(message ){
                messages = messages.filter(function( obj ) {
                    return (obj.isVisible !== false );
                });

                var finalMessage;
                if (message.text === undefined) {
                    finalMessage = {
                        type: 'message',
                        text: message
                    }
                } else {
                    finalMessage = message;
                }
                messages.push(finalMessage);
                $rootScope.$emit('message-notify', messages);
            },
            messages: function(){
                return messages;
            }
        }

    }).factory('apps', ['$http',function($http)  {
        return {
            getPatientApps: $http.get('static/js/config/patient-apps.json'),
            getPractitionerPatientApps: $http.get('static/js/config/practitioner-patient-apps.json'),
            getPractitionerApps: $http.get('static/js/config/practitioner-apps.json')
        };

    }]).factory('appsSettings', ['$http',function($http)  {

    var settings;

    return {
        loadSettings: function(){
            var deferred = $.Deferred();
            $http.get('static/js/config/sandbox-manager.json').success(function(result){
                    settings = result;
                    deferred.resolve(result);
                });
            return deferred;
        },
        getSettings: function(){
            var deferred = $.Deferred();
            if (settings !== undefined) {
                deferred.resolve(settings);
            } else {
                this.loadSettings().then(function(result){
                    deferred.resolve(result);
                });
            }
            return deferred;
        }
    };

}]);
