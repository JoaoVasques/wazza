// application module

angular.module('ApplicationModule.services', [])
  .factory('readyToSubmitService', function() {
    return {
      validate: function(formData){
        if (formData.name != "" &&
            formData.url != "" &&
            formData.appType != ""
        ) {
          if (formData.appType == "Android" && formData.androidData.packageName != "") {
            return false;
          } else {
            // TODO: iOS stuff
            return true;
          }
        } else {
          return true;
        }
      }
    };
});
