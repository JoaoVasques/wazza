'use strict';


// Declare app level module which depends on filters, and services
angular.module('Wazza', ['ngRoute', 'Wazza.controllers', 'ItemModule']).
  config([ '$routeProvider', '$locationProvider' , function($routeProvider, $locationProvider) {
    $routeProvider
      .when('/xpto', {
        templateUrl: '/splash',
        controller: ''
      })
      .when('/home', {
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
