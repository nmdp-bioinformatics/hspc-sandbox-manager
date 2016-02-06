'use strict';

angular.module('sandManApp', ['ui.router', 'ngSanitize', 'ngAnimate', 'ui.bootstrap', 'sandManApp.filters', 'sandManApp.services',
    'sandManApp.controllers', 'sandManApp.directives'], function($stateProvider, $urlRouterProvider){

    $urlRouterProvider.otherwise('/login');

    $stateProvider

        .state('login', {
            url: '/login',
            templateUrl: 'static/js/templates/login.html'
        })

        .state('patient-view', {
            url: '/patient-view/:source',
            templateUrl: 'static/js/templates/patientView.html',
            authenticate: true
        })

        .state('practitioner-view', {
            url: '/practitioner-view',
            templateUrl: 'static/js/templates/practitionerView.html',
            authenticate: true
        })

        .state('launch-scenarios', {
            url: '/launch-scenarios',
            templateUrl: 'static/js/templates/launchScenarioView.html',
            authenticate: true
        })

        .state('apps', {
            url: '/apps/:source/:action',
            templateUrl: 'static/js/templates/appsLauncher.html',
            scenarioBuilderStep: true,
            authenticate: true
        })

        .state('patients', {
            url: '/patients',
            templateUrl: 'static/js/templates/patients.html',
            authenticate: true
        })

        .state('practitioners', {
            url: '/practitioners',
            templateUrl: 'static/js/templates/practitioners.html',
            authenticate: true
        })

        .state('users', {
            url: '/users',
            templateUrl: 'static/js/templates/users.html',
            authenticate: true
        })

        .state('after-auth', {
            url: '/after-auth',
            templateUrl:'static/js/templates/start.html'
        })

        .state('resolve', {
            url: '/resolve/:context/against/:iss/for/:clientName/then/:endpoint',
            templateUrl:'static/js/templates/resolve.html'
        });

});
