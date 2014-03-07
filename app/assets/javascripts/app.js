'use strict';


// Declare app level module which depends on filters, and services
angular.module('Wazza', ['ngRoute', 'Wazza.controllers', 'ItemModule', 'UserModule', 'SecurityModule', 'DashboardModule']).
  config([ '$routeProvider', '$locationProvider' , function($routeProvider, $locationProvider) {
    $routeProvider
      .when('/', {
        templateUrl: '/launch'
      })
      .when('/login', {
        templateUrl: '/login',
        controller: 'LoginController'
      })
      .when('/home', {
        templateUrl: '/dashboard',
        controller: 'DashboardController'
      })
      .when('/analytics', {
        templateUrl: '/dashboard/analytics',
        controller: ''
      })      
      .when('/register', {
        templateUrl: '/user/register',
        controller: 'UserRegistrationController'
      })
      .when('/newapp', {
        templateUrl : '/app/new',
        controller : ''
      })
      .when('/item/create', {
        templateUrl: '/app/item/new/',
        controller: 'NewItemController'
      })
      .when('/dss', {
        redirectTo: '/'
      });

    $locationProvider.html5Mode(true);
    }
  ])
;
