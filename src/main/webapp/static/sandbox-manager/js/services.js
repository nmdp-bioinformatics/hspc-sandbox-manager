'use strict';

angular.module('sandManApp.services', [])
    .factory('fhirSettings', function() {

    var settings = {
            name: 'Local HSP dev server, oauth',
            serviceUrl: 'http://localhost:8080/hsp-reference-api/data',
            auth: {
                type: 'oauth2'
            }
        };

    return {
        get: function() {
            return settings;
        }
    }

}).factory('oauth2', function($rootScope, $location) {

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
            }
        };

    }).factory('fhirApiServices', function (oauth2, fhirSettings, patientDetails, $rootScope, $location) {

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
                    oauth2.authorize(fhirSettings.get());
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
            }
        }
    }).factory('userServices', function($rootScope, fhirApiServices, patientDetails) {
        var fhirUser = {};
        var oauthUser = {};

        return {
            fhirUser: function(){
                return fhirUser;
            },
            oauthUser: function(){
                return oauthUser;
            },
            updateProfile: function(selectedUser){
                $.ajax({
                    // TODO get the root path from the fhir url
                    url: "http://localhost:8080/hsp-reference-messaging/sandboxuser/profileupdate",
                    type: 'POST',
                    data: JSON.stringify({
                        user_id: oauthUser.sub,
                        profile_url: selectedUser.fullUrl
                    }),
                    contentType: "application/json"
                }).done(function(result){
                        //TODO check result value for 200 or 201
                        fhirUser = selectedUser;
                        $rootScope.$emit('profile-change');
                        $rootScope.$digest();
                    }).fail(function(){
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
                        fhirUser = user;
                        fhirUser.fullUrl = userResult.config.url;
                        deferred.resolve(user);
                    });
                return deferred;
            },
            getOAuthUser: function() {
                var deferred = $.Deferred();
                var userInfoEndpoint = fhirApiServices.fhirClient().state.provider.oauth2.authorize_uri.replace("authorize", "userinfo");
                $.ajax({
                    url: userInfoEndpoint,
                    type: 'GET',
                    contentType: "application/json",
                    beforeSend : function( xhr ) {
                        xhr.setRequestHeader( 'Authorization', 'BEARER ' + fhirApiServices.fhirClient().server.auth.token );
                    }
                }).done(function(result){
                        oauthUser = result;
                        deferred.resolve(result);
                    }).fail(function(){
                    });
                return deferred;
            }
        };
    }).factory('patientDetails', function() {
        return {
            id: function(p){
                return p.id;
            },
            name: function(p){
                if (p.resourceType === "Patient") {
                    var name = p && p.name && p.name[0];
                    if (!name) return null;

                    return name.given.join(" ") + " " + name.family.join(" ");
                } else {
                    var name = p && p.name;
                    if (!name) return null;

                    var practitioner =  name.given.join(" ") + " " + name.family.join(" ");
                    if (name.suffix) {
                        practitioner = practitioner + " " + name.suffix.join(", ");
                    }
                    return practitioner;
                }
            }
        };
    });
