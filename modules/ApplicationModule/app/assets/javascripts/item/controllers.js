// item module

angular.module('ItemModule.controllers', ['ItemModule.services', 'angularFileUpload', 'DashboardModule']).
  controller('NewItemController',[
    '$scope',
    '$upload',
    'createNewItemService',
    '$routeParams',
    '$location',
    'getVirtualCurrenciesService',
    'uploadPhotoService',
    'ApplicationStateService',
    'GetLanguagesService',
    function (
      $scope,
      $upload,
      createNewItemService,
      $routeParams,
      $location,
      getVirtualCurrenciesService,
      uploadPhotoService,
      ApplicationStateService,
      GetLanguagesService
    ) {

    $scope.currencyOptions = ["Real","Virtual"];
    $scope.showCurrencyInputs = {
      "real": true,
      "virtual": false
    };

    $scope.bootstrapModule = function(){
      $scope.itemForm = {
        "applicationName": ApplicationStateService.applicationName,
        "name": "",
        "description": "",
        "store": 1,
        "metadata": {
          "osType": "",
          "title": "",
          "description": "",
          "publishedState": "published",
          "purchaseType": "managed_by_publisher",
          "autoTranslate": false,
          "locale": [],
          "autofill": false,
          "language": "", //TODO: get default lang
          "price": 0.0
        },
        "currency": {
          "typeOf": "Real",
          "value": 0.0,
          "virtualCurrency": ""
        },
        "imageInfo": {
          "imageName": "",
          "url": ""
        }
      };
      $scope.showCurrencyInputs.real = true;
      $scope.errors = false;
      $scope.formErrors = [];
      $scope.moneyCurrency = "$"; /*+ TODO: in the future get this via API **/
      $scope.currentCurrency = "$";
      $scope.virtualCurrencies = [];
      getVirtualCurrenciesService.execute($scope.itemForm.applicationName)
        .then(
          $scope.handleVirtualCurrencyRequestSuccess,
          $scope.handleVirtualCurrencyRequestError
        );
      $scope.itemForm.metadata.language = _.first(GetLanguagesService.languageOptions());
      $scope.$watch('itemForm.currency.typeOf', function(newValue, oldValue, scope){
        if (newValue == "Real") {
          $scope.showCurrencyInputs.real = true;
          $scope.showCurrencyInputs.virtual = false;
          $scope.itemForm.currency.virtualCurrency = "";
          $scope.currentCurrency = "$"
        } else {
          $scope.showCurrencyInputs.real = false;
          $scope.showCurrencyInputs.virtual = true;
        }
      });

      $scope.$watch('itemForm.currency.virtualCurrency', function(newValue, oldValue, scope) {
        if (newValue != "") {
          $scope.currentCurrency = newValue;
        }
      });
    };
    $scope.bootstrapModule();

    $scope.onFileSelect = function(files) {
      uploadPhotoService.execute(_.first(files))
        .then(
          $scope.handlePhotoUploadSuccess,
          $scope.handlePhotoUploadError
        );
    }

    $scope.handleVirtualCurrencyRequestSuccess = function(success){
      _.each(success.data, function(value, key, list){
        $scope.virtualCurrencies.push(value.name);
      });

      /**Removes virtual currency option if application has not virtual currencies **/
      if(_.size($scope.virtualCurrencies) == 0) {
        $scope.currencyOptions = _.filter($scope.currencyOptions, function(value){
          return value != "Virtual";
        });
      }
    };

    $scope.handleVirtualCurrencyRequestError = function(error){
      $scope.currencyOptions = _.filter($scope.currencyOptions, function(value){
          return value != "Virtual";
      });
    };

    $scope.handlePhotoUploadSuccess = function(success) {
      $scope.itemForm.imageInfo.url = success.data.url;
      $scope.itemForm.imageInfo.name = success.data.fileName;
    };

    $scope.handlePhotoUploadError = function(error) {
      /** TODO **/
      console.log(error);
    }

    $scope.handleSuccess = function(data){
      console.log(data);
      var hiddenElement = document.createElement('a');
      hiddenElement.href = 'data:attachment/csv,' + encodeURI(data.data);
      hiddenElement.target = '_blank';
      hiddenElement.download = 'myFile.csv';
      hiddenElement.click();
      $scope.errors = false;
      $scope.formErrors = [];
      $location.path("/home");
    }

    $scope.handleErrors = function(errors){
      $scope.errors = true;
      _.each(angular.fromJson(errors.data.errors), function(error){
        $scope.formErrors.push(error);
      });
    }

    $scope.createItem = function(){
      createNewItemService.send($scope.itemForm, $scope.myFile)
        .then(
          $scope.handleSuccess,
          $scope.handleErrors
        );
    };
  }])
;
