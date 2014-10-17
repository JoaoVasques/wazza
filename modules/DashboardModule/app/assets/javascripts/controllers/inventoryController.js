'use strict';

dashboard
.controller('InventoryController', [
  '$scope',
  '$rootScope',
  'FetchItemsService',
  'BootstrapDashboardService',
  'DeleteItemService',
  'ApplicationStateService',
  'ItemSearchService',
  '$state',
  function (
    $scope,
    $rootScope,
    FetchItemsService,
    BootstrapDashboardService,
    DeleteItemService,
    ApplicationStateService,
    ItemSearchService,
    $state
    ) {

    ApplicationStateService.setPath("Management");

    $scope.addItem = function () {
        $state.go("home.newitem");
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

        push(data.data.virtualCurrencies, $scope.virtualCurrencies);
        push(data.data.items, $scope.items);

    }

    $scope.failureCallback = function (errorData) {
        console.log(errorData);
    }

    $scope.virtualCurrencies = [];
    $scope.items = [];

    $scope.$on("ITEM_SEARCH_EVENT", function () {
        $scope.itemSearch = ItemSearchService.searchData
    });

    BootstrapDashboardService.execute()
    .then(
        $scope.successCallback,
        $scope.failureCallback);

}])
