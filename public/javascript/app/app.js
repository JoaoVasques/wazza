'use strict';

// Declare app level module which depends on filters, and services
angular.module('Wazza', [
    'ui.router',
    'ngRoute',
    'duScroll',
    'chart.js',
    'ngResource',
    'LocalStorageModule',
    'Wazza.controllers',
    'ItemModule',
    'UserModule',
    'SecurityModule',
    'DashboardModule',
    'ChartModule',
    'Wazza.broadcastEvents'
]).

config(function($stateProvider, $urlRouterProvider, $locationProvider, $httpProvider, localStorageServiceProvider){

      $locationProvider.html5Mode(true);

      $urlRouterProvider.when("/home","/home/overview/"); //Default to the overview
      $urlRouterProvider.otherwise('/');

      $stateProvider
        // .state('webframe', {
        //     templateUrl: '/webframe'
        // })

        .state('login', {
            url: "/",
            templateUrl: '/login',
            controller: 'LoginController'
        })

        .state('home', {
            templateUrl: '/home'
        })

      //not available yet
        .state('home.notavailableyet', {
            url: "^/",
            templateUrl : '/notavailableyet',
            controller: 'NotAvailableYetController'
        })

      //users
        .state('home.user', {
            url: "^/",
            templateUrl : '/user',
            controller : ''
        })

        .state('newuser', {
            url: "^/",
            templateUrl: '/user/register',
            controller: 'UserRegistrationController'
        })

      //items
        // .state('home.newitem', {
        //     url: "^/",
        //     templateUrl: '/app/item/new/',
        //     controller: 'NewItemController'
        // })

      //inventory
        // .state('home.inventory', {
        //     url: "^/",
        //     templateUrl: '/dashboard/inventory',
        //     controller: 'InventoryController'
        // })

        // .state('home.inventory.crud', {
        //     url: "^/",
        //     templateUrl: '/dashboard/inventory/crud',
        //     controller: 'InventoryController'
        // })

        // .state('home.inventory.virtualCurrencies', {
        //     url: "^/",
        //     templateUrl: '/dashboard/inventory/virtualCurrencies',
        //     controller: 'InventoryController'
        // })

      //analytics
        .state('analytics', {
            templateUrl: '/analyticsframe',
            controller: 'AnalyticsController',
            abstract: true
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

      //analytics - metrics
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

        .state('analytics.sessionsFirstPurchase', {
            url: "^/",
            templateUrl: '/dashboard/analytics',
            controller: 'NumberSessionsFirstPurchaseController'
        })

        .state('analytics.sessionsBetweenPurchase', {
            url: "^/",
            templateUrl: '/dashboard/analytics',
            controller: 'NumberSessionsBetweenPurchaseController'
        })

      //applications
        .state('analytics.newapp', {
            url: "^/",
            templateUrl : '/app/new',
            controller : 'NewApplicationFormController'
        })

      //settings
        .state('analytics.settings', {
            url: "^/",
            templateUrl: '/dashboard/settings',
            controller: 'SettingsController'
        })

      //random
        .state('analytics.terms', {
            url: "^/",
            templateUrl: '/terms'
        })

        .state('terms', {
            url: "^/",
            templateUrl: '/terms'
        })

        .state('privacy', {
            url: "^/",
            templateUrl: '/privacy'
        })

    $httpProvider.interceptors.push('SecurityHttpInterceptor');

    //local storage
    localStorageServiceProvider.setPrefix('wazza');

    });
