'use strict';

var broadcastEvents = angular.module('Wazza.broadcastEvents',[]);

broadcastEvents.value('RevenueDateChanged','home.revenue');
broadcastEvents.value('ArpuDateChanged','home.arpu');
broadcastEvents.value('AvgRevenueSessionDateChanged','home.avgRevenueSession');

