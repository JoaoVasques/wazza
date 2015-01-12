'use strict';


// Declare app level module which depends on filters, and services
angular.module('Wazza', [
    'ui.router',
    'LocalStorageModule',
    'ngRoute',
    'Wazza.controllers',
    'ItemModule',
    'UserModule',
    'SecurityModule',
    'DashboardModule',
    'Wazza.broadcastEvents',
    'duScroll'
]).

config(function($stateProvider, $urlRouterProvider, $locationProvider, $httpProvider, localStorageServiceProvider){

      $locationProvider.html5Mode(true);

      $urlRouterProvider.when("/home","/home/overview/"); //Default to the overview
      $urlRouterProvider.otherwise('/');

      $stateProvider
        .state('webframe', {
            templateUrl: '/webframe'
        })

        .state('webframe.login', {
            url: "/",
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
            url: "^/",
            templateUrl: '/dashboard/overview',
            controller: 'OverviewController'
        })

        .state('analytics.dashboard', {
            url: "^/",
            templateUrl: '/dashboard',
            controller: 'DashboardController'
        })

      //analytics
        .state('analytics.revenue', {
            url: "^/",
            templateUrl: '/dashboard/analytics',
            controller: 'RevenueController'
        })

        .state('analytics.arpu', {
            url: "^/",
            templateUrl: '/dashboard/analytics',
            controller: 'ArpuController'
        })

        .state('analytics.avgRevenueSession', {
            url: "^/",
            templateUrl: '/dashboard/analytics',
            controller: 'AvgRevenueSessionController'
        })

        .state('analytics.payingUsers', {
            url: "^/",
            templateUrl: '/dashboard/analytics',
            controller: 'PayingUsersController'
        })

        .state('analytics.ltv', {
            url: "^/",
            templateUrl: '/dashboard/analytics',
            controller: 'LifeTimeValueController'
        })

        .state('analytics.avgPurchasesUser', {
            url: "^/",
            templateUrl: '/dashboard/analytics',
            controller: 'AvgPurchasesPerUserController'
        })

        .state('analytics.purchasesPerSession', {
            url: "^/",
            templateUrl: '/dashboard/analytics',
            controller: 'PurchasePerSessionController'
        })

        .state('analytics.avgTime1stPurchase', {
            url: "^/",
            templateUrl: '/dashboard/analytics',
            controller: 'AverageTimeFirstPurchaseController'
        })

        .state('analytics.avgTimebetweenPurchase', {
            url: "^/",
            templateUrl: '/dashboard/analytics',
            controller: 'AverageTimeBetweenPurchaseController'
        })

      //inventory
        .state('home.inventory', {
            url: "^/",
            templateUrl: '/dashboard/inventory',
            controller: 'InventoryController'
        })

        .state('home.inventory.crud', {
            url: "^/",
            templateUrl: '/dashboard/inventory/crud',
            controller: 'InventoryController'
        })

        .state('home.inventory.virtualCurrencies', {
            url: "^/",
            templateUrl: '/dashboard/inventory/virtualCurrencies',
            controller: 'InventoryController'
        })

      //users
        .state('home.user', {
            url: "^/",
            templateUrl : '/user',
            controller : ''
        })

        .state('webframe.newuser', {
            url: "^/",
            templateUrl: '/user/register',
            controller: 'UserRegistrationController'
        })

      //items
        .state('home.newitem', {
            url: "^/",
            templateUrl: '/app/item/new/',
            controller: 'NewItemController'
        })

      //applications
        .state('home.newapp', {
            url: "^/",
            templateUrl : '/app/new',
            controller : 'NewApplicationFormController'
        })

      //settings
        .state('home.settings', {
            url: "^/",
            templateUrl: '/dashboard/settings',
            controller: 'SettingsController'
        })

      //not available yet
        .state('home.notavailableyet', {
            url: "^/",
            templateUrl : '/notavailableyet',
            controller: 'NotAvailableYetController'
        })

      //random
        .state('webframe.terms', {
            url: "^/",
            templateUrl: '/terms'
        })

        .state('webframe.privacy', {
            url: "^/",
            templateUrl: '/privacy'
        })

    $httpProvider.responseInterceptors.push('SecurityHttpInterceptor');


    //local storage
    localStorageServiceProvider.setPrefix('wazza');

    });
