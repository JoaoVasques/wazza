'use strict';

dashboard.filter('wzCurrencyFilter', [
  'ApplicationStateService',
  'CurrencyService',
  function(
    ApplicationStateService,
    CurrencyService
  ) {
  return function(input, kpiName) {
    var out = "";
    var kpis = [
      'Total Revenue', 'Avg Revenue Per User', 'Avg Revenue per Session',
      'Average Revenue Per User', 'Average Revenue Per Session', 'Life Time Value'
    ];
    if(_.contains(kpis, kpiName)) {
      switch(ApplicationStateService.currency.name) {
        case 'Euro':
            out = CurrencyService.getCurrency('Euro').symbol + " " + input;
          break;
        case 'Dollar':
            out = CurrencyService.getCurrency('Dollar').symbol + " " + input;
          break;
      }
    } else {
      out = input;
    }
      
    return out;
  };
}]);

