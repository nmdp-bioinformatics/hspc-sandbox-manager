'use strict';

angular.module('sandManApp.services', [])
    .factory('oauth2', function($rootScope, $location, appsSettings, tools) {

        var authorizing = false;

        return {
            authorizing: function(){
                return authorizing;
            },
            authorize: function(s, sandboxId, sandboxVersion){
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
                    serviceUrl = s.baseServiceUrl_1 + sandboxId + "/data";
                    if (sandboxVersion !== undefined && sandboxVersion !== "" && sandboxVersion === "2") {
                        serviceUrl = s.baseServiceUrl_2 + sandboxId + "/data";
                    }
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
                appsSettings.getSettings().then(function(settings) {
                    window.location.href = settings.oauthLogoutUrl
                        + "?hspcRedirectUrl="
                        + encodeURI(appsSettings.getSandboxUrlSettings().sandboxManagerRootUrl);
                });
            },
            login: function(sandboxId){
                
                var that = this;
                appsSettings.getSettings().then(function(settings){
                    tools.validateSandboxIdFromUrl().then(function (resultSandboxId, schemaVersion) {
                        that.authorize(settings, resultSandboxId, schemaVersion);
                    }, function () {
                        that.authorize(settings);
                    });
                });
            }
        };

    }).factory('fhirApiServices', function ($q, oauth2, notification, appsSettings, $rootScope, $location, exportResources) {

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
                        // if (newSmart.state && newSmart.state.from !== undefined){
                        //     $location.url(newSmart.state.from);
                        // }
                        sessionStorage.setItem("hspcAuthorized", true);
                        fhirClient = newSmart;
                        $rootScope.$emit('signed-in');
                        $rootScope.$digest();
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
            hasPrev: function(lastSearch) {
                var hasLink = false;
                if (lastSearch  === undefined) {
                    return false;
                } else {
                    lastSearch.data.link.forEach(function(link) {
                        if (link.relation == "previous") {
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
                if (typeof searchValue !== 'undefined' && searchValue !== "") {
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
            //NOTE: This is FHIR implementation specific.
            // Next, Prev and Self link impls are not defined in the FHIR spec
            calculateResultSet: function(lastSearch) {
                var count = {start: 0, end: 0, total: 0};
                count.total = lastSearch.data.total;
                var pageSize;
                var hasNext = this.hasNext(lastSearch);

                if (this.hasNext(lastSearch)) {
                    lastSearch.data.link.forEach(function (link) {
                        if (link.relation == "next") {
                            var querySting = decodeURIComponent(link.url).split("?");
                            var paramPairs = querySting[1].split("&");
                            for (var i = 0; i < paramPairs.length; i++) {
                                var parts = paramPairs[i].split('=');
                                if (parts[0] === "_count") {
                                    pageSize = Number(parts[1]);
                                }
                            }
                        }
                    });
                    lastSearch.data.link.forEach(function(link) {
                        if (link.relation == "next") {
                            var querySting = decodeURIComponent(link.url).split("?");
                            var paramPairs = querySting[1].split("&");
                            for (var i = 0; i < paramPairs.length; i++) {
                                var parts = paramPairs[i].split('=');
                                if (parts[0] === "_getpagesoffset") {
                                    if (Number(parts[1]) === pageSize) {
                                        count.start = 1;
                                    } else {
                                        count.start = Number(parts[1]) - pageSize + 1;
                                    }
                                    if ((Number(parts[1]) + pageSize) != count.total) {
                                        count.end = Number(parts[1]);
                                    } else {
                                        count.end = count.total;
                                    }
                                }
                            }
                        }
                    });
                } else {
                    lastSearch.data.link.forEach(function (link) {
                        if (link.relation == "self") {
                            var querySting = decodeURIComponent(link.url).split("?");
                            var paramPairs = querySting[1].split("&");
                            for (var i = 0; i < paramPairs.length; i++) {
                                var parts = paramPairs[i].split('=');
                                if (parts[0] === "_count") {
                                    pageSize = Number(parts[1]);
                                }
                            }
                        }
                    });
                    lastSearch.data.link.forEach(function(link) {
                        if (link.relation == "self") {
                            var querySting = decodeURIComponent(link.url).split("?");
                            var paramPairs = querySting[1].split("&");
                            for (var i = 0; i < paramPairs.length; i++) {
                                var parts = paramPairs[i].split('=');
                                if (parts[0] === "_getpagesoffset") {
                                    if (Number(parts[1]) === 0) {
                                        count.start = 1;
                                    } else {
                                        count.start = Number(parts[1]) + 1;
                                    }
                                    if ((Number(parts[1]) + pageSize) < count.total) {
                                        count.end = Number(parts[1]) + pageSize;
                                    } else {
                                        count.end = count.total;
                                    }
                                }
                            }
                        }
                    });
                }

                return count;
            },
            runRawQuery: function(query) {
                var deferred = $.Deferred();
                var that = this;

                $.ajax({
                    url: that.fhirClient().server.serviceUrl + "/" + query,
                    type: 'GET',
                    beforeSend : function( xhr ) {
                        xhr.setRequestHeader( 'Authorization', 'BEARER ' + that.fhirClient().server.auth.token );
                    }
                }).done(function(results){
                    deferred.resolve(results);
                }).fail(function(){
                    deferred.reject();
                });
                return deferred;
            },
            getAllPages: function(lastSearch) {
                var that = this;
                var deferred = $.Deferred();
                var resourceResults = [];

                $.when(fhirClient.api.nextPage({bundle: lastSearch.data}))
                    .done(function (pageResult) {
                        var resourceResults = [];
                        if (pageResult.data.entry) {
                            pageResult.data.entry.forEach(function (entry) {
                                resourceResults.push(entry.resource);
                            });
                            if (pageResult.data.entry.length === 50) {
                                that.getAllPages(pageResult).then(function(resourceList){
                                    resourceList.forEach(function(resource){
                                        resourceResults.push(resource);
                                    });
                                    deferred.resolve(resourceResults);
                                });
                            } else {
                                deferred.resolve(resourceResults);
                            }
                        }
                    });

                return deferred;
            },
            queryAllResourcePages: function(resource) {
                var that = this;
                var deferred = $.Deferred();

                $.when(fhirClient.api.search({type: resource, count: 50}))
                    .done(function(resourceSearchResult){
                        var resourceResults = [];
                        if (resourceSearchResult.data.entry) {
                            resourceSearchResult.data.entry.forEach(function(entry){
                                resourceResults.push(entry.resource);
                            });
                        }

                        if (resourceSearchResult.data.entry !== undefined && resourceSearchResult.data.total > resourceSearchResult.data.entry.length) {

                            that.getAllPages(resourceSearchResult).then(function (resourceList) {
                                resourceList.forEach(function (resource) {
                                    resourceResults.push(resource);
                                });
                                deferred.resolve(resourceResults);
                            });
                        }  else {
                            deferred.resolve(resourceResults);
                        }

                    }).fail(function(error){
                    deferred.reject();
                    var test = error;
                });
                return deferred;
            },
            readResourceInstance: function(resource, id) {
                var deferred = $.Deferred();

                $.when(fhirClient.api.read({type: resource, id: id}))
                    .done(function(resourceResult){
                        var resource;
                        resource = resourceResult.data.entry;
                        resource = resourceResult.data.entry.fullUrl;
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
            importBundle: function(bundle) {
                var deferred = $.Deferred();

                $.when(fhirClient.api.transaction({data: angular.copy(bundle)}))
                    .done(function(results){
                        // notification.message("Bundle Uploaded");
                        deferred.resolve(results.data);
                    }).fail(function(error){
                        deferred.reject(error.data.responseText);
                    });
                return deferred;
           },
            exportAllData: function (){
                var that = this;
                var deferred = $.Deferred();
                var transactionBundle = {
                    resourceType:"Bundle",
                    type : "transaction",
                    entry:[]
                };

                var promises = [];
                exportResources.getExportResources().done(function(resources){
                    angular.forEach(resources, function (resourceType) {
                        promises.push(that.queryAllResourcePages(resourceType));
                    });
                    $q.all(promises).then(function(resourceTypeList){
                        angular.forEach(resourceTypeList, function (resourceList) {
                            angular.forEach(resourceList, function (resource) {
                                var resourceObject = angular.copy(resource);
                                delete resourceObject.meta;
                                delete resourceObject.fullUrl;
                                var transactionEntry = {
                                    resource: resourceObject,
                                    request : {
                                        method : "PUT",
                                        url : resource.resourceType + "/" + resource.id
                                    }
                                };
                                transactionBundle.entry.push(transactionEntry);
                            });
                        });
                        deferred.resolve(transactionBundle );
                    });
                });
                return deferred;
            },
            registerContext: function(app, params, isSandboxLaunch){
                var deferred = $.Deferred();

                if (isSandboxLaunch === true) {
                    var reqLaunch = fhirClient.authenticated({
                        url: fhirClient.server.serviceUrl + '/_services/smart/Launch',
                        type: 'POST',
                        contentType: "application/json",
                        data: JSON.stringify({
                            client_id: app.authClient.clientId,
                            parameters:  params
                        })
                    });

                    $.ajax(reqLaunch)
                        .done(deferred.resolve)
                        .fail(deferred.reject);
                } else {
                    // Launch as User Persona
                    var req = fhirClient.authenticated({
                        url: appsSettings.getSandboxUrlSettings().baseRestUrl + "/util/registerContext",
                        type: 'POST',
                        contentType: "application/json",
                        data: JSON.stringify({
                            client_id: app.authClient.clientId,
                            parameters: params
                        })
                    });

                    $.ajax(req)
                        .done(deferred.resolve)
                        .fail(deferred.reject);

                }
                return deferred;
            }
        }
    }).factory('sandboxManagement', function($rootScope, $location, $filter, fhirApiServices,
                                           errorService, appsSettings, userServices, notification, tools) {

        var scenarioBuilder = {
            createdBy: '',
            description: '',
            userPersona: '',
            patient: '',
            app: ''
        };

        var sandbox = {
            sandboxId: '',
            name: '',
            description: '',
            createdBy: '',
            users: [],
            userRoles: []
        };

        var selectedScenario;
        var recentLaunchScenarioList = [];
        var fullLaunchScenarioList = [];
        var sandboxes = [];
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
                lastLaunchSeconds: new Date().getTime(),
                app: angular.copy(launchScenario.app)
            };
            delete newLaunchScenario.app.clientJSON;
            if (launchScenario.userPersona !== undefined && launchScenario.userPersona !== '') {
                newLaunchScenario.userPersona = launchScenario.userPersona;
            }
            if (launchScenario.patient !== undefined && launchScenario.patient !== '') {
                newLaunchScenario.patient = launchScenario.patient;
            }
            return newLaunchScenario;
        }

        return {
            getSandboxes: function() {
                return sandboxes;
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
                    users: [],
                    userRoles: []
                };
            },
            clearSandboxes: function() {
                sandboxes = [];
            },
            clearScenarioBuilder: function() {
                scenarioBuilder = {
                    createdBy: userServices.getOAuthUser(),
                    description: '',
                    userPersona: '',
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
                launchScenario = prepLaunchScenario(launchScenario);
                if (sandbox.sandboxId !== '') {
                    launchScenario.sandbox = sandbox;
                }
                $.ajax({
                    url: appsSettings.getSandboxUrlSettings().baseRestUrl + "/launchScenario",
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
            },
            updateLaunchScenario: function(launchScenario){
                var that = this;
                var updatedLaunchScenario = angular.copy(launchScenario);
                updatedLaunchScenario.app = angular.copy(updatedLaunchScenario.app);
                delete updatedLaunchScenario.app.clientJSON;
                $.ajax({
                    url: appsSettings.getSandboxUrlSettings().baseRestUrl + "/launchScenario/" + updatedLaunchScenario.id,
                    type: 'PUT',
                    data: JSON.stringify(updatedLaunchScenario),
                    contentType: "application/json",
                    beforeSend : function( xhr ) {
                        xhr.setRequestHeader( 'Authorization', 'BEARER ' + fhirApiServices.fhirClient().server.auth.token );
                    }
                }).done(function(result){
                        that.getSandboxLaunchScenarios();
                    }).fail(function(){
                });
            },
            launchScenarioLaunched: function(launchScenario){
                var that = this;
                var updatedLaunchScenario = angular.copy(launchScenario);
                updatedLaunchScenario.app = angular.copy(updatedLaunchScenario.app);
                delete updatedLaunchScenario.app.clientJSON;
                $.ajax({
                    url: appsSettings.getSandboxUrlSettings().baseRestUrl + "/launchScenario/" + updatedLaunchScenario.id + "/launched",
                    type: 'PUT',
                    data: JSON.stringify(updatedLaunchScenario),
                    contentType: "application/json",
                    beforeSend : function( xhr ) {
                        xhr.setRequestHeader( 'Authorization', 'BEARER ' + fhirApiServices.fhirClient().server.auth.token );
                    }
                }).done(function(result){
                    that.getSandboxLaunchScenarios();
                }).fail(function(){
                });
            },
            deleteLaunchScenario: function(launchScenario){
                var that = this;
                $.ajax({
                    url: appsSettings.getSandboxUrlSettings().baseRestUrl + "/launchScenario/" + launchScenario.id,
                    type: 'DELETE',
                    beforeSend : function( xhr ) {
                        xhr.setRequestHeader( 'Authorization', 'BEARER ' + fhirApiServices.fhirClient().server.auth.token );
                    }
                }).done(function(result){
                        notification.message("Launch Scenario Deleted");
                        that.getSandboxLaunchScenarios();
                    }).fail(function(){
                        notification.message("Failed to Delete Launch Scenario");
                    });
            },
            getLaunchScenarioByApp: function(appId){
                var deferred = $.Deferred();
                $.ajax({
                    url: appsSettings.getSandboxUrlSettings().baseRestUrl + "/launchScenario?appId=" + appId,
                    type: 'GET',
                    beforeSend : function( xhr ) {
                        xhr.setRequestHeader( 'Authorization', 'BEARER ' + fhirApiServices.fhirClient().server.auth.token );
                    }
                }).done(function(results){
                    deferred.resolve(results);
                }).fail(function(){
                    deferred.reject();
                });
                return deferred;
            },
            getLaunchScenarioByUserPersona: function(userPersonaId){
                var deferred = $.Deferred();
                $.ajax({
                    url: appsSettings.getSandboxUrlSettings().baseRestUrl + "/launchScenario?userPersonaId=" + userPersonaId,
                    type: 'GET',
                    beforeSend : function( xhr ) {
                        xhr.setRequestHeader( 'Authorization', 'BEARER ' + fhirApiServices.fhirClient().server.auth.token );
                    }
                }).done(function(results){
                    deferred.resolve(results);
                }).fail(function(){
                    deferred.reject();
                });
                return deferred;
            },
            getSandboxLaunchScenarios: function() {
                var deferred = $.Deferred();
                $.ajax({
                    url: appsSettings.getSandboxUrlSettings().baseRestUrl + "/launchScenario?sandboxId=" + sandbox.sandboxId,
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
                    deferred.reject();
                });
                return deferred;
            },
            createSandbox: function(newSandbox) {
                var that = this;
                var deferred = $.Deferred();
                var createSandbox = {
                    createdBy: userServices.getOAuthUser(),
                    name: newSandbox.sandboxName,
                    sandboxId: newSandbox.sandboxId,
                    description: newSandbox.description,
                    schemaVersion: newSandbox.schemaVersion,
                    users: [userServices.getOAuthUser()]
                };

                $.ajax({
                    url: appsSettings.getSandboxUrlSettings().baseRestUrl + "/sandbox",
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
                return deferred;
            },
            updateSandbox: function(sandbox) {
                var that = this;
                var deferred = $.Deferred();
                $.ajax({
                    url: appsSettings.getSandboxUrlSettings().baseRestUrl + "/sandbox/" + sandbox.sandboxId,
                    type: 'PUT',
                    data: JSON.stringify(sandbox),
                    contentType: "application/json",
                    beforeSend : function( xhr ) {
                        xhr.setRequestHeader( 'Authorization', 'BEARER ' + fhirApiServices.fhirClient().server.auth.token );
                    }
                }).done(function(){
                    that.getSandboxById();
                    notification.message("Sandbox Updated");
                    deferred.resolve(true);
                }).fail(function(){
                });
                return deferred;
            },
            deleteSandbox: function() {
                var that = this;
                var deferred = $.Deferred();
                $.ajax({
                    url: appsSettings.getSandboxUrlSettings().baseRestUrl + "/sandbox/" + sandbox.sandboxId,
                    type: 'DELETE',
                    contentType: "application/json",
                    beforeSend : function( xhr ) {
                        xhr.setRequestHeader( 'Authorization', 'BEARER ' + fhirApiServices.fhirClient().server.auth.token );
                    }
                }).done(function(){
                    deferred.resolve(true);
                }).fail(function(){
                });
                return deferred;
            },
            getUserSandboxesByUserId: function() {
                var that = this;
                var deferred = $.Deferred();
                $.ajax({
                    url: appsSettings.getSandboxUrlSettings().baseRestUrl + "/sandbox?userId=" + encodeURIComponent(userServices.getOAuthUser().ldapId),
                    type: 'GET',
                    contentType: "application/json",
                    beforeSend : function( xhr ) {
                        xhr.setRequestHeader( 'Authorization', 'BEARER ' + fhirApiServices.fhirClient().server.auth.token );
                    }
                }).done(function(sandboxResult){
                    if (sandboxResult.length > 0) {
                        that.setHasSandbox(true);
                        sandboxes = sandboxResult;
                        deferred.resolve(true);
                    } else {
                        that.clearSandboxes();
                        deferred.resolve(false);
                    }
                }).fail(function(){
                    that.clearSandboxes();
                    deferred.resolve(false);
                });
                return deferred;
            },
            removeUserFromSandboxByUserId: function(ldapId) {
                var that = this;
                var deferred = $.Deferred();
                $.ajax({
                    url: appsSettings.getSandboxUrlSettings().baseRestUrl + "/sandbox/" + sandbox.sandboxId + "?removeUserId=" + encodeURIComponent(ldapId),
                    type: 'PUT',
                    contentType: "application/json",
                    beforeSend : function( xhr ) {
                        xhr.setRequestHeader( 'Authorization', 'BEARER ' + fhirApiServices.fhirClient().server.auth.token );
                    }
                }).done(function(sandboxResult){
                    deferred.resolve(true);
                }).fail(function(){
                });
                return deferred;
            },
            getSandboxById: function() {
                var that = this;
                var deferred = $.Deferred();
                appsSettings.getSettings().then(function(settings){
                    var sandboxId = appsSettings.getSandboxUrlSettings().sandboxId;
                    if (sandboxId !== undefined && settings.reservedEndpoints.indexOf(sandboxId.toLowerCase()) > -1) {
                        deferred.resolve("reserved");
                    } else if (sandboxId !== undefined) {
                        $.ajax({
                            url: appsSettings.getSandboxUrlSettings().baseRestUrl + "/sandbox/" + sandboxId,
                            type: 'GET',
                            contentType: "application/json",
                            beforeSend: function (xhr) {
                                xhr.setRequestHeader('Authorization', 'BEARER ' + fhirApiServices.fhirClient().server.auth.token);
                            }
                        }).done(function (sandboxResult) {
                            if (sandboxResult !== undefined && sandboxResult !== "") {
                                if (sandboxId !== undefined && sandboxId !== "" && sandboxId !== sandboxResult.sandboxId) {
                                    deferred.resolve('invalid');
                                } else {
                                    that.setHasSandbox(true);
                                    sandbox = sandboxResult;
                                    $rootScope.$emit('refresh-sandboxes');
                                    that.getSandboxLaunchScenarios();
                                    deferred.resolve(true);
                                }
                            } else {
                                that.clearSandbox();
                                deferred.resolve(false);
                            }
                        }).fail(function () {
                            that.clearSandbox();
                            deferred.resolve(false);
                        });
                    } else {
                        that.clearSandbox();
                        deferred.resolve(false);
                    }
                });
                return deferred;
            },
            sandboxLogin: function(ldapId) {
                var that = this;
                var deferred = $.Deferred();
                // Record the sandbox login 
                $.ajax({
                    url: appsSettings.getSandboxUrlSettings().baseRestUrl + "/sandbox/" + sandbox.sandboxId + "/login" + "?userId=" + encodeURIComponent(ldapId),
                    type: 'POST',
                    contentType: "application/json",
                    beforeSend : function( xhr ) {
                        xhr.setRequestHeader( 'Authorization', 'BEARER ' + fhirApiServices.fhirClient().server.auth.token );
                    }
                }).done(function(sandboxResult){
                    deferred.resolve(true);
                }).fail(function(){
                });
                return deferred;
            },
            fhirQuerySuggestions: function() {
                var that = this;
                var deferred = $.Deferred();
                $.ajax({
                    url: appsSettings.getSandboxUrlSettings().baseRestUrl + "/config/0",
                    type: 'GET',
                    contentType: "application/json",
                    beforeSend : function( xhr ) {
                        xhr.setRequestHeader( 'Authorization', 'BEARER ' + fhirApiServices.fhirClient().server.auth.token );
                    }
                }).done(function(results){
                    var suggestions = [];
                    var defaultSuggestions = [];
                    if (results) {
                        results.forEach(function(item){
                            suggestions.push(item.value);
                        });
                        results.forEach(function(item){
                            if (item.keyName.startsWith("Default")) {
                                defaultSuggestions.push(item.value);
                            }
                        });
                    }
                    deferred.resolve(suggestions, defaultSuggestions);
                }).fail(function(){
                    deferred.reject();
                });
                return deferred;
            }
        }

    }).factory('userServices', function($rootScope, fhirApiServices, $filter, appsSettings) {
        var oauthUser;
        var sandboxManagerUser;

        return {
            updateProfile: function(selectedUser){
                var deferred = $.Deferred();
                var that = this;
                appsSettings.getSettings().then(function(settings){

                    $.ajax({
                        url: settings.sandboxUserUri + "/profileupdate",
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
            getOAuthUser: function() {
                return oauthUser;
            },
            clearOAuthUser: function () {
                oauthUser = undefined;
                this.clearSandboxManagerUser();
            },
            sandboxManagerUser: function() {
                return sandboxManagerUser;
            },
            clearSandboxManagerUser: function () {
                sandboxManagerUser = undefined;
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
                                ldapId: result.sub.toLowerCase(),
                                name: result.name
                            };
                            deferred.resolve(oauthUser);
                        }).fail(function(){
                        });
                }
                return deferred;
            },
            getSandboxManagerUser: function(ldapId) {
                var deferred = $.Deferred();
                if (sandboxManagerUser !== undefined) {
                    deferred.resolve(sandboxManagerUser);
                } else {
                    $.ajax({
                        url: appsSettings.getSandboxUrlSettings().baseRestUrl + "/user?ldapId=" + ldapId,
                        type: 'GET',
                        contentType: "application/json",
                        beforeSend: function (xhr) {
                            xhr.setRequestHeader('Authorization', 'BEARER ' + fhirApiServices.fhirClient().server.auth.token);
                        }
                    }).done(function (userResult) {
                        sandboxManagerUser = userResult;
                        deferred.resolve(userResult);
                    }).fail(function () {
                        deferred.reject();
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
            },
            hasSystemRole: function(role) {
                var hasRole = false;
                sandboxManagerUser.systemRoles.forEach(function(systemRole){
                    if (systemRole === role) {
                        hasRole = true;
                    }
                });
                return hasRole;
            },
            hasSandboxRole: function(roles, role) {
                var hasRole = false;
                roles.forEach(function(userRole){
                    if (sandboxManagerUser != undefined && userRole.user.ldapId.toLocaleLowerCase() === sandboxManagerUser.ldapId.toLocaleLowerCase()
                        && userRole.role === role) {
                        hasRole = true;
                    }
                });
                return hasRole;
            },
            canModify: function(item, sandbox) {
                if (item.visibility === "PRIVATE") {
                    return item.createdBy.ldapId.toLocaleLowerCase() === this.sandboxManagerUser().ldapId.toLocaleLowerCase();
                } else { // PUBLIC Item
                    if (sandbox.visibility === "PRIVATE") {
                        return !this.hasSandboxRole(item.sandbox.userRoles, "READ_ONLY");
                    } else {
                        return this.hasSandboxRole(sandbox.userRoles, "ADMIN");
                    }
                }
            },
            canModifySandbox: function(sandbox) {
                if (sandbox.visibility === "PRIVATE") {
                    return !this.hasSandboxRole(sandbox.userRoles, "READ_ONLY");
                } else {
                    return this.hasSandboxRole(sandbox.userRoles, "ADMIN");
                }
            },
            canInviteUsers: function(sandbox) {
                if (sandbox.visibility === "PRIVATE") { // No invite needed on a PUBLIC sandbox
                    return !this.hasSandboxRole(sandbox.userRoles, "READ_ONLY");
                }
            },
            createUser: function() {
                var that = this;
                appsSettings.getSettings().then(function(settings){
                    window.location.href = settings.userManagementUrl + "/public/newuser/"
                });
            }
        };
    }).factory('personaServices', function($rootScope, sandboxManagement, fhirApiServices, userServices, appsSettings) {
    var personaList = [];
    var personaBuilder = {
        fhirId: '',
        fhirName: '',
        ldapId: '',
        ldapName: '',
        resource: '',
        resourceUrl: '',
        password: '',
        sandbox: sandboxManagement.getSandbox()
    };

    return {
        getUserPersonaBuilder: function() {
            return personaBuilder;
        },
        resourceIdFromFullUrl: function(fullUrl) {
            var resourceId = angular.copy(fullUrl);
            if (resourceId.startsWith(fhirApiServices.fhirClient().server.serviceUrl)) {
                resourceId = resourceId.substr(fhirApiServices.fhirClient().server.serviceUrl.length + 1);
            }
            return resourceId;
        },
        getPersonaList: function() {
            return personaList;
        },
        clearUserPersonaBuilder: function() {
            return personaBuilder = {
                fhirId: '',
                fhirName: '',
                ldapId: '',
                ldapName: '',
                resource: '',
                resourceUrl: '',
                password: '',
                sandbox: sandboxManagement.getSandbox(),
                createdBy: userServices.getOAuthUser()
            };
        },
        createPersona: function(){
            var deferred = $.Deferred();
            var that = this;
            $.ajax({
                url: appsSettings.getSandboxUrlSettings().baseRestUrl + "/userPersona",
                type: 'POST',
                data: JSON.stringify(personaBuilder),
                contentType: "application/json",
                beforeSend : function( xhr ) {
                    xhr.setRequestHeader( 'Authorization', 'BEARER ' + fhirApiServices.fhirClient().server.auth.token );
                }
            }).done(function(){
                that.clearUserPersonaBuilder();
                that.getPersonaListBySandbox();
                deferred.resolve();
                $rootScope.$digest();
            }).fail(function(){
                deferred.reject();
            });
            return deferred;
        },
        updatePersona: function(persona){
            var deferred = $.Deferred();
            var that = this;
            $.ajax({
                url: appsSettings.getSandboxUrlSettings().baseRestUrl + "/userPersona",
                type: 'PUT',
                data: JSON.stringify(persona),
                contentType: "application/json",
                beforeSend : function( xhr ) {
                    xhr.setRequestHeader( 'Authorization', 'BEARER ' + fhirApiServices.fhirClient().server.auth.token );
                }
            }).done(function(){
                that.clearUserPersonaBuilder();
                that.getPersonaListBySandbox();
                deferred.resolve();
                $rootScope.$digest();
            }).fail(function(){
                deferred.reject();
            });
            return deferred;
        },
        deletePersona: function(persona){
            var deferred = $.Deferred();
            var that = this;
            $.ajax({
                url: appsSettings.getSandboxUrlSettings().baseRestUrl + "/userPersona/" + persona.id,
                type: 'DELETE',
                beforeSend : function( xhr ) {
                    xhr.setRequestHeader( 'Authorization', 'BEARER ' + fhirApiServices.fhirClient().server.auth.token );
                }
            }).done(function(){
                that.getPersonaListBySandbox();
                deferred.resolve();
                $rootScope.$digest();
            }).fail(function(){
                deferred.reject();
            });
            return deferred;
        },
        getPersonaListBySandbox: function() {
            var deferred = $.Deferred();
            $.ajax({
                url: appsSettings.getSandboxUrlSettings().baseRestUrl + "/userPersona?sandboxId=" + sandboxManagement.getSandbox().sandboxId,
                type: 'GET',
                contentType: "application/json",
                beforeSend : function( xhr ) {
                    xhr.setRequestHeader( 'Authorization', 'BEARER ' + fhirApiServices.fhirClient().server.auth.token );
                }
            }).done(function(results){
                personaList = results;
                $rootScope.$emit('persona-list-update');
                deferred.resolve(results);
            }).fail(function(){
            });
            return deferred;
        },
        checkForUserPersonaById: function(userPersonaId) {
            var deferred = $.Deferred();
            $.ajax({
                url: appsSettings.getSandboxUrlSettings().baseRestUrl + "/userPersona?lookUpId=" + userPersonaId,
                type: 'GET'
            }).done(function(persona){
                if (persona !== undefined && persona !== "") {
                    deferred.resolve(persona);
                } else {
                    deferred.resolve(undefined);
                }

                $rootScope.$digest();

            }).fail(function(error){
                deferred.resolve(undefined);
                $rootScope.$digest();
            });
            return deferred;
        }
    };
}).factory('appRegistrationServices', function($rootScope, $http, appsSettings, userServices,
                                                   fhirApiServices, sandboxManagement, notification, errorService) {

    var selectedApp;
    var fullAppList = [];

    return {
        setSelectedApp: function(app) {
            selectedApp = app;
        },
        getSelectedApp: function() {
            return selectedApp;
        },
        getAppList: function() {
            return fullAppList;
        },
        createSandboxApp: function(app){
            var deferred = $.Deferred();
            var that = this;
            app.sandbox = sandboxManagement.getSandbox();
            app.createdBy = userServices.getOAuthUser();

            var logo = app.logo;
            delete app.logo;

            app.clientJSON = JSON.stringify(app.clientJSON);
            $.ajax({
                url: appsSettings.getSandboxUrlSettings().baseRestUrl + "/app",
                type: 'POST',
                data: JSON.stringify(app),
                contentType: "application/json",
                beforeSend : function( xhr ) {
                    xhr.setRequestHeader( 'Authorization', 'BEARER ' + fhirApiServices.fhirClient().server.auth.token );
                }
            }).done(function(result){
                if (logo) {
                    that.uploadAppImage(result.id, logo).then(function () {
                        that.getSandboxApps();
                        notification.message("App Created");
                        deferred.resolve();
                    }, function(err) {
                        deferred.reject();
                    });
                }else {
                    that.getSandboxApps();
                    notification.message("App Created");
                    deferred.resolve();
                }
            }).fail(function(error){
                errorService.setErrorMessage(error.message);
                notification.message({ type:"error", text: "Failed to Create App" });
                deferred.reject();
            });
            return deferred;
        },
        updateSandboxApp: function(app){
            var deferred = $.Deferred();
            var that = this;
            var logo = app.logo;
            delete app.logo;
            var newApp = angular.copy(app);
            newApp.clientJSON = JSON.stringify(newApp.clientJSON);

            $.ajax({
                url: appsSettings.getSandboxUrlSettings().baseRestUrl + "/app/" + newApp.id,
                type: 'PUT',
                data: JSON.stringify(newApp),
                contentType: "application/json",
                beforeSend : function( xhr ) {
                    xhr.setRequestHeader( 'Authorization', 'BEARER ' + fhirApiServices.fhirClient().server.auth.token );
                }
            }).done(function(result){
                if (logo) {
                    that.uploadAppImage(result.id, logo).then(function () {
                        that.getSandboxApps();
                        notification.message("App Updated");
                        deferred.resolve();
                    }, function(err) {
                        deferred.reject();
                    });
                }else {
                    that.getSandboxApps();
                    notification.message("App Updated");
                    deferred.resolve();
                }
            }).fail(function(error){
                errorService.setErrorMessage(error.message);
                notification.message({ type:"error", text: "Failed to Update App" });
                deferred.reject();
            });
            return deferred;
        },
        deleteSandboxApp: function(appId){
            var deferred = $.Deferred();
            var that = this;
            $.ajax({
                url: appsSettings.getSandboxUrlSettings().baseRestUrl + "/app/" + appId,
                type: 'DELETE',
                beforeSend : function( xhr ) {
                    xhr.setRequestHeader( 'Authorization', 'BEARER ' + fhirApiServices.fhirClient().server.auth.token );
                }
            }).done(function(result){
                that.getSandboxApps();
                deferred.resolve();
                notification.message("App Deleted");
            }).fail(function(error){
                errorService.setErrorMessage(error.message);
                notification.message({ type:"error", text: "Failed to Delete App" });
                deferred.reject();
            });
            return deferred;
        },
        getSandboxApp: function(appId){
            var deferred = $.Deferred();
            var that = this;
            $.ajax({
                url: appsSettings.getSandboxUrlSettings().baseRestUrl + "/app/" + appId,
                type: 'GET',
                beforeSend : function( xhr ) {
                    xhr.setRequestHeader( 'Authorization', 'BEARER ' + fhirApiServices.fhirClient().server.auth.token );
                }
            }).done(function(clientJSON){
                deferred.resolve(clientJSON);
            }).fail(function(error){
                errorService.setErrorMessage(error.message);
                deferred.reject();
                notification.message({ type:"error", text: "Failed to Retrieve App Info" });
            });
            return deferred;
        },
        getSandboxApps: function(){
            var deferred = $.Deferred();
            var that = this;
            $.ajax({
                url: appsSettings.getSandboxUrlSettings().baseRestUrl + "/app?sandboxId=" + sandboxManagement.getSandbox().sandboxId,
                type: 'GET',
                beforeSend : function( xhr ) {
                    xhr.setRequestHeader( 'Authorization', 'BEARER ' + fhirApiServices.fhirClient().server.auth.token );
                }
            }).done(function(results){
                fullAppList = [];
                if (results) {
                    results.forEach(function(app){
                        fullAppList.push(app);
                    });
                    $rootScope.$emit('app-list-update');
                }
                deferred.resolve(fullAppList);
                // $rootScope.$digest();
            }).fail(function(){
                deferred.reject();
            });
            return deferred;
        },
        uploadAppImage: function(id, file){
            var deferred = $.Deferred();
            var formData = new FormData();
            formData.append("file", file);
            $http.post(appsSettings.getSandboxUrlSettings().baseRestUrl + "/app/" + id + "/image", formData, {
                transformRequest: angular.identity,
                headers: {
                    'Content-Type': undefined,
                    'Authorization': 'BEARER ' + fhirApiServices.fhirClient().server.auth.token
                }
            }).success(function(){
                deferred.resolve();
            }).error(function(error){
                errorService.setErrorMessage(error.message);
                deferred.reject();
                // notification.message({ type:"error", text: "Failed to Upload Image" });
            });
            return deferred;
        }

    };
}).factory('sandboxInviteServices', function($rootScope, $http, fhirApiServices, userServices,
                                             appsSettings, sandboxManagement, notification, errorService) {

    return {
        createSandboxInvite: function(ldapId){
            var deferred = $.Deferred();
            
            var sandboxInvite = {
                invitedBy: {
                    ldapId: userServices.getOAuthUser().ldapId
                },
                invitee: {
                    ldapId: ldapId
                },
                sandbox: sandboxManagement.getSandbox()
            };

            $.ajax({
                url: appsSettings.getSandboxUrlSettings().baseRestUrl + "/sandboxinvite",
                type: 'PUT',
                data: JSON.stringify(sandboxInvite),
                contentType: "application/json",
                beforeSend : function( xhr ) {
                    xhr.setRequestHeader( 'Authorization', 'BEARER ' + fhirApiServices.fhirClient().server.auth.token );
                }
            }).done(function(){
                // notification.message("Invite Sent");
                deferred.resolve();
            }).fail(function(error){
                // notification.message({ type:"error", text: "Failed to Send Invite" });
                deferred.reject();
            });
            return deferred;
        },
        getSandboxInvitesByLdapId: function(status){
            var deferred = $.Deferred();
            var that = this;
            $.ajax({
                url: appsSettings.getSandboxUrlSettings().baseRestUrl + "/sandboxinvite?ldapId=" + encodeURIComponent(userServices.getOAuthUser().ldapId) +
                    "&status=" + status,
                type: 'GET',
                beforeSend : function( xhr ) {
                    xhr.setRequestHeader( 'Authorization', 'BEARER ' + fhirApiServices.fhirClient().server.auth.token );
                }
            }).done(function(results){
                deferred.resolve(results);
                $rootScope.$digest();
            }).fail(function(){
                deferred.reject();
            });
            return deferred;
        },
        getSandboxInvitesBySandboxId: function(status){
            var deferred = $.Deferred();
            var that = this;
            $.ajax({
                url: appsSettings.getSandboxUrlSettings().baseRestUrl + "/sandboxinvite?sandboxId=" + sandboxManagement.getSandbox().sandboxId +
                "&status=" + status,
                type: 'GET',
                beforeSend : function( xhr ) {
                    xhr.setRequestHeader( 'Authorization', 'BEARER ' + fhirApiServices.fhirClient().server.auth.token );
                }
            }).done(function(results){
                deferred.resolve(results);
                $rootScope.$digest();
            }).fail(function(){
                deferred.reject();
            });
            return deferred;
        },
        updateSandboxInvite: function(sandboxInvite, status){
            var deferred = $.Deferred();
            var that = this;
            $.ajax({                                                                                                 
                url: appsSettings.getSandboxUrlSettings().baseRestUrl + "/sandboxinvite/" + sandboxInvite.id + "?status=" + status,
                type: 'PUT',
                beforeSend : function( xhr ) {
                    xhr.setRequestHeader( 'Authorization', 'BEARER ' + fhirApiServices.fhirClient().server.auth.token );
                }
            }).done(function(results){
                deferred.resolve(results);
                $rootScope.$digest();
            }).fail(function(){
                deferred.reject();
            });
            return deferred;
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

    }).factory('launchApp', function($rootScope, fhirApiServices, personaServices, appsService, appsSettings, random) {

        var patientDataManagerApp;
        var settings;
        var appWindow;

        appsSettings.getSettings().then(function(results) {
            settings = results;
        });

        getPatientDataManagerApp();

        function registerAppContext(app, params, key, launchAsUserPersona, isSandboxLaunch) {
            var appToLaunch = angular.copy(app);
            delete appToLaunch.clientJSON;
            var issuer = fhirApiServices.fhirClient().server.serviceUrl;
            if (launchAsUserPersona) {
                appsSettings.getSettings().then(function(settings){
                    issuer = fhirApiServices.fhirClient().server.serviceUrl.replace(settings.baseServiceUrl_1, settings.basePersonaServiceUrl_1);
                    if (appToLaunch.sandbox.schemaVersion === "2") {
                        issuer = fhirApiServices.fhirClient().server.serviceUrl.replace(settings.baseServiceUrl_2, settings.basePersonaServiceUrl_2);
                    }
                });
            }

            fhirApiServices
                .registerContext(appToLaunch, params, isSandboxLaunch)
                .done(function(c){
                    console.log(fhirApiServices.fhirClient());
                    window.localStorage[key] = JSON.stringify({
                        app: appToLaunch,
                        iss: issuer,
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
            appsService.getSampleApps().done(function(patientApps){
                var pdm;
                for (var i=0; i < patientApps.length; i++) {
                    if (patientApps[i]["authClient"]["clientId"] == "patient_data_manager") {
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

            launch: function(app, patientContext, contextParams, userPersona, sandboxLaunch) {
                var key = random(32);
                window.localStorage[key] = "requested-launch";
                // var appWindow;
                    // = window.open('launch.html' + "" +
                    // '?username=' + encodeURIComponent(userPersona.ldapId) +
                    // '&password=' + encodeURIComponent(userPersona.password) +
                    // '&redirect=' + encodeURIComponent(window.location.href + '/launch.html?key='+key ), '_blank');

                var params = {};
                if (patientContext !== undefined && patientContext.name !== 'None' && patientContext !== "") {
                    params = {patient: patientContext.fhirId}
                }

                if (contextParams !== undefined) {
                    for (var i=0; i < contextParams.length; i++) {
                        params[contextParams[i].name] = contextParams[i].value;
                    }
                }

                if (userPersona !== null && userPersona !== undefined && userPersona !== "" ) {
                    appWindow = window.open('launch.html?key='+key +
                        '&username=' + encodeURIComponent(userPersona.ldapId) +
                        '&password=' + encodeURIComponent(userPersona.password) +
                        '&auth=' + encodeURIComponent(settings.oauthAuthenticationUrl));
                        registerAppContext(app, params, key, true, sandboxLaunch);
                } else {
                    appWindow = window.open('launch.html?'+key, '_blank');
                    registerAppContext(app, params, key, false, sandboxLaunch);
                }
            },
            launchPatientDataManager: function(patient){
                if (patient.fhirId === undefined){
                    patient.fhirId = patient.id;
                }
                this.launch(patientDataManagerApp, patient, undefined, undefined, true );
            },
            launchFromApp: function(app, patient){
                this.launch(app, patient, undefined, undefined, true );
            }
    }

    }).factory('descriptionBuilder', function() {
        return  {
            launchScenarioDescription: function(scenario){

                var desc = {title: "", detail: ""};

                if (scenario.userPersona !== null && scenario.userPersona.resource === 'Practitioner') {
                    desc.title = "Launch App as a Practitioner";
                    if (scenario.patient.resource !== "None") {
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
    }).factory('dataManagerService', function() {
        var settings = {
            showing: {import: {results: false}, export: {results: false}},
            bundleResults: "",
            resourceList: [],
            fhirQuery: "",
            selected: {selectedResource: undefined},
            allQuerySuggestions: [],
            dataManagerService: [],
            querySuggestions: [],
            selectedResourceType: {}
        };
    
        return {
            getSettings: function () {
                return settings;
            }
        };
    }).factory('tools', function(appsSettings, $rootScope) {

        return {
            validateSandboxIdFromUrl: function() {
                var deferred = $.Deferred();

                if (appsSettings.getSandboxUrlSettings().sandboxId !== undefined) {
                    this.checkForSandboxById(appsSettings.getSandboxUrlSettings().sandboxId).then(function(sandbox){
                        if (sandbox !== undefined && sandbox !== "") {
                            deferred.resolve(appsSettings.getSandboxUrlSettings().sandboxId, sandbox.schemaVersion);
                        } else {
                            deferred.reject();
                        }
                    });
                } else {
                    deferred.reject();
                }
                return deferred;
            },
            checkForSandboxById: function(sandboxId) {
                var deferred = $.Deferred();
                appsSettings.getSettings().then(function(settings){
                    if (settings.reservedEndpoints.indexOf(sandboxId.toLowerCase()) > -1) {
                        deferred.resolve("reserved");
                    } else {
                        $.ajax({
                            url: appsSettings.getSandboxUrlSettings().baseRestUrl + "/sandbox?lookUpId=" + sandboxId,
                            type: 'GET'
                        }).done(function(sandbox){
                            if (sandbox !== undefined && sandbox !== "") {
                                deferred.resolve(sandbox);
                            } else {
                                deferred.resolve(undefined);
                            }

                            $rootScope.$digest();

                        }).fail(function(error){
                            deferred.resolve(undefined);
                            $rootScope.$digest();
                        });
                    }
                });
                return deferred;
            },
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

    }).factory('appsService', ['$http', 'envInfo',function($http, envInfo)  {

    var sampleApps;

    return {
        getSampleApps : function() {
            var deferred = $.Deferred();
            if (sampleApps !== undefined) {
                deferred.resolve(sampleApps);
            } else {
                this.loadSettings().then(function(){
                    deferred.resolve(sampleApps);
                });
            }
            return deferred;
        },
        loadSettings: function(){
            var deferred = $.Deferred();
            if (envInfo.env !== "null") {
                $http.get('static/js/config/sample-apps-' + envInfo.env + '.json').success(function (result) {
                    sampleApps = result;
                    deferred.resolve(sampleApps);
                });
            } else {
                $http.get('static/js/config/sample-apps-localhost.json').success(function (result) {
                    sampleApps = result;
                    deferred.resolve(sampleApps);
                });
            }
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

}]).factory('exportResources', ['$http',function($http)  {
    var resources;

    return {
        loadSettings: function(){
            var deferred = $.Deferred();
            $http.get('static/js/config/export-resources.json').success(function(result){
                resources = result;
                deferred.resolve(result);
            });
            return deferred;
        },
        getExportResources: function(){
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

}]).factory('dataManagerResources', ['$http',function($http)  {
    var resources;

    return {
        loadSettings: function(){
            var deferred = $.Deferred();
            $http.get('static/js/config/data-manager-resources.json').success(function(result){
                resources = result;
                deferred.resolve(result);
            });
            return deferred;
        },
        getDataManagerResources: function(){
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

}]).factory('branded', ['brandedText', 'envInfo',function(brandedText, envInfo)  {
    var text = brandedText["hspc"];
    if (envInfo.hostOrg !== undefined && envInfo.hostOrg !== "null") {
        text = brandedText[envInfo.hostOrg];
    }
    return text;
}]).factory('appsSettings', ['$http', 'envInfo',function($http, envInfo)  {

    var settings;
    var sandboxUrlSettings;

    function getDashboardUrl(isLocal, fullBaseUrl) {

        if (!isLocal) {
            // In test/prod the dashboard url is the part of the URL which does not include the path
            var path = window.location.pathname;
            var trailingPathSlash = path.lastIndexOf("/");
            if (trailingPathSlash > -1 && trailingPathSlash === path.length - 1) {
                path = path.substring(0, path.length - 1);
            }
            return fullBaseUrl.substring(0, fullBaseUrl.length - path.length);
        } else {
            var urlPath = window.location.pathname;
            var trailingSlash = urlPath.lastIndexOf("/");
            if (trailingSlash > -1 && trailingSlash === urlPath.length - 1) {
                urlPath = urlPath.substring(0, urlPath.length - 1);
            }
            var leadingSlash = urlPath.indexOf("/");
            if (leadingSlash === 0) {
                urlPath = urlPath.substring(1, urlPath.length);
            }
            var pathSegments = urlPath.split("/");
            switch (pathSegments.length) {
                case 1:   // For localhost, the dashboard url includes the first path segment
                    return fullBaseUrl;
                    break;
                default:  // For localhost, the dashboard url includes the first path segment,
                          // the second path segment (if exists) is the sandboxId
                    var additionalPath = urlPath.substring(pathSegments[0].length);
                    return fullBaseUrl.substring(0, fullBaseUrl.length - additionalPath.length);
                    break;
            }
        }
    }

    return {
        getSandboxUrlSettings: function () {
            if (sandboxUrlSettings !== undefined) {
                return sandboxUrlSettings;
            } else {
                sandboxUrlSettings = {};
                var sandboxBaseUrlWithoutHash = window.location.href.split("#")[0].substring(0, window.location.href.split("#")[0].length);
                if (sandboxBaseUrlWithoutHash.endsWith("/")) {
                    sandboxBaseUrlWithoutHash = sandboxBaseUrlWithoutHash.substring(0, sandboxBaseUrlWithoutHash.length-1);
                }
                sandboxUrlSettings.sandboxManagerRootUrl = getDashboardUrl(envInfo.defaultServiceUrl === "null", sandboxBaseUrlWithoutHash);
                sandboxUrlSettings.sandboxId = sandboxBaseUrlWithoutHash.substring(sandboxUrlSettings.sandboxManagerRootUrl.length + 1);
                var trailingSlash = sandboxUrlSettings.sandboxId.lastIndexOf("/");
                if (trailingSlash > -1 && trailingSlash === sandboxUrlSettings.sandboxId.length - 1) {
                    sandboxUrlSettings.sandboxId = sandboxUrlSettings.sandboxId.substring(0, sandboxUrlSettings.sandboxId.length - 1);
                }
                if (sandboxUrlSettings.sandboxId === "") {
                    sandboxUrlSettings.sandboxId = undefined;
                }
                sandboxUrlSettings.baseRestUrl = sandboxUrlSettings.sandboxManagerRootUrl + "/REST";
                
            }
            return sandboxUrlSettings;    
        },
        loadSettings: function(){
            var deferred = $.Deferred();
            $http.get('static/js/config/sandbox-manager.json').success(function(result){
                settings = result;
                if (envInfo.active !== "null" && envInfo.active !== "false") {
                    settings.sandboxUserUri = envInfo.sandboxUserUri;
                    settings.defaultServiceUrl = envInfo.defaultServiceUrl;
                    settings.baseServiceUrl_1 = envInfo.baseServiceUrl_1;
                    settings.baseServiceUrl_2 = envInfo.baseServiceUrl_2;
                    settings.basePersonaServiceUrl_1 = envInfo.basePersonaServiceUrl_1;
                    settings.basePersonaServiceUrl_2 = envInfo.basePersonaServiceUrl_2;
                    settings.oauthLogoutUrl = envInfo.oauthLogoutUrl;
                    settings.oauthAuthenticationUrl = envInfo.oauthAuthenticationUrl;
                    settings.userManagementUrl = envInfo.userManagementUrl;
                }
                deferred.resolve(settings);
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
        },
        settings: function(){
            return settings;
        }
    };

}]);
