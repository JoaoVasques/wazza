'use strict';

dashboard.factory("AppOverviewModel", function() {
  function AppOverviewModel(name, url, platforms) {
    this.name = name;
    this.imageUrl = url;
    this.platforms = platforms; 
  }

  return AppOverviewModel;
});

