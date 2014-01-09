// item module

angular.module('ItemModule.controllers', ['ItemModule.services', 'angularFileUpload']).
  controller('NewItemController', ['$scope', '$upload', 'createNewItemService', '$routeParams', function ($scope, $upload, createNewItemService, $routeParams) {

    $scope.currencyOptions = ["Real","Virtual"];

    $scope.showCurrencyInputs = {
      "real": true,
      "virtual": false
    };

    $scope.virtualCurrencies = ["1","2"]; //TODO fetch from DB - not mandatory for now

    $scope.bootstrapModule = function(){
      $scope.itemForm = {
        "applicationName": $routeParams.applicationId,
        "name": "",
        "description": "",
        "store": 1,
        "metadata": {
          "osType": "",
          "title": "",
          "description": "",
          "publishedState": "published",
          "purchaseType": 0,
          "autoTranslate": true,
          "locale": [],
          "autofill": true,
          "language": "English", //TODO: get default lang
          "price": 0.0
        },
        "currency": {
          "typeOf": "Real",
          "value": 0.0
        }
      };
      $scope.showCurrencyInputs.real = true;
    };
    $scope.bootstrapModule();

    $scope.$watch('itemForm.currency.typeOf', function(newValue, oldValue, scope){
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
          function(errors){
            console.log("error");
            console.log(errors);
            //TODO: handle error
          }
        );
    };

  }])
;
