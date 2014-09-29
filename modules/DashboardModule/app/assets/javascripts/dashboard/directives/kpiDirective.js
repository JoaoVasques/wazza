dashboard.directive('kpi', ['$state', function($state) {
    return {
      restrict: 'E',
      scope: {
        kpi: '=info'
      },
      controller: function($scope) {
        $scope.switchDetailedView = function(state) {
          $state.go(state);
        }
      },
      template:
      '<div class="col-md-4 col-lg-4">'+
        '<div style="margin:10px, width:inherit" class="well profile glow" ng-click="switchDetailedView(kpi.link)">' +
            '<div style="200px">' +
                '<h1>{{kpi.name}}</h1>' +
            '</div>' +
            '<div class="divider text-center">' +
                '<div class="col-xs-12 col-sm-6 emphasis">' +
                    '<h2><strong>{{kpi.unit}} {{kpi.value}} </strong></h2>' +
                    '<p><small>Total</small></p>' +
                '</div>' +
                '<div class="col-xs-12 col-sm-6 emphasis">' +
                    '<h2><strong>{{kpi.delta}} % </strong></h2>' +
                    '<p><small>Delta</small></p>' +
                '</div>' +
            '</div>' +
          '</div>' +
      '</div>'
    };
}]);
