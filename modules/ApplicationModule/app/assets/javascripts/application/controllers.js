// application module

angular.module('ApplicationModule.controllers', []).
  controller('NewApplicationFormController', ['$scope', function($scope) {

    $scope.applicationType = ""

    $scope.showStoreSection = function(storeType) {
      return storeType == $scope.applicationType
    };

    $scope.hideStoreSection = function(storeType) {
      if ($scope.applicationType == ''){
        return true
      } else {
        showStoreSection(storeType)
      }
    };
  }]
);
