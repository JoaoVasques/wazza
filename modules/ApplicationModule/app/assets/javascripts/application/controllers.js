// application module

angular.module('ApplicationModule.controllers', ['ApplicationModule.services']).
  controller('NewApplicationFormController', ['$scope', 'readyToSubmitService', function($scope, readyToSubmitService) {

    $scope.applicationForm = {
      "name": "",
      "appType": "",
      "url": "",
      "androidData": {
        "packageName": ""
      }
    };
    $scope.readyToSubmit = true;
    $scope.store = {
      "Android": false,
      "iOS": false
    };

    $scope.$watch('applicationForm.appType', function(newValue, oldValue, scope) {
      if (newValue == "Android") {
        $scope.store.Android = true;
        $scope.store.iOS = false;
      } else {
        $scope.store.Android = false;
        $scope.store.iOS = true;
      }
    });

    $scope.$watch(
      function(){
        return $scope.applicationForm;
      },

      function(newVal, oldVal){
        $scope.readyToSubmit = readyToSubmitService.validate($scope.applicationForm);
      },

      true
    );
  }]
);
