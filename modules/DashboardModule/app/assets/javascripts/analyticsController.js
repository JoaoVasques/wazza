'use strict';

dashboard
.controller('AnalyticsController', [
  '$scope',
  '$location',
  '$rootScope',
  'FetchItemsService',
  'BootstrapDashboardService',
  'DeleteItemService',
  'ApplicationStateService',
  'ItemSearchService',
  'TopbarService',
  function (
        $scope,
        $location,
        $rootScope,
        FetchItemsService,
        BootstrapDashboardService,
        DeleteItemService,
        ApplicationStateService,
        ItemSearchService,
        TopbarService
    ) {

        TopbarService.setName("Analytics");

        var dummyData = {
            "Users": {
                "totalMoneySpent": {
                    "UserLevel0": 0,
                    "UserLevel1": 0,
                    "UserLevel2": 20,
                    "UserLevel3": 10,
                    "UserLevel4": 10,
                    "UserLevel5": 40,
                    "UserLevel6": 67,
                    "UserLevel7": 80,
                    "UserLevel8": 80,
                    "UserLevel9": 90
                },
                "deviceInfo": {
                    "pieChart": {
                        "appVersion": {
                            "2": 10000,
                            "2.1": 45000,
                            "3": 4000000
                        },
                        "OS": {
                            "Android 2.3": 10000,
                            "Android 4.0": 20000,
                            "iOS 6": 4025000
                        },
                        "Screen Resolution": {
                            "240p": 5000,
                            "480p": 40000,
                            "720p": 2000000,
                            "1080p": 2000000
                        }
                    },
                    "map": {
                        "lat": "38",
                        "long": "34"
                    }
                },
                "moneySpentLines": {

                }

            },
            "string": "Hello World"
        }


        var rows = [];

        for (var key in dummyData.Users.totalMoneySpent) {
            if (dummyData.Users.totalMoneySpent.hasOwnProperty(key)) {
                rows.push(dummyData.Users.totalMoneySpent[key]);
            }
        }


        $(function () {
            $('#revenue').highcharts({
                chart: {
                    type: 'column'
                },
                title: {
                    text: 'Revenue per User level'
                },
                xAxis: {
                    type: 'category',
                    labels: {
                        rotation: -45,
                        align: 'right',
                        style: {
                            fontSize: '13px',
                            fontFamily: 'Verdana, sans-serif'
                        }
                    }
                },
                legend: {
                    enabled: false
                },
                tooltip: {
                    pointFormat: "Total Money Spent in Q1 2014: <b>{point.y:.1f} millions</b>"
                },
                yAxis: {
                    title: {
                        text: 'in Million USD'
                    }
                },
                series: [{
                    name: 'Population',
                    data: rows,
                    dataLabels: {
                        enabled: true,
                        rotation: -90,
                        color: '#FFFFFF',
                        align: 'right',
                        x: 4,
                        y: 10,
                        style: {
                            fontSize: '13px',
                            fontFamily: 'Verdana, sans-serif',
                            textShadow: '0 0 3px black'
                        }
                    }
                }]
            });
        });

        $(function () {
            $('#users').highcharts({
                title: {
                    text: 'Users and Downloads evolution',
                    x: -20 //center
                },
                xAxis: {
                    categories: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
                    'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
                },
                yAxis: [{ // Primary yAxis
                            title: {
                                text: 'Users',
                                style: {
                                    color: Highcharts.getOptions().colors[1]
                                }
                            },
                            labels: {
                                format: '{value} k',
                                style: {
                                    color: Highcharts.getOptions().colors[1]
                                }
                            }
                        }, { // Secondary yAxis
                            title: {
                                text: 'Downloads',
                                style: {
                                    color: Highcharts.getOptions().colors[0]
                                }
                            },
                            labels: {
                                format: '{value} k',
                                style: {
                                    color: Highcharts.getOptions().colors[0]
                                }
                            },
                            opposite: true
                        }],
                tooltip: {
                    valueSuffix: ''
                },
                legend: {
                    layout: 'vertical',
                    align: 'right',
                    verticalAlign: 'middle',
                    borderWidth: 0
                },
                series: [{
                    name: 'Total Downloads',
                    type: 'spline',
                    data: [7.0, 6.9, 9.5, 14.5, 18.2, 21.5, 25.2, 26.5, 23.3, 18.3, 13.9, 9.6]
                }, {
                    name: 'Active Users',
                    type: 'spline',
                    data: [3.9, 4.2, 5.7, 11.3, 17.0, 22.0, 24.8, 24.1, 20.1, 14.1, 8.6, 4.8]
                }, {
                    name: 'Active Users with IAP',
                    type: 'spline',
                    data: [-0.2, 0.8, 5.7, 8.5, 11.9, 15.2, 17.0, 16.6, 14.2, 10.3, 6.6, 2.5]
                }]
            });
        });

        $(function () {
            $('#activeUsersIAP').highcharts({
                chart: {
                    zoomType: 'xy'
                },
                title: {
                    text: 'Active users and IAPs'
                },
                xAxis: [{
                    categories: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
                'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
                }],
                yAxis: [{ // Primary yAxis
                            labels: {
                                format: '{value}',
                                style: {
                                    color: Highcharts.getOptions().colors[0]
                                }
                            },
                            title: {
                                text: 'Active users with IAPs',
                                style: {
                                    color: Highcharts.getOptions().colors[0]
                                }
                            },
                            opposite: true
                        }, { // Secondary yAxis
                            gridLineWidth: 0,
                            title: {
                                text: 'IAP',
                                style: {
                                    color: Highcharts.getOptions().colors[1]
                                }
                            },
                            labels: {
                                format: '{value} k',
                                style: {
                                    color: Highcharts.getOptions().colors[1]
                                }
                            }

                        }, { // Tertiary yAxis
                            gridLineWidth: 0,
                            title: {
                                text: 'Revenue',
                                style: {
                                    color: Highcharts.getOptions().colors[2]
                                }
                            },
                            labels: {
                                format: '{value} k USD',
                                style: {
                                    color: Highcharts.getOptions().colors[2]
                                }
                            },
                            opposite: true
                        }],
                tooltip: {
                    shared: true
                },
                legend: {
                    layout: 'vertical',
                    align: 'right',
                    verticalAlign: 'middle',
                    borderWidth: 0
                },
                series: [{
                            name: 'Active users with IAPs',
                            type: 'spline',
                            yAxis: 1,
                            data: [49.9, 71.5, 106.4, 129.2, 144.0, 176.0, 135.6, 148.5, 216.4, 194.1, 95.6, 54.4],
                            tooltip: {
                                valueSuffix: ' k'
                            }

                        }, {
                            name: 'IAPs/month',
                            type: 'spline',
                            yAxis: 2,
                            data: [1016, 1016, 1015.9, 1015.5, 1012.3, 1009.5, 1009.6, 1010.2, 1013.1, 1016.9, 1018.2, 1016.7],
                            marker: {
                                enabled: false
                            },
                            dashStyle: 'shortdot',
                            tooltip: {
                                valueSuffix: ' mb'
                            }

                        }, {
                            name: 'Revenue/month',
                            type: 'spline',
                            data: [7.0, 6.9, 9.5, 14.5, 18.2, 21.5, 25.2, 26.5, 23.3, 18.3, 13.9, 9.6],
                            tooltip: {
                                valueSuffix: ' °C'
                            }
                        }]
            });
        });

        $(function () {
            $('#appSession').highcharts({
                chart: {
                    zoomType: 'xy'
                },
                title: {
                    text: 'Session and IAPs'
                },
                xAxis: [{
                    categories: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
                'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
                }],
                yAxis: [{ // Primary yAxis
                            labels: {
                                format: '{value}',
                                style: {
                                    color: Highcharts.getOptions().colors[1]
                                }
                            },
                            title: {
                                text: 'Session Time',
                                style: {
                                    color: Highcharts.getOptions().colors[1]
                                }
                            },
                            opposite: true

                        }, { // Secondary yAxis
                            gridLineWidth: 0,
                            title: {
                                text: 'Revenue / Session',
                                style: {
                                    color: Highcharts.getOptions().colors[0]
                                }
                            },
                            labels: {
                                format: '{value} k USD',
                                style: {
                                    color: Highcharts.getOptions().colors[0]
                                }
                            }
                        }],
                tooltip: {
                    shared: true
                },
                legend: {
                    layout: 'vertical',
                    align: 'right',
                    verticalAlign: 'middle',
                    borderWidth: 0
                },
                series: [{
                            name: 'Session time',
                            type: 'spline',
                            yAxis: 1,
                            data: [14, 13.8, 19, 29, 36.4, 43, 49.6, 48.2, 40.2, 28.2, 17.2, 5],
                            tooltip: {
                                valueSuffix: ' k'
                            }

                        }, {
                            name: 'Revenue/session',
                            type: 'spline',
                            yAxis: 0,
                            data: [1016, 1016, 1015.9, 1015.5, 1012.3, 1009.5, 1009.6, 1010.2, 1013.1, 1016.9, 1018.2, 1016.7],
                            marker: {
                                enabled: false
                            },
                            dashStyle: 'shortdot',
                            tooltip: {
                                valueSuffix: ' mb'
                            }
                        }]
            });
        });



        $(function () {

            var colors = Highcharts.getOptions().colors,
                categories = ['Android', 'iOS'],
                name = 'Mobile Platforms',
                data = [{
                    y: 55.11,
                    color: colors[0],
                    drilldown: {
                        name: 'Android versions',
                        categories: ['Android 2.1', 'Android 2.2', 'Android 4', 'Android 4.2.2'],
                        data: [10.85, 7.35, 33.06, 2.81],
                        color: colors[0]
                    }
                }, {
                    y: 21.63,
                    color: colors[1],
                    drilldown: {
                        name: 'iOS versions',
                        categories: ['iOS 2.0', 'iOS 3.0', 'iOS 5', 'iOS 6', 'iOS 7.0'],
                        data: [0.20, 0.83, 1.58, 13.12, 5.43],
                        color: colors[1]
                    }
                }];


            // Build the data arrays
            var platformsData = [];
            var versionsData = [];
            for (var i = 0; i < data.length; i++) {

                // add browser data
                platformsData.push({
                    name: categories[i],
                    y: data[i].y,
                    color: data[i].color
                });

                // add version data
                for (var j = 0; j < data[i].drilldown.data.length; j++) {
                    var brightness = 0.2 - (j / data[i].drilldown.data.length) / 5;
                    versionsData.push({
                        name: data[i].drilldown.categories[j],
                        y: data[i].drilldown.data[j],
                        color: Highcharts.Color(data[i].color).brighten(brightness).get()
                    });
                }
            }

            // Create the chart
            $('#device').highcharts({
                chart: {
                    type: 'pie'
                },
                title: {
                    text: 'Device Info'
                },
                yAxis: {
                    title: {
                        text: 'DELETE Total percent market share'
                    }
                },
                plotOptions: {
                    pie: {
                        shadow: false,
                        center: ['50%', '50%']
                    }
                },
                tooltip: {
                    valueSuffix: '%'
                },
                series: [{
                    name: 'Platforms',
                    data: platformsData,
                    size: '60%',
                    datSaLabels: {
                        formatter: function () {
                            return this.y > 5 ? this.point.name : null;
                        },
                        color: 'white',
                        distance: -30
                    }
            }, {
                    name: 'Versions',
                    data: versionsData,
                    size: '80%',
                    innerSize: '60%',
                    dataLabels: {
                        formatter: function () {
                            // display only if larger than 1
                            return this.y > 1 ? '<b>' + this.point.name + ':</b> ' + this.y + '%' : null;
                        }
                    }
            }]
            });
        });

}])