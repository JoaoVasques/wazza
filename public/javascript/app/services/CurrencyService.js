service.factory('CurrencyService', ['$rootScope', '$resource', function($rootScope, $resource) {
  function CurrencyInfo(name, globalID, cssSymbol, symbol) {
    this.name = name;
    this.globalID = globalID;
    this.cssSymbol = cssSymbol;
    this.symbol = symbol;
  }

  var currencies = [];
  currencies.push(new CurrencyInfo("Euro", "EUR", "fa fa-eur", "€"));
  currencies.push(new CurrencyInfo("Dollar", "USD", "fa fa-usd", "$"));

  this.getCurrency = function(name) {
    return _.find(currencies, function(c) {return c.name == name;});
  };

  this.getCurrencies = function() {return currencies;}

  this.getCurrencyExchange = function(_id, successCallback) {
    var Currency = $resource("http://api.fixer.io/latest?symbols=:currencyID");
    Currency.get({currencyID: _id}, function(res) {
      successCallback(res);
    });
  };

  this.getDefaultCurrency = function() {
    return this.getCurrency("Euro");
  };

  return this;
}]);

