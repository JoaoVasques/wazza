dashboard.controller('OverviewController',[
  '$scope',
  '$state',
  'OverviewInitService',
  'AppOverviewModel',
  'GetKPIService',
  'DateModel',
  'ApplicationStateService',
  '$q',
  'UserVoiceService',
  'OverviewUpdateValuesOnDateChange',
   function(
    $scope,
    $state,
    OverviewInitService,
    AppOverviewModel,
    GetKPIService,
    DateModel,
    ApplicationStateService,
    $q,
    UserVoiceService,
    OverviewUpdateValuesOnDateChange
  ) {

    UserVoiceService.identifyUser();
    ApplicationStateService.setPath("Overview");
    ApplicationStateService.updateApplicationName("");

    $scope.applications = [];
    var noImageUrl = "assets/images/default-user-icon-profile.png";

    fetchCompanyName = function(){
      OverviewInitService.getCompany()
        .then(function(results){
          var company = results.data.name;
          ApplicationStateService.updateCompanyName(company);
          fetchApplications();
        });
    }

    fetchApplications = function(){
      OverviewInitService.getApplications()
      .then(function(results) {
        var names = [];
        _.each(results.data, function(appInfo) {
          $scope.applications.push(new AppOverviewModel(
            appInfo.name,
            (appInfo.url == '') ? noImageUrl : appInfo.url,
            appInfo.platforms,
            appInfo.paymentSystems
          ));
          names.push(appInfo.name);
        });
        ApplicationStateService.updateApps(results.data);
        ApplicationStateService.updateApplicationsList(names);
        fetchKPIs();
      })
    }

    fetchKPIs = function(){
        var companyName = ApplicationStateService.getCompanyName();
        var revenue = "revenue";
        var ltv = "ltv";
        var arpu = "arpu";
        var start = DateModel.formatDate(DateModel.startDate);
        var end = DateModel.formatDate(DateModel.endDate);
        var defaultPlatforms = ["Android", "iOS"];
        
        _.each($scope.applications, function(app) {
          $q.all([
            GetKPIService.getTotalKpiData(companyName, app.name, start, end, revenue, defaultPlatforms),
            GetKPIService.getTotalKpiData(companyName, app.name, start, end, ltv, defaultPlatforms),
            GetKPIService.getTotalKpiData(companyName, app.name, start, end, arpu, defaultPlatforms)
          ])
          .then(function(res) {
            var extractValue = function(index) {
              return res[index].data.value;
            };
            var DecimalPlaces = 2;
            app.totalRevenue = parseFloat(extractValue(0).toFixed(DecimalPlaces));
            app.ltv = parseFloat(extractValue(1).toFixed(DecimalPlaces));
            app.arpu = parseFloat(extractValue(2).toFixed(DecimalPlaces));
          });
        });

        ApplicationStateService.updateApplicationsOverview($scope.applications);
      }
      
    fetchCompanyName();
  }
]);
