'use strict';


// Declare app level module which depends on filters, and services
angular.module('Wazza', [
    'ui.router',
    'ngRoute',
    'Wazza.controllers',
    'ItemModule',
    'UserModule',
    'SecurityModule',
    'DashboardModule',
    'Wazza.broadcastEvents'
]).

config(function($stateProvider, $urlRouterProvider, $locationProvider){

      $locationProvider.html5Mode(true);

      $urlRouterProvider.when("/home","/home/overview/"); //Default to the dashboard
      $urlRouterProvider.otherwise('/login');

      $stateProvider
        .state('webframe', {
            templateUrl: '/webframe'
        })

        .state('webframe.login', {
            url: "/login",
            templateUrl: '/login',
            controller: 'LoginController'
        })

        .state('home', {
            url: "/home",
            //'abstract': true,
            templateUrl: '/home'
        })

        .state('home.overview', {
            url: "^/overview",
            templateUrl: '/dashboard/overview',
            controller: 'OverviewController'
        })

        .state('home.dashboard', {
            url: "^/dashboard",
            templateUrl: '/dashboard',
            controller: 'DashboardController'
        })

      //analytics
        .state('home.analytics', {
            url: "^/analytics",
            templateUrl: '/dashboard/analytics',
            controller: 'DashboardController'
        })

        .state('home.revenue', {
            url: "^/analytics",
            templateUrl: '/dashboard/analytics',
            controller: 'RevenueController'
        })

        .state('home.arpu', {
            url: "^/analytics",
            templateUrl: '/dashboard/analytics',
            controller: 'ArpuController'
        })

        .state('home.avgRevenueSession', {
            url: "^/analytics",
            templateUrl: '/dashboard/analytics',
            controller: 'AvgRevenueSessionController'
        })

        .state('home.payingUsers', {
            url: "^/analytics",
            templateUrl: '/dashboard/analytics',
            controller: 'AvgRevenueSessionController'
        })

        .state('home.ltv', {
            url: "^/analytics",
            templateUrl: '/dashboard/analytics',
            controller: 'AvgRevenueSessionController'
        })

        .state('home.churn', {
            url: "^/analytics",
            templateUrl: '/dashboard/analytics',
            controller: 'AvgRevenueSessionController'
        })

        .state('home.purchases', {
            url: "^/analytics",
            templateUrl: '/dashboard/analytics',
            controller: 'AvgRevenueSessionController'
        })

        .state('home.avgTime1stPurchase', {
            url: "^/analytics",
            templateUrl: '/dashboard/analytics',
            controller: 'AvgRevenueSessionController'
        })

        .state('home.avgTimebetweenPurchase', {
            url: "^/analytics",
            templateUrl: '/dashboard/analytics',
            controller: 'AvgRevenueSessionController'
        })

      //inventory
        .state('home.inventory', {
            url: "^/inventory",
            templateUrl: '/dashboard/inventory',
            controller: 'InventoryController'
        })

        .state('home.inventory.crud', {
            url: "^/inventory",
            templateUrl: '/dashboard/inventory/crud',
            controller: 'InventoryController'
        })

        .state('home.inventory.virtualCurrencies', {
            url: "^/inventory",
            templateUrl: '/dashboard/inventory/virtualCurrencies',
            controller: 'InventoryController'
        })

      //users
        .state('home.user', {
            url: "^/user",
            templateUrl : '/user',
            controller : ''
        })

        .state('webframe.newuser', {
            url: "^/register",
            templateUrl: '/user/register',
            controller: 'UserRegistrationController'
        })

      //items
        .state('home.newitem', {
            url: "^/newItem",
            templateUrl: '/app/item/new/',
            controller: 'NewItemController'
        })

      //applications
        .state('home.newapp', {
            url: "^/newApp",
            templateUrl : '/app/new',
            controller : ''
        })

      //settings
        .state('home.settings', {
            url: "^/settings",
            templateUrl: '/dashboard/settings',
            controller: 'DashboardController'
        })

      //not available yet
        .state('home.notavailableyet', {
            url: "^/notavailableyet",
            templateUrl : '/notavailableyet',
            controller: 'NotAvailableYetController'
        })

    });

/*    leftovers

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

      //campaigns
      .when('/campaigns', {
        templateUrl: '/dashboard/campaigns',
        controller: 'CampaignsController'
      })

*/
