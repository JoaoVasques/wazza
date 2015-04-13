'use strict';

dashboard.filter('wzPaymentSystemFilter', [function() {
  return function(input) {
    var paymentSystemsHash = {
      1: "In-App Purchases",
      2: "PayPal",
      3: "Stripe"
    };
    return paymentSystemsHash[input];
  };
}]);

