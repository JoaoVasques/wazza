dashboard.directive('kpi', ['$location', function($location) {
    return {
      restrict: 'E',
      scope: {
        kpi: '=info'
      },
      controller: function($scope) {
        $scope.switchDetailedView = function(url) {
          $location.path(url);
        }
      },
      template:
          '<div class="col-md-4 col-lg-4">'+
            '<div class="panel kpi">' +
             '<div class="panel-heading">' +
              '<h2>{{kpi.name}}</h2>' +
              '<hr>'+
             '</div>'+
             '<div class="row">'+
              '<div class="col-md-6">'+
               '<div class="kpi value">{{kpi.unit}}{{kpi.value}}</div>'+
              '</div>'+
             '<div class="col-md-6">'+
              '<div ng-class="kpi.css">'+
               '<span ng-class="kpi.icon"></span> {{kpi.delta}}%'+
              '</div>'+
             '</div>'+
             '<div class="col-md-12 kpi link">'+
              '<button type="button" ng-click="switchDetailedView(kpi.link)" class="btn btn-lg btn-info">More info</button>'+
             '</div>'+
           '</div>'+
           '</div>'+
           '</div>'
      
    };
}]);

