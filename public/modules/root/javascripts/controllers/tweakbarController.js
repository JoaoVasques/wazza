application.controller('TweakBarController',[
  '$scope',
  'DateModel',
  '$state',
  '$rootScope',
  '$stateParams',
  function (
    $scope,
    DateModel,
    $state,
    $rootScope,
    $stateParams
    ) {

    $scope.userInfo = {
        name: "",
        email: ""
    };

    $scope.today = function() {
      if(DateModel.refresh){
        $scope.beginDate = DateModel.min;
        $scope.endDate = DateModel.max;
        DateModel.refresh = false;
      }
      else{
        DateModel.initDateInterval();
        $scope.beginDate = DateModel.startDate;
        $scope.endDate = DateModel.endDate;
      }
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

      //update kpi in a given time range
      if($state.current.name === "analytics.dashboard" || $state.current.name === "analytics.overview"){
        DateModel.refresh = true;
        DateModel.min = $scope.beginDate;
        DateModel.max = $scope.endDate;

        $state.transitionTo($state.current, $stateParams, {
            reload: true,
            inherit: false,
            notify: true
        });
      } else
        $rootScope.$broadcast($state.current.name);
    }
  }]);
