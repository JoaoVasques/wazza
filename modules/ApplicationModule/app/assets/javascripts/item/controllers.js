// item module

angular.module('ItemModule.controllers', ['ItemModule.services']).
  controller('NewItemController', ['$scope', function ($scope) {

    $scope.itemForm = {};

    $scope.createItem = function(){
      console.log("create item");
    };

  }])
;