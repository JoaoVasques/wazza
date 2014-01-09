// item module

angular.module('ItemModule.services', []).
  factory('createNewItemService', ['$upload', '$q', function ($upload, $q) {
    return {
      send: function(formData, file){
        var request = $upload.upload({
            url: '/app/item/new/' + formData.applicationName, //TODO: get application name/id
            method: 'POST',
            data: formData,
            file: file
            // file: $files, //upload multiple files, this feature only works in HTML5 FromData browsers
             // set file formData name for 'Content-Desposition' header. Default: 'file' 
            //fileFormDataName: myFile,
            /* customize how data is added to formData. See #40#issuecomment-28612000 for example */
            //formDataAppender: function(formData, key, val){} 
        });

        var deferred = $q.defer();
        deferred.resolve(request);
        return deferred.promise;
      }
    }
  }])
;
