'use strict';


// Declare app level module which depends on filters, and services
angular.module('Wazza', ['ui.router', 'ngRoute', 'Wazza.controllers', 'ItemModule', 'UserModule', 'SecurityModule', 'DashboardModule', 'n3-line-chart']).

config(function($stateProvider, $urlRouterProvider){
      
      // For any unmatched url, send to /dashboard
      $urlRouterProvider.otherwise("/dashboard")
      
      $stateProvider
        .state('dashboard', {
            url: "/dashboard",
            templateUrl: '/dashboard',
            controller: 'DashboardController'
        })
          .state('revenue', {
              url: "/revenue",
              templateUrl: '/dashboard/revenue',
              controller: 'RevenueController'
          })

    }).


  config([ '$locationProvider' , function($locationProvider) {
   /* $routeProvider
      .when('/', {
        templateUrl: '/dashboard',
        controller: 'DashboardController'
      })
      //analytics
      .when('/churn', {
        templateUrl: '/dashboard/churn',
        controller: 'ChurnController'
      })
      .when('/revenue', {
        templateUrl: '/dashboard/revenue',
        controller: 'RevenueController'
      })
      .when('/arpu', {
        templateUrl: '/dashboard/arpu',
        controller: 'ArpuController'
      })
      .when('/ltv', {
        templateUrl: '/dashboard/ltv',
        controller: 'ltvController'
      })
      //inventory
      .when('/inventory', {
        templateUrl: '/dashboard/inventory',
        controller: 'InventoryController'
      })
      .when('/inventory/crud', {
        templateUrl: '/dashboard/inventory/crud',
        controller: 'InventoryController'
      })
      .when('/inventory/virtualCurrencies', {
        templateUrl: '/dashboard/inventory/virtualCurrencies',
        controller: 'InventoryController'
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
      //settings
      .when('/settings', {
        templateUrl: '/dashboard/settings',
        controller: 'DashboardController'
      })
      //campaigns
      .when('/campaigns', {
        templateUrl: '/dashboard/campaigns',
        controller: 'CampaignsController'
      })
      //TODO
      .when('/notavailableyet', {
        templateUrl : '/notavailableyet'
      })
      .otherwise({redirectTo:"/"});

*/
    $locationProvider.html5Mode(true);
    }
  ])
;
