application.controller('TweakBarController',[
  '$scope',
  'DateModel',
  '$state',
  '$rootScope',
  '$stateParams',
  'ApplicationStateService',
  'SelectedPlatformsChange',
  'DashboardViewChanges',
  'DashboardShowPlatformDetails',
  function (
    $scope,
    DateModel,
    $state,
    $rootScope,
    $stateParams,
    ApplicationStateService,
    SelectedPlatformsChange,
    DashboardViewChanges,
    DashboardShowPlatformDetails
    ) {

    var hideShowBar = ['analytics.newapp', 'analytics.settings'];
    $rootScope.$on('$stateChangeSuccess', function(event, toState, toParams, fromState, fromParams) {
      $scope.showBar = _.find(hideShowBar, function(s) {return s == toState.name;}) == undefined ? true : false;
    });

    $scope.showDashboardViewOptions = false;
    $scope.viewText = "Numerical";
    $scope.showDetailsButton = true;
    $scope.updateView = function(value) {
      $scope.showDetailsButton = (value == 1) ? true: false;
      $scope.viewText = (value == 1) ? "Numerical" : "Visual";
      $rootScope.$broadcast(DashboardViewChanges, {newView: value});
    };
    $scope.hideDetails = true;
    $scope.showDetails = function(){
      $scope.hideDetails = ! $scope.hideDetails;
      $rootScope.$broadcast(DashboardShowPlatformDetails, {value: $scope.hideDetails});
    };

    $rootScope.$on('$stateChangeSuccess',
      function(event, toState, toParams, fromState, fromParams){
        $scope.showDashboardViewOptions = (toState.name != 'analytics.dashboard') ? false : true;
      });
      
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

    $scope.platforms = {
      iOS: true,
      Android: true
    };

    var updatePlatformsCheckboxes = function(){
      var appPlatforms = ApplicationStateService.selectedPlatforms;
      $scope.platforms.iOS = _.contains(appPlatforms, "iOS") ? true : false;
      $scope.platforms.Android = _.contains(appPlatforms, "Android") ? true : false;
    };
    $scope.$on(SelectedPlatformsChange, updatePlatformsCheckboxes);

    var platformWatcher = function(platform, newValue, oldValue) {
      newValue ? ApplicationStateService.addPlatforms(platform) : ApplicationStateService.removePlatform(platform);
      if($state.current.name != "analytics.dashboard" && $state.current.name != "analytics.overview") {
        $rootScope.$broadcast($state.current.name + "-platformChange", {platform: platform, value: newValue});
      }
    }
      
    $scope.$watch("platforms.iOS", function(newValue, oldValue, scope){
      platformWatcher("iOS", newValue, oldValue);
    });

    $scope.$watch("platforms.Android", function(newValue, oldValue, scope){
      platformWatcher("Android", newValue, oldValue);
    });

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
