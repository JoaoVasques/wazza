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
  function(
    $scope,
    $state,
    OverviewInitService,
    AppOverviewModel,
    GetKPIService,
    DateModel,
    ApplicationStateService,
    $q,
    UserVoiceService
  ) {

    UserVoiceService.identifyUser();
    ApplicationStateService.setPath("Overview");
    ApplicationStateService.updateApplicationName("");

    $scope.applications = [];
    var noImageUrl = "assets/images/default-user-icon-profile.png";

    fetchCompanyName = function(){
        OverviewInitService
          .getCompany()
          .then(function(results){
            var company = results.data.name;
            ApplicationStateService.updateCompanyName(company);
          });
    }

    fetchApplications = function(){
      OverviewInitService
      .getApplications()
      .then(function(results) {
        var names = [];
        _.each(results.data, function(appInfo) {
          $scope.applications.push(new AppOverviewModel(
            appInfo.name,
            (appInfo.url == '') ? noImageUrl : appInfo.url,
            appInfo.platforms
          ));
          names.push(appInfo.name);
        });
        ApplicationStateService.updateApplicationsList(names);
      })
    }

    fetchKPIs = function(){
        var companyName = ApplicationStateService.getCompanyName();
        var revenue = "revenue";
        var ltv = "ltv";
        var arpu = "arpu";
        var start = DateModel.formatDate(DateModel.startDate);
        var end = DateModel.formatDate(DateModel.endDate);
        
        _.each($scope.applications, function(app) {
          $q.all([
            GetKPIService.getTotalKpiData(companyName, app.name, start, end, revenue),
            GetKPIService.getTotalKpiData(companyName, app.name, start, end, ltv),
            GetKPIService.getTotalKpiData(companyName, app.name, start, end, arpu)
          ])
          .then(function(res) {
            var extractValue = function(index) {
              return res[index].data.value;
            };
            app.totalRevenue = numeral(extractValue(0)).format('0');
            app.ltv = numeral(extractValue(1)).format('0')
            app.arpu = numeral(extractValue(2)).format('0')
          });
        });

        ApplicationStateService.updateApplicationsOverview($scope.applications);
      }

    bootstrap = function(){
      if(ApplicationStateService.getCompanyName() === "")
        fetchCompanyName();

      if(ApplicationStateService.getApplicationsList().length === 0)
        fetchApplications();
      else
        $scope.applications = ApplicationStateService.getApplicationsOverview();

      fetchKPIs();
    }

    bootstrap();
  }
]);
