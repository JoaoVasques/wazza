
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
  'LoginLogoutService',
  'DateModel',
  function (
    $scope,
    LoginLogoutService,
    DateModel
  ) {

     $scope.logout = function(){
       LoginLogoutService.logout();
     };


        $scope.format = 'dd-MMMM-yyyy';

        $scope.today = function() {
          DateModel.initDateInterval();
          $scope.beginDate = DateModel.startDate;
          $scope.endDate = DateModel.endDate;
        };
        $scope.today();

        // Disable weekend selection
        $scope.disabled = function(date, mode) {
          return ( mode === 'day' && ( date.getDay() === 0 || date.getDay() === 6 ) );
        };

        $scope.toggleMin = function() {
          $scope.minDate = moment().subtract('years', 1).format('d-M-YYYY');
          $scope.endDateMin = $scope.beginDate;
        };
        $scope.toggleMin();

        $scope.updateEndDateMin = function(){
          $scope.endDateMin = $scope.beginDate;
        };

        $scope.maxDate = new Date();

        $scope.openBeginDate = function($event) {
          $event.preventDefault();
          $event.stopPropagation();

          $scope.beginDateOpened = true;
        };

        $scope.openEndDate = function($event) {
          $event.preventDefault();
          $event.stopPropagation();

          $scope.endDateOpened = true;
        };

        $scope.initDate = $scope.today;


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
