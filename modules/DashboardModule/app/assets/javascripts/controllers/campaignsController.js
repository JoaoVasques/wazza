'use strict';

dashboard
.controller('CampaignsController', [
  '$scope',
  '$rootScope',
  'FetchItemsService',
  'BootstrapDashboardService',
  'DeleteItemService',
  'ApplicationStateService',
  'ItemSearchService',
  function (
        $scope,
        $rootScope,
        FetchItemsService,
        BootstrapDashboardService,
        DeleteItemService,
        ApplicationStateService,
        ItemSearchService
    ) {

        ApplicationStateService.setPath("Campaigns");

}])
