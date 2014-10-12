dashboard.controller('OverviewController',[
  '$scope',
  '$state',
  'OverviewInitService',
  'AppOverviewModel',
  'GetMainKPIsService',
  'DateModel',
  'ApplicationStateService',
  '$q',
  function(
    $scope,
    $state,
    OverviewInitService,
    AppOverviewModel,
    GetMainKPIsService,
    DateModel,
    ApplicationStateService,
    $q
  ) {

    ApplicationStateService.setPath("Overview");
    ApplicationStateService.updateApplicationName("");

    $scope.applications = [];
    var noImageUrl = "assets/images/default-user-icon-profile.png";

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
      .then(function(){
        var companyName = ApplicationStateService.companyName;
        var revenue = "revenue";
        var ltv = "ltv";
        var arpu = "arpu";
        var start = DateModel.formatDate(DateModel.startDate);
        var end = DateModel.formatDate(DateModel.endDate);
        
        _.each($scope.applications, function(app) {
          $q.all([
            GetMainKPIsService.getTotalKpiData(companyName, app.name, start, end, revenue),
            GetMainKPIsService.getTotalKpiData(companyName, app.name, start, end, ltv),
            GetMainKPIsService.getTotalKpiData(companyName, app.name, start, end, arpu)
          ])
          .then(function(res) {
            var extractValue = function(index) {
              return res[index].data.value;
            };
            app.totalRevenue = extractValue(0);
            app.ltv = extractValue(1);
            app.arpu = extractValue(2);
          });
        });
      });
  }
]);

