'use strict';


// Declare app level module which depends on filters, and services
angular.module('Wazza', ['ngRoute', 'Wazza.controllers', 'ItemModule', 'UserModule', 'SecurityModule']).
  config([ '$routeProvider', '$locationProvider' , function($routeProvider, $locationProvider) {
    $routeProvider
      .when('/', {
        templateUrl: '/login',
        controller: 'LoginController'
      })
      .when('/home', {
        templateUrl: '/dashboard',
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
      .when('/item/create/:applicationId', {
        templateUrl: '/app/item/new/Android',
        controller: 'NewItemController'
      });

    $locationProvider.html5Mode(true);
    }
  ])
;
