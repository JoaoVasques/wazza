
var application = angular.module('Wazza.controllers', [
    'ApplicationModule',
    'Wazza.services',
    'ItemModule',
    'ngCookies',
    'SecurityModule',
    'DashboardModule',
    'ui.bootstrap'
])

//TODO: delete these whenever the refactor is complete
.controller('NavBarController',[
  '$scope',
  function (
    $scope
  ) {

}])

.controller('SidebarController', [
  '$scope',
  '$rootScope',
   function($scope, $rootScope) {
    
    $scope.selectDashboardSection = function(sectionId) {
      $rootScope.$broadcast('ChangeDashboardSection', {section: sectionId});
    };

}])

.controller('NotAvailableYetController', [
  '$scope',
  '$rootScope',
  'TopbarService',
   function(
    $scope,
    $rootScope,
    TopbarService
   ) {
    TopbarService.setName("Not available yet :(");


}])

;
