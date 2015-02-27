'use strict';

var broadcastEvents = angular.module('Wazza.broadcastEvents',[]);

/** Datepicker changes **/
broadcastEvents.value('RevenueDateChanged','analytics.revenue');
broadcastEvents.value('ArpuDateChanged','analytics.arpu');
broadcastEvents.value('AvgRevenueSessionDateChanged','analytics.avgRevenueSession');
broadcastEvents.value('ATBPDateChanged','analytics.avgTimebetweenPurchase');
broadcastEvents.value('AT1PDateChanged','analytics.avgTimeFirstPurchase');
broadcastEvents.value('PurchasesPerUserChanged','analytics.avgPurchasesUser');
broadcastEvents.value('LtvDateChanged','analytics.ltv');
broadcastEvents.value('PayingUsersDateChanged','analytics.payingUsers');
broadcastEvents.value('PurchaseDateChanged','analytics.purchasesPerSession');

/** Platform selection changes **/
broadcastEvents.value("SelectedPlatformsChange", 0);
broadcastEvents.value('RevenuePlatformsChanged','analytics.revenue-platformChange');
broadcastEvents.value('ArpuPlatformsChanged','analytics.arpu-platformChange');
broadcastEvents.value('AvgRevenueSessionPlatformsChanged','analytics.avgRevenueSession-platformChange');
broadcastEvents.value('ATBPPlatformsChanged','analytics.avgTimebetweenPurchase-platformChange');
broadcastEvents.value('AT1PPlatformsChanged','analytics.avgTimeFirstPurchase-platformChange');
broadcastEvents.value('PurchasesPerUserPlatformsChanged','analytics.avgPurchasesUser-platformChange');
broadcastEvents.value('LtvPlatformsChanged','analytics.ltv-platformChange');
broadcastEvents.value('PayingUsersPlatformsChanged','analytics.payingUsers-platformChange');
broadcastEvents.value('PurchaseDatePlatformsChanged','analytics.purchasesPerSession-platformChange');

/** Dashboard view changes **/
broadcastEvents.value("DashboardViewChanges", 1);
broadcastEvents.value("DashboardShowPlatformDetails", 2);

/** Update dates **/
broadcastEvents.value("DashboardUpdateValuesOnDateChange", 3);
broadcastEvents.value("OverviewUpdateValuesOnDateChange", 4);

/** Currency Changes **/
broadcastEvents.value("CurrencyChanges", "newCurrency");
