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
            templateUrl: '/home'
        })

        .state('analytics', {
            templateUrl: '/analyticsframe',
            controller: 'AnalyticsController'
        })

        .state('analytics.overview', {
            url: "^/overview",
            templateUrl: '/dashboard/overview',
            controller: 'OverviewController'
        })

        .state('analytics.dashboard', {
            url: "^/dashboard",
            templateUrl: '/dashboard',
            controller: 'DashboardController'
        })

      //analytics
        .state('analytics.revenue', {
            url: "^/analytics",
            templateUrl: '/dashboard/analytics',
            controller: 'RevenueController'
        })

        .state('analytics.arpu', {
            url: "^/analytics",
            templateUrl: '/dashboard/analytics',
            controller: 'ArpuController'
        })

        .state('analytics.avgRevenueSession', {
            url: "^/analytics",
            templateUrl: '/dashboard/analytics',
            controller: 'AvgRevenueSessionController'
        })

        .state('analytics.payingUsers', {
            url: "^/analytics",
            templateUrl: '/dashboard/analytics',
            controller: 'AvgRevenueSessionController'
        })

        .state('analytics.ltv', {
            url: "^/analytics",
            templateUrl: '/dashboard/analytics',
            controller: 'AvgRevenueSessionController'
        })

        .state('analytics.churn', {
            url: "^/analytics",
            templateUrl: '/dashboard/analytics',
            controller: 'AvgRevenueSessionController'
        })

        .state('analytics.purchases', {
            url: "^/analytics",
            templateUrl: '/dashboard/analytics',
            controller: 'AvgRevenueSessionController'
        })

        .state('analytics.avgTime1stPurchase', {
            url: "^/analytics",
            templateUrl: '/dashboard/analytics',
            controller: 'AverageTimeBetweenPurchase'
        })

        .state('analytics.avgTimebetweenPurchase', {
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
