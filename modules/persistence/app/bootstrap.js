var NumberUsers = 1000;
var SessionsPerUser = 22;
var PurchasesPerUser = 22;

function Device(os, version, model) {
  this.osType = os;
  this.osName = "name";
  this.osVersion = version;
  this.deviceModel = model; 
}

function User(userId, device) {
  this.userId = userId;
  this.sessions = [];
  this.purchases = [];
  this.devices = [device];
}

function Session(id, userId, device, length) {
  this.id = id;
  this.userId = userId;
  this.length = length;
  this.startTime = new Date().getTime();
  this.device = device;
  this.purchases = [];
}

function Purchase(id, sessionId, userId, itemId, price, device) {
  this.id = id;
  this.sessionId = sessionId;
  this.userId = userId;
  this.itemId = itemId;
  this.price = price;
  this.time = new Date().getTime();
  this.device = device;
  this.paymentSystem = 1; /** IAP **/
  this.success = true;
}

function getPurchaseResume(purchase) {
  return {
    id: purchase.id,
    time: purchase.time,
    platform: purchase.device.osType,
    paymentSystem: purchase.paymentSystem
  };
}

function getSessionResume(session) {
  return {
    id: session.id,
    startTime: session.startTime,
    platform: session.device.osType
  };
}

function generateUsers() {
  printjson("GENERATE USERS");
  for(var u = 0; u < NumberUsers; u++) {
      var newUser = new User(u, new Device("iOS", "8", "iPhone 6"));
      printjson("Inserting user: " + u);
      db.Wazza_mUsers_PayPalDemo.insert(newUser);
  }
}

function generateSessions() {
  printjson("GENERATE SESSIONS");
  for(var u = 0; u < NumberUsers; u++) {
    for(var s = 1; s < SessionsPerUser; s++) {
      var _id = "session-" + u + "-" + s;
      printjson("Creating sessions: " + s + " of user: " + u);
      var session = new Session(_id, u, new Device("iOS", "8", "iPhone 6"), 3)
      db.Wazza_mobileSessions_PayPalDemo.insert(session);
      db.Wazza_mUsers_PayPalDemo.update({"userId": u}, {$push: {"sessions": getSessionResume(session)}});
    }
  } 
}

function generatePurchases() {
  printjson("GENERATE PURCHASES");
  for(var u = 0; u < NumberUsers; u++) {
    for(var p = 1; p < PurchasesPerUser; p++) {
      var _id = "purchase-" + u + "-" + p;
      var sessionId = "session-" + u + "-" + p;
      printjson("Creating purchases: " + p + " of user: " + u);
      var price = Math.floor(Math.random() * 6);
      var purchase = new Purchase(_id, sessionId, u, "itemId", price, new Device("iOS", "8", "iPhone 6"));
      db.Wazza_purchases_PayPalDemo.insert(purchase);
      db.Wazza_mUsers_PayPalDemo.update({"userId": u}, {$push: {"purchases": getPurchaseResume(purchase)}});
    }
  }
}
var purchase = new Purchase(1, "sessionId", "u", "itemId", 2.8, new Device("iOS", "8", "iPhone 6"));
printjson(purchase)
printjson(getPurchaseResume(purchase));

generateUsers();
generateSessions();
generatePurchases();

