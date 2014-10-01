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
      templateUrl: '/dashboard/kpi'
    };
}]);
