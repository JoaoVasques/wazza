'use strict';

dashboard.controller('OverviewController', [
  '$scope',
  '$location',
  '$rootScope',
  "$anchorScroll",
  'FetchItemsService',
  'BootstrapDashboardService',
  'DeleteItemService',
  'ApplicationStateService',
  'ItemSearchService',
  'TopbarService',
  'GetMainKPIsService',
  'KpiData',
  "KpiModel",
  "AnchorSmoothScroll",
  function (
    $scope,
    $location,
    $rootScope,
    $anchorScroll,
    FetchItemsService,
    BootstrapDashboardService,
    DeleteItemService,
    ApplicationStateService,
    ItemSearchService,
    TopbarService,
    GetMainKPIsService,
    KpiData,
    KpiModel,
    AnchorSmoothScroll
  ) {

    TopbarService.setName("Overview");


        var bootstrapSuccessCallback = function (data) {
            var push = function (origin, destination) {
                _.each(origin, function (el) {
                    destination.push(el);
                });
            };

            angular.extend($scope.credentials, data.data.credentials);
            push(data.data.virtualCurrencies, $scope.virtualCurrencies);
            push(data.data.items, $scope.items);
            push(
                _.map(data.data.applications, function (element) {
                    return element.name;
                }),
                $scope.applications
            );
            ApplicationStateService.updateApplicationName(_.first(data.data.applications).name);
            ApplicationStateService.updateUserInfo(data.data.userInfo);

            ApplicationStateService.updateApplicationsList(
              _.map(data.data.applications, function(app) {
                  return app.name;
              })
            );

            ApplicationStateService.updateCompanyName(data.data.companyName);
        };

        var bootstrapFailureCallback = function (errorData) {
            console.log(errorData);
        };

        $scope.bootstrapModule = function () {
            $scope.applicationName = "";
            $scope.applications = [];
            $scope.credentials = {};
            $scope.virtualCurrencies = [];
            $scope.items = [];
            $scope.isCollapsed = true;
            $scope.$on("ITEM_SEARCH_EVENT", function () {
                $scope.itemSearch = ItemSearchService.searchData
            });
            $scope.$on("APPLICATION_NAME_UPDATED", function () {
                $scope.applicationName = ApplicationStateService.applicationName;
            });

            $scope.$on("APPLICATIONS_LIST_UPDATED", function() {
                $scope.applications = ApplicationStateService.applicationsList;
            });

            BootstrapDashboardService.execute()
                .then(
                    bootstrapSuccessCallback,
                    bootstrapFailureCallback);
        };

        $scope.bootstrapModule();

    }]
);
