'use strict';

dashboard.factory("AppOverviewModel", function() {
  function AppOverviewModel(name, url, platforms) {
    this.name = name;
    this.imageUrl = url;
    this.platforms = platforms;
    this.totalRevenue = 0;
    this.ltv = 0;
    this.arpu = 0;
  }

  return AppOverviewModel;
});

