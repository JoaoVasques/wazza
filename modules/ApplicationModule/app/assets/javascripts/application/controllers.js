// application module

angular.module('ApplicationModule.controllers', ['ApplicationModule.services', 'DashboardModule']).
  controller(
    'NewApplicationFormController',
    ['$scope',
    '$location',
    'createNewApplicationService',
    '$route',
    'TopbarService',
    function(
      $scope,
      $location,
      createNewApplicationService,
      $route,
      TopbarService
    ) {

    TopbarService.setName("New Application");
    $scope.noImageThumbnailUrl = "http://allaboutuarts.ca/wp-content/uploads/2012/07/placeholder_2.jpg";
    $scope.storeOptions = ['iOS', 'Android'];
    $scope.applicationForm = {
      "name": "",
      "url": "",
      "packageName": "",
      "imageUrl": $scope.noImageThumbnailUrl,
      "appType": []
    };

    $scope.formErrors = {};

    $scope.createApplication = function(formData){
      createNewApplicationService.send(formData)
        .then(
          function(result){
            $scope.formErrors = {};
            $scope.applicationName=$scope.applicationForm.name;
            $location.path('/home');
          },
          function(errors){
            angular.extend($scope.formErrors, errors.data.errors);
          }
        );
    };

    $scope.imgThumb = "";
    $scope.AndroidSelected = false;
    $scope.iOSSelected = false;
    $scope.readyToSubmit = true;
    $scope.cssAndroidEnabled = "fa fa-android fa-2x store-selected";
    $scope.cssAndroidDisabled = "fa fa-android fa-2x store-unselected";
    $scope.cssiOSEnabled = "fa fa-apple fa-2x store-selected";
    $scope.cssiOSDisabled = "fa fa-apple fa-2x store-unselected";
    $scope.androidStoreCss = $scope.cssAndroidDisabled;
    $scope.iOSStoreCss = $scope.cssiOSDisabled;

    $scope.updatedStoreType = function(op, store) {
      if(op == 'add'){
        $scope.applicationForm.appType.push(store);
      } else {
        $scope.applicationForm.appType = _.without($scope.applicationForm.appType, store);
      }
    }

    $scope.$watch('AndroidSelected', function(newValue, oldValue, scope) {
      if(newValue) {
        $scope.androidStoreCss = $scope.cssAndroidEnabled;
        $scope.updatedStoreType('add', 'Android');
      } else {
        $scope.androidStoreCss = $scope.cssAndroidDisabled;
        $scope.updatedStoreType('remove', 'Android');
      }
    });

    $scope.$watch('iOSSelected', function(newValue, oldValue, scope) {
      if(newValue) {
        $scope.iOSStoreCss = $scope.cssiOSEnabled;
        $scope.updatedStoreType('add', 'iOS');
      } else {
        $scope.iOSStoreCss = $scope.cssiOSDisabled;
        $scope.updatedStoreType('remove', 'iOS');
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
