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
      .when('/index2',{
        templateUrl: '/dashboard',
        controller: 'RedirectController'
      })
      //inventory
      .when('/inventory', {
        templateUrl: '/dashboard/inventory',
        controller: 'DashboardController'
      })
      .when('/inventory/crud', {
        templateUrl: '/dashboard/inventory/crud',
        controller: 'DashboardController'
      })
      .when('/inventory/virtualCurrencies', {
        templateUrl: '/dashboard/inventory/virtualCurrencies',
        controller: 'DashboardController'
      })
      //stores
      .when('/store/android', {
        templateUrl: '/dashboard/store/android',
        controller: 'DashboardController'
      })
      .when('/store/apple', {
        templateUrl: '/dashboard/store/apple',
        controller: 'DashboardController'
      })
      .when('/store/amazon', {
        templateUrl: '/dashboard/store/amazon',
        controller: 'DashboardController'
      })
      //analytics
      .when('/analytics', {
        templateUrl: '/dashboard/analytics',
        controller: ''
      })
      //settings
      .when('/settings', {
        templateUrl: '/dashboard/settings',
        controller: 'DashboardController'
      })
      //users
      .when('/register', {
        templateUrl: '/user/register',
        controller: 'UserRegistrationController'
      })
      .when('/item/create', {
        templateUrl: '/app/item/new/',
        controller: 'NewItemController'
      })
      //applications
      .when('/newapp', {
        templateUrl : '/app/new',
        controller : ''
      })
      //user
      .when('/user', {
        templateUrl : '/user',
        controller : ''
      })
      .otherwise({redirectTo:"/"});


    $locationProvider.html5Mode(true);
    }
  ])
;