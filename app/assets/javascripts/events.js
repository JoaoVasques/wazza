'use strict';

var broadcastEvents = angular.module('Wazza.broadcastEvents',[]);

broadcastEvents.value('RevenueDateChanged','analytics.revenue');
broadcastEvents.value('ArpuDateChanged','analytics.arpu');
broadcastEvents.value('AvgRevenueSessionDateChanged','analytics.avgRevenueSession');
broadcastEvents.value('ATBPDateChanged','analytics.avgTimebetweenPurchase');
broadcastEvents.value('AT1PDateChanged','analytics.avgTimeFirstPurchase');
broadcastEvents.value('ChurnDateChanged','analytics.avgPurchasesUser');
broadcastEvents.value('LtvDateChanged','analytics.ltv');
broadcastEvents.value('PayingUsersDateChanged','analytics.payingUsers');
broadcastEvents.value('PurchaseDateChanged','analytics.purchasesPerSession');
