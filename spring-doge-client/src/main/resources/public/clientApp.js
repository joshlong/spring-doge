var appName = 'client';


/***
 * Displays newly uploaded images communicated using websockets
 */
require.config({
    paths: {
        doge: 'doge',
        stomp: doge.jsUrl('stomp-websocket/lib/stomp'),
        sockjs: doge.jsUrl('sockjs/sockjs'),
        angular: doge.jsUrl('angular/angular'),
        domReady: doge.jsUrl('requirejs-domready/domReady')
    },
    shim: {
        angular: {
            exports: 'angular'
        }
    }
});

define([ 'require' , 'doge', 'angular'], function (require, angular) {
    'use strict';
    require(['sockjs', 'angular', 'stomp' , 'domReady!'], function (sockjs, angular, stomp) {
        angular.bootstrap(document, [appName]);
    });

    var doge = angular.module(appName, []);
    doge.controller('ClientController', [ '$scope', '$http', '$log', function ($scope, $http, $log) { } ]);
});