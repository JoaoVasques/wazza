<h1>Analytics Endpoint Documentation</h1>

<h3>Quick notes</h3>

+ every request starts with ***/analytics***
+ date format is ***"yyyy-mm-dd"*** for every request

<h3>Revenue</h3>

TODO: add return value

| Request type  | Endpoint|
| ------------- |:-------------:|
| Total | /revenue/total/{company name}/{application name }/{start date}/{end date}|
| Detailed      | /revenue/detail/{company name}/{application name }/{start date}/{end date}|

<h3>Average Revenue per User - ARPU</h3>

| Request type  | Endpoint|
| ------------- |:-------------:|
| Total | /arpu/total/{company name}/{application name }/{start date}/{end date}|
| Detailed      | /arpu/detail/{company name}/{application name }/{start date}/{end date}|
