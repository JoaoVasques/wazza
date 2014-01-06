// item module

angular.module('ItemModule.controllers', ['ItemModule.services', 'angularFileUpload']).
  controller('NewItemController', ['$scope', '$upload', 'createNewItemService', function ($scope, $upload, createNewItemService) {

    $scope.itemForm = {};
    $scope.currencyOptions = ["Real","Virtual"];

    $scope.showCurrencyInputs = {
      "real": true,
      "virtual": false
    };

    $scope.virtualCurrencies = ["1","2"]; //TODO

    $scope.bootstrapModule = function(){
      $scope.itemForm.currencyType = "Real";
      $scope.showCurrencyInputs.real = true;
    };
    $scope.bootstrapModule();

    $scope.$watch('itemForm.currencyType', function(newValue, oldValue, scope){
      if (newValue == "Real") {
        $scope.showCurrencyInputs.real = true;
        $scope.showCurrencyInputs.virtual = false;
      } else {
        $scope.showCurrencyInputs.real = false;
        $scope.showCurrencyInputs.virtual = true;
      }
    });

    $scope.createItem = function(){
      createNewItemService.send($scope.itemForm, $scope.myFile)
        .then(
          function(){
            console.log("success");
            //TODO: handle success
          },
          function(){
            console.log("error");
            //TODO: handle error
          }
        );
    };

  }])
;
