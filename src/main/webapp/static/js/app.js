'use strict';

angular.module('sandManApp', ['ui.router', 'ngSanitize', 'sandManApp.filters', 'sandManApp.services',
    'sandManApp.controllers', 'sandManApp.directives'], function($stateProvider, $urlRouterProvider){

    $urlRouterProvider.otherwise('/login');

    $stateProvider

        .state('login', {
            url: '/login',
            templateUrl: 'static/js/templates/login.html'
        })

//        .state('home', {
//            url: '/home',
//            templateUrl: 'static/js/templates/layout.html'
//        })
//
        .state('patient-view', {
            url: '/patient-view/:source',
            templateUrl: 'static/js/templates/patient-view.html',
            authenticate: true
        })

        .state('practitioner-view', {
            url: '/practitioner-view',
            templateUrl: 'static/js/templates/practitioner-view.html',
            authenticate: true
        })

        .state('practitioner-search', {
            url: '/practitioner-search',
            templateUrl: 'static/js/templates/practitioner-search.html',
            authenticate: true
        })

        .state('patient-search', {
            url: '/patient-search',
            templateUrl: 'static/js/templates/patient-search.html',
            authenticate: true
        })

        .state('launch-scenario', {
            url: '/launch-scenario',
            templateUrl: 'static/js/templates/launch-scenario.html',
            authenticate: true
        })

        .state('patient', {
            url: '/patient',
            templateUrl: 'static/js/templates/patient.html',
            authenticate: true
        })

        .state('practitioner', {
            url: '/practitioner',
            templateUrl: 'static/js/templates/practitioner.html',
            authenticate: true
        })

        .state('users', {
            url: '/users',
            templateUrl: 'static/js/templates/users.html',
            authenticate: true
        })

        .state('launch', {
            url: '/launch',
            templateUrl: 'static/js/templates/launch.html',
            authenticate: true
        })

        .state('apps', {
            url: '/apps/:source/:action',
            templateUrl: 'static/js/templates/apps-launcher.html',
            authenticate: true
        })

        .state('after-auth', {
            url: '/after-auth',
            templateUrl:'static/js/templates/start.html'
        });

});
