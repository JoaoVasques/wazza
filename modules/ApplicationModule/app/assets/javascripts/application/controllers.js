// application module

angular.module('ApplicationModule.controllers', ['ApplicationModule.services']).
  controller(
    'NewApplicationFormController',
    ['$scope', '$location','createNewApplicationService', '$route', function($scope, $location, createNewApplicationService, $route) {

    $scope.applicationForm = {
      "name": "",
      "appType": "",
      "appUrl": "",
      "packageName": ""
    };

    $scope.formErrors = {};

    $scope.createApplication = function(formData){
      createNewApplicationService.send(formData)
        .then(
          function(result){
            $scope.formErrors = {};
            $scope.applicationName=$scope.applicationForm.name;
            $scope.exist=true;
            $location.path('/home');
          },
          function(errors){
            angular.extend($scope.formErrors, errors.data.errors);
          }
        );
    };

    $scope.imgThumb = "";
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
        $scope.readyToSubmit = createNewApplicationService.validate($scope.applicationForm);
      },

      true
    );
  }])
;
