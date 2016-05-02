'use strict';

angular.module('sandManApp.services', [])
    .factory('oauth2', function($rootScope, $location, appsSettings) {

        var authorizing = false;

        return {
            authorizing: function(){
                return authorizing;
            },
            authorize: function(s, sandboxId){
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

                var serviceUrl = s.defaultServiceUrl;
                if (sandboxId !== undefined && sandboxId !== "") {
                    serviceUrl = s.baseServiceUrl + sandboxId + "/data";
                }
                FHIR.oauth2.authorize({
                    client: client,
                    server: serviceUrl,
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
            login: function(sandboxId){
                var that = this;
                appsSettings.getSettings().then(function(settings){
                    that.authorize(settings, sandboxId);
                });
            }
        };

    }).factory('fhirApiServices', function (oauth2, appsSettings, notification, $rootScope, $location) {

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
                    oauth2.login();
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
            queryResourceInstances: function(resource, searchValue, tokens, sort, count) {
                var deferred = $.Deferred();

                if (count === undefined) {
                    count = 50;
                }

                var searchParams = {type: resource, count: count};
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
                    }).fail(function(error){
                    var test = error;
                    });
                return deferred;
            },
            readResourceInstance: function(resource, id) {
                var deferred = $.Deferred();

                $.when(fhirClient.api.read({type: resource, id: id}))
                    .done(function(resourceResult){
                        var resource;
                        resource = esourceResult.data.entry;
                        resource = entry.fullUrl;
                        deferred.resolve(resource);
                    }).fail(function(error){
                        var test = error;
                    });
                return deferred;
            },
            createResourceInstance: function(resource){
                var req =fhirClient.authenticated({
                    url: fhirClient.server.serviceUrl + '/' + resource.resourceType,
                    type: 'POST',
                    contentType: "application/json",
                    data: JSON.stringify(resource)
                });

                $.ajax(req)
                    .done(function(){
                        console.log(resource.resourceType + " created!", arguments);
                        notification.message(resource.resourceType + " Created");
                    })
                    .fail(function(){
                        console.log("Failed to create " + resource.resourceType, arguments);
                        notification.message({ type:"error", text: "Failed to Create " + resource.resourceType });
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
    }).factory('sandboxManagement', function($rootScope, $location, $filter, fhirApiServices,
                                           errorService, appsSettings, apps, userServices, notification) {

        var scenarioBuilder = {
            createdBy: '',
            description: '',
            persona: '',
            patient: '',
            app: ''
        };

        var sandbox = {
            sandboxId: '',
            name: '',
            description: '',
            createdBy: '',
            users: []
        };

        var selectedScenario;
        var recentLaunchScenarioList = [];
        var fullLaunchScenarioList = [];
        var sandboxIdFromURL;
        var hasSandbox = false;
        var creatingSandbox = false;

        function orderByLastLaunch() {
            if(fullLaunchScenarioList){
                fullLaunchScenarioList = $filter('orderBy')(fullLaunchScenarioList, "lastLaunchSeconds", true);
                recentLaunchScenarioList = [];
                for (var i=0; i < fullLaunchScenarioList.length && i < 3; i++) {
                    recentLaunchScenarioList.push(fullLaunchScenarioList[i]);
                }
            }
        }

        function prepLaunchScenario(launchScenario) {
            var newLaunchScenario = {
                createdBy: launchScenario.owner,
                description: launchScenario.description,
                patient: launchScenario.patient,
                app: launchScenario.app
            };
            if (launchScenario.persona !== undefined && launchScenario.persona !== '') {
                newLaunchScenario.persona = launchScenario.persona;
            }
            return newLaunchScenario;
        }

        return {
            getSandboxIdFromUrl: function() {

                sandboxIdFromURL = window.location.pathname;
                var trailingSlash = sandboxIdFromURL.lastIndexOf("/");
                if (trailingSlash === sandboxIdFromURL.length-1) {
                    sandboxIdFromURL = sandboxIdFromURL.substring(0, sandboxIdFromURL.length-1);
                }

                var slash = sandboxIdFromURL.lastIndexOf("/");
                if (slash !== 0) {
                    sandboxIdFromURL = sandboxIdFromURL.substring(slash+1, sandboxIdFromURL.length);
                } else {
                    sandboxIdFromURL = undefined;
                }
            },
            getSandbox: function() {
                return sandbox;
            },
            hasSandbox: function() {
                return hasSandbox;
            },
            setHasSandbox: function(exists) {
                hasSandbox = exists;
            },
            creatingSandbox: function() {
                return creatingSandbox;
            },
            setCreatingSandbox: function(creating) {
                creatingSandbox = creating;
            },
            clearSandbox: function() {
                sandbox = {
                    id: '',
                    sandboxId: '',
                    name: '',
                    description: '',
                    createdBy: '',
                    users: []
                };
            },
            clearScenarioBuilder: function() {
                scenarioBuilder = {
                    createdBy: userServices.getOAuthUser(),
                    description: '',
                    persona: '',
                    patient: '',
                    app: ''
                };
            },
            getScenarioBuilder: function() {
                return scenarioBuilder;
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
                this.clearScenarioBuilder();
            },
            getRecentLaunchScenarioList: function() {
                return recentLaunchScenarioList;
            },
            addLaunchScenario: function(launchScenario, showNotification){
                var that = this;
                appsSettings.getSettings().then(function(settings){
                    launchScenario = prepLaunchScenario(launchScenario);
                    if (sandbox.sandboxId !== '') {
                        launchScenario.sandbox = sandbox;
                    }
                    $.ajax({
                        url: settings.baseUrl + "/launchScenario",
                        type: 'POST',
                        data: JSON.stringify(launchScenario),
                        contentType: "application/json",
                        beforeSend : function( xhr ) {
                            xhr.setRequestHeader( 'Authorization', 'BEARER ' + fhirApiServices.fhirClient().server.auth.token );
                        }
                    }).done(function(result){
                            that.getSandboxLaunchScenarios();
                            if (showNotification) {
                                notification.message("Launch Scenario Created");
                            }
                        }).fail(function(){
                            if (showNotification) {
                                notification.message({ type:"error", text: "Failed to Create Launch Scenario" });
                            }
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
                        contentType: "application/json",
                        beforeSend : function( xhr ) {
                            xhr.setRequestHeader( 'Authorization', 'BEARER ' + fhirApiServices.fhirClient().server.auth.token );
                        }
                    }).done(function(result){
                            that.getSandboxLaunchScenarios();
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
                            that.getSandboxLaunchScenarios();
                        }).fail(function(){
                        });
                });
            },
            getSandboxLaunchScenarios: function() {
                var deferred = $.Deferred();
                appsSettings.getSettings().then(function(settings){
                    $.ajax({
                        url: settings.baseUrl + "/launchScenario?userId=" + encodeURIComponent(userServices.getOAuthUser().ldapId) +
                        "&sandboxId=" + sandbox.sandboxId,
                        type: 'GET',
                        contentType: "application/json",
                        beforeSend : function( xhr ) {
                            xhr.setRequestHeader( 'Authorization', 'BEARER ' + fhirApiServices.fhirClient().server.auth.token );
                        }
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
            },
            createSandbox: function(newSandbox) {
                var that = this;
                var deferred = $.Deferred();
                appsSettings.getSettings().then(function(settings){

                    var createSandbox = {
                        createdBy: userServices.getOAuthUser(),
                        name: newSandbox.sandboxName,
                        sandboxId: newSandbox.sandboxId,
                        description: newSandbox.description,
                        users: [userServices.getOAuthUser()]
                    };

                    $.ajax({
                        url: settings.baseUrl + "/sandbox",
                        type: 'POST',
                        data: JSON.stringify(createSandbox),
                        contentType: "application/json",
                        beforeSend : function( xhr ) {
                            xhr.setRequestHeader( 'Authorization', 'BEARER ' + fhirApiServices.fhirClient().server.auth.token );
                        }
                    }).done(function(sandboxResult){
                            sandbox = {
                                id: sandboxResult.id,
                                createdBy: sandboxResult.createdBy,
                                name: sandboxResult.name,
                                sandboxId: sandboxResult.sandboxId,
                                description: sandboxResult.description
                            };
                            deferred.resolve(sandbox);
                        }).fail(function(error){
                            errorService.setErrorMessage(error.responseText);
                            that.clearSandbox();
                            deferred.reject();
                        });
                });
                return deferred;
            },
            getUserSandbox: function() {
                var that = this;
                var deferred = $.Deferred();
                appsSettings.getSettings().then(function(settings){
                    $.ajax({
                        url: settings.baseUrl + "/sandbox?userId=" + encodeURIComponent(userServices.getOAuthUser().ldapId),
                        type: 'GET',
                        contentType: "application/json",
                        beforeSend : function( xhr ) {
                            xhr.setRequestHeader( 'Authorization', 'BEARER ' + fhirApiServices.fhirClient().server.auth.token );
                        }
                    }).done(function(sandboxResult){
                            if (sandboxResult.length > 0) {
                                if (settings.reservedEndpoints.indexOf(sandboxIdFromURL) > -1) {
                                    deferred.resolve("reserved");
                                } else if (sandboxIdFromURL !== undefined && sandboxIdFromURL !== sandboxResult[0].sandboxId) {
                                    deferred.resolve('invalid');
                                } else {
                                    sandbox = {
                                        id: sandboxResult[0].id,
                                        createdBy: sandboxResult[0].createdBy,
                                        name: sandboxResult[0].name,
                                        sandboxId: sandboxResult[0].sandboxId,
                                        description: sandboxResult[0].description
                                    };
                                    that.getSandboxLaunchScenarios();
                                    deferred.resolve(true);
                                }
                            } else {
                                that.clearSandbox();
                                deferred.resolve(false);
                            }
                        }).fail(function(){
                            that.clearSandbox();
                            deferred.resolve(false);
                        });
                });
                return deferred;
            },
            getSandboxById: function(sandboxId) {
                var deferred = $.Deferred();
                appsSettings.getSettings().then(function(settings){
                        if (settings.reservedEndpoints.indexOf(sandboxId) > -1) {
                            deferred.resolve("reserved");
                        } else {
                            $.ajax({
                                url: settings.baseUrl + "/sandbox?id=" + sandboxId,
                                type: 'GET',
                                contentType: "application/json",
                                beforeSend : function( xhr ) {
                                    xhr.setRequestHeader( 'Authorization', 'BEARER ' + fhirApiServices.fhirClient().server.auth.token );
                                }
                            }).done(function(sandbox){
                                deferred.resolve(sandbox);
                                $rootScope.$digest();

                                }).fail(function(){
                                    deferred.resolve();
                                    $rootScope.$digest();
                                });
                        }
                });
                return deferred;
            },
            createDefaultLaunchScenarios: function() {
                var that = this;
                apps.getGalleryApps().then(function(galleryApps){
                    appsSettings.getSettings().then(function(settings){
                        var defaultPersona;
                        fhirApiServices.readResourceInstance('Practitioner', settings.defaultPersonaId)
                            .then(function(resource){
                                defaultPersona = {
                                    fhirId: "",
                                    fullUrl: "",
                                    name: "",
                                    resource: "Practitioner"
                                };
                                settings.defaultLaunchScenarios.forEach(function(client_id) {
                                    galleryApps.forEach(function(app) {
                                        var newLaunchScenario = {};
                                        if (app.client_id === client_id) {
                                            newLaunchScenario.createdBy = userServices.getOAuthUser();
                                            newLaunchScenario.description = app.client_name;
                                            newLaunchScenario.app = app;
                                            newLaunchScenario.patient = {"fhirId": app.patient};
                                            newLaunchScenario.persona = defaultPersona;
                                            that.addLaunchScenario(newLaunchScenario, false);
                                        }
                                    });
                                });
                            });
                    });
                });
            }

        }

    }).factory('userServices', function($rootScope, fhirApiServices, $filter, appsSettings) {
        var oauthUser;

        return {
            updateProfile: function(selectedUser){
                var deferred = $.Deferred();
                var that = this;
                appsSettings.getSettings().then(function(settings){

                    $.ajax({
                        url: settings.profile_update_uri,
                        type: 'POST',
                        data: JSON.stringify({
                            user_id: that.getOAuthUser().ldapId,
                            profile_url: selectedUser.fullUrl
                        }),
                        contentType: "application/json"
                    }).done(function(result){
                            deferred.resolve();
                            $rootScope.$digest();
                        }).fail(function(){
                            deferred.reject();
                        });
                });
                return deferred;
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
                        user.name = $filter('nameGivenFamily')(userResult.data);
                        user.id  = userResult.data.id;
                        deferred.resolve(user);
                    });
                return deferred;
            },
//            getOAuthUser: function() {
//                if (fhirApiServices.fhirClient().tokenIdPayload === null ||
//                    typeof fhirApiServices.fhirClient().tokenIdPayload === "undefined"){
//                    oauthUser = null;
//                } else {
//                    var payload = fhirApiServices.fhirClient().tokenIdPayload;
//                    oauthUser.ldapId = payload.sub;
//                    oauthUser.name = payload.displayName;
//                    oauthUser.email = payload.email;
//                }
//                return oauthUser;
//            },
            getOAuthUser: function() {
                return oauthUser;
            },
            getOAuthUserFromServer: function() {
                var deferred = $.Deferred();
                if (oauthUser !== undefined) {
                    deferred.resolve(oauthUser);
                } else {
                    var userInfoEndpoint = fhirApiServices.fhirClient().state.provider.oauth2.authorize_uri.replace("authorize", "userinfo");
                    $.ajax({
                        url: userInfoEndpoint,
                        type: 'GET',
                        contentType: "application/json",
                        beforeSend : function( xhr ) {
                            xhr.setRequestHeader( 'Authorization', 'BEARER ' + fhirApiServices.fhirClient().server.auth.token );
                        }
                    }).done(function(result){
                            oauthUser = {
                                ldapId: result.sub,
                                name: result.name
                            };
                            deferred.resolve(oauthUser);
                        }).fail(function(){
                        });
                }
                return deferred;
            },
            userSettings: function() {
                var that = this;
                appsSettings.getSettings().then(function(settings){
                    $.ajax({
                        url: settings.userManagementUrl,
                        type: 'GET',
                        beforeSend : function( xhr ) {
                            xhr.setRequestHeader( 'c8381465-a7f8-4ecc-958d-ec296d6e8671', that.getOAuthUser().ldapId);
                            xhr.setRequestHeader("Access-Control-Allow-Origin", "*");
                        }

                    }).done(function(data){
                            window.location.href = settings.userManagementUrl + "/private/"
                        }).fail(function(){
                        });
                });
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

    }).factory('launchApp', function($rootScope, fhirApiServices, apps, random, userServices) {

        var patientDataManagerApp;

        getPatientDataManagerApp();

        function registerContext(app, params, key) {
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
        }

        function getPatientDataManagerApp() {
            apps.getPractitionerPatientApps().done(function(patientApps){
                var pdm;
                for (var i=0; i < patientApps.length; i++) {
                    if (patientApps[i]["client_id"] == "patient_data_manager") {
                        pdm = patientApps[i];
                    }
                }
                patientDataManagerApp = pdm;
            });
        }

        return {
            /* Hack to get around the window popup behavior in modern web browsers
             (The window.open needs to be synchronous with the click event to
             avoid triggering  popup blockers. */

            launch: function(app, patientContext, contextParams, persona) {
                var key = random(32);
                window.localStorage[key] = "requested-launch";
                var appWindow = window.open('launch.html?'+key, '_blank');

                var params = {};
                if (patientContext !== undefined && patientContext !== "") {
                    params = {patient: patientContext.fhirId}
                }

                if (contextParams !== undefined) {
                    for (var i=0; i < contextParams.length; i++) {
                        params[contextParams[i].name] = contextParams[i].value;
                    }
                }

                if (persona !== undefined && persona !== "" ) {
                    userServices.updateProfile(persona).then(function(){
                        registerContext(app, params, key);
                    });
                } else {
                    registerContext(app, params, key);
                }
            },
            launchPatientDataManager: function(patient){
                if (patient.fhirId === undefined){
                    patient.fhirId = patient.id;
                }
                this.launch(patientDataManagerApp, patient);
            }
    }

    }).factory('descriptionBuilder', function() {
        return  {
            launchScenarioDescription: function(scenario){

                var desc = {title: "", detail: ""};

                if (scenario.persona.resource === 'Practitioner') {
                    desc.title = "Launch App as a Practitioner";
                    if (scenario.patient !== "") {
                        desc.title = desc.title + " with Patient Context";
                    } else {
                        desc.title = desc.title + " with NO Patient Context";
                    }
                } else {
                    desc.title = "Launch an App As a Patient";
                }

                return desc;
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
    }).factory('errorService', function() {
        var errorMessage;
        return {
            getErrorMessage: function () {
                return errorMessage;
            },
            setErrorMessage: function (error) {
                errorMessage = error;
            }
    };
    }).factory('tools', function() {

        return {
            decodeURLParam: function (url, param) {
                var query;
                var data;
                var result = [];

                try {
                    query = decodeURIComponent(url).split("?")[1];
                    data = query.split("&");
                } catch (err) {
                    return null;
                }

                for(var i=0; i<data.length; i++) {
                    var item = data[i].split("=");
                    if (item[0] === param) {
                        result.push(item[1]);
                    }
                }

                if (result.length === 0){
                    return null;
                }
                return result[0];
            }
        };

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

    var patientApps;
    var practitionerPatientApps;
    var practitionerApps;
    var galleryApps;
    var galleryAppDetails;

    return {
        getPatientApps : function() {
            var deferred = $.Deferred();
            if (patientApps !== undefined) {
                deferred.resolve(patientApps);
            } else {
                this.loadSettings().then(function(){
                    deferred.resolve(patientApps);
                });
            }
            return deferred;
        },
        getPractitionerPatientApps : function() {
            var deferred = $.Deferred();
            if (practitionerPatientApps !== undefined) {
                deferred.resolve(practitionerPatientApps);
            } else {
                this.loadSettings().then(function(){
                    deferred.resolve(practitionerPatientApps);
                });
            }
            return deferred;
        },
        getPractitionerApps : function() {
            var deferred = $.Deferred();
            if (practitionerApps !== undefined) {
                deferred.resolve(practitionerApps);
            } else {
                this.loadSettings().then(function(){
                    deferred.resolve(practitionerApps);
                });
            }
            return deferred;
        },
        getGalleryApps : function() {
            var deferred = $.Deferred();
            if (galleryApps !== undefined) {
                deferred.resolve(galleryApps);
            } else {
                this.loadSettings().then(function(){
                    deferred.resolve(galleryApps);
                });
            }
            return deferred;
        },
        loadSettings: function(){
            var deferred = $.Deferred();
            $http.get('static/js/config/patient-apps.json').success(function(result){
                patientApps = result;
                $http.get('static/js/config/practitioner-patient-apps.json').success(function(result){
                    practitionerPatientApps = result;
                    $http.get('static/js/config/practitioner-apps.json').success(function(result){
                        practitionerApps = result;
                        $http.get('static/js/config/gallery-app-details.json').success(function(result){
                            galleryAppDetails = result;
                            $http.get('static/js/config/gallery-apps.json').success(function(result){
                                galleryApps = result;
                                angular.forEach(galleryApps, function (app) {
                                    angular.forEach(galleryAppDetails, function (appDetails) {
                                        if (app.client_id === appDetails.client_id) {
                                            app.info = appDetails.info;
                                            app.company_url = appDetails.company_url;
                                            app.author = appDetails.author;
                                        }
                                    });
                                });
                                deferred.resolve();
                            });
                        });
                    });
                });
            });
            return deferred;
            }
        };

    }]).factory('patientResources', ['$http',function($http)  {
    var resources;

    return {
        loadSettings: function(){
            var deferred = $.Deferred();
            $http.get('static/js/config/supported-patient-resources.json').success(function(result){
                resources = result;
                deferred.resolve(result);
            });
            return deferred;
        },
        getSupportedResources: function(){
            var deferred = $.Deferred();
            if (resources !== undefined) {
                deferred.resolve(resources);
            } else {
                this.loadSettings().then(function(result){
                    deferred.resolve(result);
                });
            }
            return deferred;
        }
    };

}]).factory('appsSettings', ['$http',function($http)  {

    var settings;

    return {
        loadSettings: function(){
            var deferred = $.Deferred();
            $http.get('static/js/config/sandbox-manager.json').success(function(result){
                settings = result;
                settings.baseUrl = window.location.href.split("#")[0].substring(0, window.location.href.split("#")[0].length-1);
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
