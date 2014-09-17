'use strict';

var dashboardServices = angular.module('DashboardModule.services', []);

dashboardServices.value("RevenueUrlType", {
    kpiType: "revenue",
    total: "total",
    detailed: ""
});

dashboardServices.value("ArpuUrlType", {
    kpiType: "arpu",
    total: "total",
    detailed: ""
});

dashboardServices.factory('AnchorSmoothScroll', function() {

  var service = {};
  service.scrollTo = function(eID) {

    function currentYPosition() {
      // Firefox, Chrome, Opera, Safari
      if (self.pageYOffset) return self.pageYOffset;
      // Internet Explorer 6 - standards mode
      if (document.documentElement && document.documentElement.scrollTop)
        return document.documentElement.scrollTop;
      // Internet Explorer 6, 7 and 8
      if (document.body.scrollTop) return document.body.scrollTop;
      return 0;
    }

    function elmYPosition(eID) {
      var elm = document.getElementById(eID);
      var y = elm.offsetTop;
      var node = elm;
      while (node.offsetParent && node.offsetParent != document.body) {
        node = node.offsetParent;
        y += node.offsetTop;
      } return y;
    }

    // This scrolling function
    // is from http://www.itnewb.com/tutorial/Creating-the-Smooth-Scroll-Effect-with-JavaScript

    var startY = currentYPosition();
    var stopY = elmYPosition(eID);
    var distance = stopY > startY ? stopY - startY : startY - stopY;
    if (distance < 100) {
      scrollTo(0, stopY); return;
    }
    var speed = Math.round(distance / 100);
    if (speed >= 20) speed = 20;
    var step = Math.round(distance / 25);
    var leapY = stopY > startY ? startY + step : startY - step;
    var timer = 0;
    if (stopY > startY) {
      for ( var i=startY; i<stopY; i+=step ) {
        setTimeout("window.scrollTo(0, "+leapY+")", timer * speed);
        leapY += step; if (leapY > stopY) leapY = stopY; timer++;
      } return;
    }
    for ( var i=startY; i>stopY; i-=step ) {
      setTimeout("window.scrollTo(0, "+leapY+")", timer * speed);
      leapY -= step; if (leapY < stopY) leapY = stopY; timer++;
    }
  };

  return service;
});

dashboardServices.factory('BootstrapDashboardService', ['$http', '$q',
    function ($http, $q) {
        var service = {};

        service.execute = function () {
            var request = $http({
                url: '/dashboard/bootstrap',
                method: 'GET'
            });

            var deferred = $q.defer();
            deferred.resolve(request);
            return deferred.promise;
        };

        return service;
}]);

dashboardServices.factory('GetKPIService', ['$http', '$q',
    function($http, $q) {
      var service = {};

      service.execute = function(companyName, applicationName, startDate, endDate, metric) {
        var buildUrl = function(urlType, subType) {
          return '/analytics/' + urlType + '/' + subType +'/' + companyName + '/' + applicationName + '/'+ startDate +'/' + endDate;
        };

        var getTotal = $http({
            url: buildUrl(metric, 'total'),
            method: 'GET'
        });

		    var getDetailed = $http({
            url: buildUrl(metric, 'detail'),
            method: 'GET'
        });

        return $q.all([getTotal, getDetailed]);
      };

      return service;
}]);

dashboardServices.factory('GetMainKPIsService', ['$http','$q',
  function($http,$q) {
      var service = {};

      var buildUrl = function(companyName, applicationName, urlType, subType, startDate, endDate) {
        return '/analytics/' +
         urlType + '/' +
         subType + '/' +
         companyName + '/' +
         applicationName + '/'+
         startDate + '/' +
         endDate;
      };

      service.execute = function(companyName, applicationName, startDate, endDate) {

        var revUrl = buildUrl(companyName, applicationName, 'revenue', 'total', startDate, endDate);
        var totalRevenue = $http({
            url: revUrl,
            method: 'GET'
        });

        var arpuUrl = buildUrl(companyName, applicationName, 'arpu', 'total', startDate, endDate);
        var totalARPU = $http({
            url: arpuUrl,
            method: 'GET'
        });

        return $q.all([totalRevenue, totalARPU]);
      };

      service.getTotalKpiData = function(companyName, applicationName, start, end, kpiName) {
        var request = $http({
          url: buildUrl(companyName, applicationName, kpiName, "total", start, end),
          method: 'GET'
        });
        
        var deferred = $q.defer();
        deferred.resolve(request);
        return deferred.promise;
      };
      
      service.getDetailedKPIData = function(companyName, applicationName, start, end, kpiName) {
        var request = $http({
          url: buildUrl(companyName, applicationName, kpiName, "detail", start, end),
          method: 'GET'
        });

        var deferred = $q.defer();
        deferred.resolve(request);
        return deferred.promise;
      };

      return service;
}]);

dashboardServices.factory('FetchItemsService', ['$http', '$q',
    function ($http, $q) {
        var service = {};

        service.execute = function (appName, offset) {
            var request = $http({
                url: '/app/api/item/get/' + appName + '/' + offset,
                method: 'GET'
            });

            var deferred = $q.defer();
            deferred.resolve(request);
            return deferred.promise;
        };

        return service;
}]);

dashboardServices.factory('DeleteItemService', ['$http', '$q',
    function ($http, $q) {
        var service = function (id, name, imageName) {
            var request = $http.post("/app/item/delete/" + id, {
                appName: name,
                image: imageName
            });

            var deferred = $q.defer();
            deferred.resolve(request);
            return deferred.promise;
        };

        return service;
}]);
