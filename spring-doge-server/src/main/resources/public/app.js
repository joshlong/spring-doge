/*
define([   'require', 'angular', 'ngResource' , 'sockjs' ,'msgs' ], function ( require , angular , msgs) {
    'use strict';

//    var msgs = require('msgs');
    var SockJS = require('sockjs');

    require('msgs/adapters/webSocket');
    require('msgs/channels/bridges/stomp');


    var doge = angular.module('doge', [ 'ngResource' ]);

    doge.controller('DogeController', ['$scope', '$http', '$log', '$injector',
        function ($scope, $http, $log, $injector) {


        }]);

    return doge;
});*/
