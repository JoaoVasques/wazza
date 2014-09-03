'use strict';


// Declare app level module which depends on filters, and services
angular.module('Wazza', ['ui.router', 'ngRoute', 'Wazza.controllers', 'ItemModule', 'UserModule', 'SecurityModule', 'DashboardModule', 'n3-line-chart']).

config(function($stateProvider, $urlRouterProvider, $locationProvider){
      
      $locationProvider.html5Mode(true);

      $urlRouterProvider.when("/home","/home/dashboard/"); //Default to the dashboard
      // For any unmatched url, send to /dashboard
      /*$urlRouterProvider.otherwise(function($injector, $location){
        if($scope.authOK === false)
            $state.to("login");
        else
            $state.to("home.dashboard");
      });
      */
      $urlRouterProvider.otherwise('/login');

      $stateProvider
        .state('login', {
            url: "/login",
            templateUrl: '/login',
            controller: 'LoginController'
        })

        .state('home', {
            url: "/home",
            templateUrl: '/home',
            controller: 'DashboardController'
        })

        .state('home.overview', {
            url: "^/overview",
            templateUrl: '/overview',
            controller: 'DashboardController'
        })

        .state('home.dashboard', {
            url: "^/overview",
            templateUrl: '/dashboard',
            controller: 'DashboardController'
        })

        .state('home.analytics', {
            url: "^/analytics",
            templateUrl: '/home',
            controller: 'DashboardController'
        })

/*
        .state('home.applications', {
            url: "^/applications",
            views: {
              'sidebar': {
                templateUrl: '/dashboard/sidebar'
//                controller: function($scope){ ... controller stuff just for sidebar view ... }
              },
              'content': {
                templateUrl: '/dashboard/application',
                controller: 'ApplicationController'
              }
            }
        })
*/

        .state('home.inventory', {
            url: "^/dashboard",
            views: {
              'sidebar': {
                templateUrl: '/dashboard/sidebar'
//                controller: function($scope){ ... controller stuff just for sidebar view ... }
              },
              'content': {
                templateUrl: '/dashboard',
                controller: 'DashboardController'
              }
            }
        })

        .state('home.notavailableyet', {
            url: "^/notavailableyet",
            templateUrl : '/notavailableyet'
        })

    });

/*
  config([ '$locationProvider' , function($locationProvider) {
    $routeProvider
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


    $locationProvider.html5Mode(true);
    }
  ])
;
*/