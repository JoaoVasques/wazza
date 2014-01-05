'use strict';


// Declare app level module which depends on filters, and services
angular.module('Wazza', ['ngRoute', 'Wazza.controllers']).
  config([ '$routeProvider', function($routeProvider) {
      $routeProvider.when('/home', {
        templateUrl : '/app/new',
        controller : ''
      })
      .when('/item/create', {
        templateUrl: '/app/item/new/Android',
        controller: ''
      })
      ;
    }
  ])
;
