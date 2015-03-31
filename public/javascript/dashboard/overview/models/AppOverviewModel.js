'use strict';

dashboard.factory("AppOverviewModel", function() {
  function AppOverviewModel(name, url, platforms, systems) {
    this.name = name;
    this.imageUrl = url;
    this.platforms = platforms;
    this.paymentSystems = systems;
    this.totalRevenue = 0;
    this.ltv = 0;
    this.arpu = 0;
  }

  return AppOverviewModel;
});

