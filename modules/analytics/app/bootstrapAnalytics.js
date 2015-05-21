/** Models **/

function Info(max, isInteger, sums) {
  this.max = max;
  this.isInteger = isInteger;
  this.sums = sums;
}

function PaymentSystemsResult(system, res) {
  this.system = system;
  this.res = res;
}

function PlatformResults(platform, res, paymentSystemResults) {
  this.platform = platform;
  this.res = res;
  this.paymentSystems = paymentSystemResults;
}

function ThoRResult(beginDate, endDate, totalResult, androidResults, iOSResults) {
  this.result = totalResult,
  this.platforms = [];
  this.platforms.push(androidResults);
  this.platforms.push(iOSResults);
  this.lowerDate = beginDate;
  this.upperDate = endDate;
}

function PurchasesPerPlatform(platform) {
  this.platform = platform;
  this.purchases = [];
}

function PurchaseInfo(id, time) {
  this.purchaseId = id;
  this.time = time;
  this.paymentSystem = Math.floor((Math.random() * 2) + 1);
  this.platform = Math.random() > 0.5 ? "Android" : "iOS"; 
} 

function PayingUsers(userId, beginDate, endDate, nrPurchases) {
  this.userId = userId;
  this.purchases = [];
  this.purchasesPerPlatform = [];
  this.purchasesPerPlatform.push(new PurchasesPerPlatform("Android"));
  this.purchasesPerPlatform.push(new PurchasesPerPlatform("iOS"));

  for(var i = 0; i < nrPurchases; i++) {
    var info = new PurchaseInfo(i, beginDate);
    this.purchases.push(info);
    if(info.platform == "Android") {
      this.purchasesPerPlatform[0].purchases.push(info);
    } else {
      this.purchasesPerPlatform[1].purchases.push(info);
    }
  }
  this.lowerDate = beginDate;
  this.upperDate = endDate;
}

function NumberSessionsFirstPurchasePerPlatform(nrSessions, platform) {
  this.platform = platform;
  this.result = nrSessions;
  this.nrUsers = 0;
  this.paymentSystems = [
    {system: 1, result: nrSessions, nrUsers: 0},
    {system: 2, result: nrSessions, nrUsers: 0}
  ];
}

function NumberSessionsFirstPurchase(nrSessions, beginDate, endDate) {
  this.result = nrSessions;
  this.nrUsers = 0;
  this.platforms = [];
  this.platforms.push(new NumberSessionsFirstPurchasePerPlatform(nrSessions, "Android"));
  this.platforms.push(new NumberSessionsFirstPurchasePerPlatform(nrSessions, "Android"));
  this.lowerDate = beginDate;
  this.upperDate = endDate;
}

/** Constants **/
var NUMBER_DAYS = 10;

var MAX_PAYING_USERS = 100;

var MAX_REVENUE = 2500;
var MAX_ARPU = 3;
var MAX_AVG_REVENUE_SESSION = 3;

var MAX_LIFE_TIME_VALUE = 2;
var MAX_PAYING_USERS = 300;
var MAX_PURCHASES_PER_USER = 10;
var MAX_AVG_PURCHASES_USER = 2;

var MAX_PURCHASES_SESSION = 2;
var MAX_SESSIONS_TO_FIRST_PURCHASE = 4;
var MAX_SESSIONS_BETWEEN_PURCHASES = 4;

var info = [];
info.push(new Info(MAX_REVENUE, false, true));
info.push(new Info(MAX_ARPU, false, false));
info.push(new Info(MAX_AVG_REVENUE_SESSION, false, false));

info.push(new Info(MAX_LIFE_TIME_VALUE, false, false));
info.push(new Info(MAX_PAYING_USERS, true, true));
info.push(new Info(MAX_AVG_PURCHASES_USER, false, false));

info.push(new Info(MAX_PURCHASES_SESSION, false, false));
info.push(new Info(MAX_SESSIONS_TO_FIRST_PURCHASE, false, false));
info.push(new Info(MAX_SESSIONS_BETWEEN_PURCHASES, false, false));

/** Aux Functions **/

function generateDates(numberDays) {
  var days = [];

  for(var d = 0; d < numberDays ; d++) {
    var date = new Date();
    date.setDate(date.getDate() - d);
    days.push(date);
  }
  return days;
};

function generateResult(upperBound, isInteger, sums, totalValue) {
  if(sums) {
    return isInteger ? Math.floor(Math.random() * totalValue) : Math.random() * totalValue;
  } else {
    return isInteger ? Math.floor(Math.random() * upperBound) : Math.random() * upperBound;
  }
}

function saveToDB(data, index) {
  switch(index) {
    case 0:
        db.Wazza_TotalRevenue_Demo.insert(data);
        break;
    case 1:
        db.Wazza_Arpu_Demo.insert(data);
      break;
    case 2:
        db.Wazza_avgRevenueSession_Demo.insert(data);
      break;
    case 3:
        db.Wazza_LifeTimeValue_Demo.insert(data);
      break;
    case 4:
        db.Wazza_payingUsers_Demo.insert(data);
        break;
    case 5:
        db.Wazza_avgPurchasesUser_Demo.insert(data);
        break;
    case 6:
        db.Wazza_PurchasesPerSession_Demo.insert(data);
        break;
    case 7:
        db.Wazza_NumberSessionsFirstPurchase_Demo.insert(data);
        break;
    case 8:
        db.Wazza_NumberSessionsBetweenPurchases_Demo.insert(data);
        break;
  }
}

/** 'MAIN' **/
var dates = generateDates(NUMBER_DAYS);
printjson("DATES");
printjson(dates);

for(var i = 0; (i+1) < dates.length; i++) {
  printjson(dates[i]);
  for(var k = 0; k < info.length; k++) {
    switch(k) {
      case 4:
          var numberUsers = generateResult(MAX_PURCHASES_PER_USER, true);
        for(var j = 0; j < numberUsers; j++) {
            var nrPurchases = generateResult(info[k].max, info[k].isInteger);
            printjson("Number of purchases: " + nrPurchases);
            var result = new PayingUsers(j, dates[i+1], dates[i], nrPurchases);
            saveToDB(result, k);
        }
        break;

      case 7:
        var sessions = generateResult(info[k].max, true);
        var result = new NumberSessionsFirstPurchase(sessions, dates[i+1], dates[i]);
        saveToDB(result, k);
        break;

        default:
      var total = generateResult(info[k].max, info[k].isInteger, info[k].sums, info[k].max);

      var androidResult = generateResult(info[k].max, info[k].isInteger, info[k].sums, total);
      var firstPaymentResult = generateResult(info[k].max, info[k].isInteger, info[k].sums, androidResult)
      var android = new PlatformResults(
        "Android",
        androidResult,
        [new PaymentSystemsResult(1, firstPaymentResult), new PaymentSystemsResult(2, generateResult(info[k].max, info[k].isInteger, info[k].sums, firstPaymentResult))]
      );

      var iOSResult = generateResult(info[k].max, info[k].isInteger, info[k].sums, total - androidResult);
      var firstIOSPaymentResult = generateResult(info[k].max, info[k].isInteger, info[k].sums, iOSResult)
      var iOS = new PlatformResults(
        "iOS",
        iOSResult,
        [new PaymentSystemsResult(1, firstIOSPaymentResult), new PaymentSystemsResult(2, generateResult(info[k].max, info[k].isInteger, info[k].sums, firstIOSPaymentResult))]
      );
      var result = new ThoRResult(dates[i+1], dates[i], total, android, iOS);
      saveToDB(result, k);
        break;
    }
  }
}

