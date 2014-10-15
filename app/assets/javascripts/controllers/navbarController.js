application.controller('NavBarController',[
  '$scope',
  'DateModel',
  '$state',
  '$rootScope',
  function (
    $scope,
    DateModel,
    $state,
    $rootScope
    ) {

    $scope.today = function() {
      DateModel.initDateInterval();
      $scope.beginDate = DateModel.startDate;
      $scope.endDate = DateModel.endDate;
    };

    $scope.toggleMin = function() {
      $scope.minDate = moment().subtract(1, 'years').format('d-M-YYYY');
      $scope.endDateMin = $scope.beginDate;
    };

    $scope.updateEndDateMin = function(){
      $scope.endDateMin = $scope.beginDate;
    };

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

    $scope.format = 'dd-MMM-yyyy';
    $scope.today();
    $scope.toggleMin();
    $scope.maxDate = new Date();
    $scope.initDate = $scope.today;

    $scope.updateKPIs = function() {
      DateModel.startDate = $scope.beginDate;
      DateModel.endDate = $scope.endDate;
      $rootScope.$broadcast($state.current.name);
    }
  }]);
