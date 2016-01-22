'use strict';

angular.module('sandManApp', ['ui.router', 'ngSanitize', 'sandManApp.filters', 'sandManApp.services',
    'sandManApp.controllers', 'sandManApp.directives'], function($stateProvider, $urlRouterProvider){

    $urlRouterProvider.otherwise('/home');

    $stateProvider

        .state('home', {
            url: '/home',
            templateUrl: 'static/sandbox-manager/js/templates/home.html'
        })

        .state('patient', {
            url: '/patient',
            templateUrl: 'static/sandbox-manager/js/templates/patient.html',
            authenticate: true
        })

        .state('practitioner', {
            url: '/practitioner',
            templateUrl: 'static/sandbox-manager/js/templates/practitioner.html',
            authenticate: true

        })

        .state('organization', {
            url: '/organization',
            templateUrl: 'static/sandbox-manager/js/templates/organization.html',
            authenticate: true

        })

        .state('after-auth', {
            url: '/after-auth',
            templateUrl:'static/sandbox-manager/js/templates/start.html'
        });

});
