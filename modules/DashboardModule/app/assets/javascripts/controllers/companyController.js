'use strict';

dashboard
.controller('CompanyController', [
  '$scope',
  '$location',
  '$rootScope',
  'FetchItemsService',
  'BootstrapDashboardService',
  'DeleteItemService',
  'ApplicationStateService',
  'ItemSearchService',
  'TopbarService',
  function (
        $scope,
        $location,
        $rootScope,
        FetchItemsService,
        BootstrapDashboardService,
        DeleteItemService,
        ApplicationStateService,
        ItemSearchService,
        TopbarService
    ) {

        TopbarService.setName("Applications");

        $scope.addItem = function () {
            $location.path("/item/create");
        };

        $scope.itemDeleteSucessCallback = function (data) {
            $scope.items = _.without($scope.items, _.findWhere($scope.items, {
                _id: data.data
            }));
        }

        /** TODO: show error message **/
        $scope.itemDeleteFailureCallback = function (data) {}

        $scope.deleteItem = function (id, image) {
            DeleteItemService(id, $scope.applicationName, image)
                .then(
                    $scope.itemDeleteSucessCallback,
                    $scope.itemDeleteFailureCallback
            );
        };

        $scope.successCallback = function (data) {
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
        }

        $scope.failureCallback = function (errorData) {
            console.log(errorData);
        }

            $scope.applicationName = "";
            $scope.applications = [];
            $scope.credentials = {};
            $scope.virtualCurrencies = [];
            $scope.items = [];

            $scope.$on("ITEM_SEARCH_EVENT", function () {
                $scope.itemSearch = ItemSearchService.searchData
            });
            $scope.$on("APPLICATION_NAME_UPDATED", function () {
                $scope.applicationName = ApplicationStateService.applicationName;
            });
            BootstrapDashboardService.execute()
                .then(
                    $scope.successCallback,
                    $scope.failureCallback);

}])
