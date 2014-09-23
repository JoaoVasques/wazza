'use strict'

/**var overviewController = angular.module('Dashboard.overviewControllers', ['DashboardModule.overviewServices']);
**/

dashboard.controller('OverviewController',[
  '$scope',
  '$location',
  '$state',
  'OverviewInitService',
  'AppOverviewModel',
  function(
    $scope,
    $location,
    $state,
    OverviewInitService,
    AppOverviewModel
  ) {

    $scope.applications = [];
    var noImageUrl = "http://www.localcrimenews.com/wp-content/uploads/2013/07/default-user-icon-profile.png";
    OverviewInitService.getApplications().then(
      function(results) {
        _.each(results.data, function(appInfo) {
          $scope.applications.push(new AppOverviewModel(
            appInfo.name,
            (appInfo.url == '') ? noImageUrl : appInfo.url,
            appInfo.platforms
          ));
        });
      }
    )
  }
]);

