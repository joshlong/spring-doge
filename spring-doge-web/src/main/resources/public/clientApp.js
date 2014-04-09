var appName = 'client';

/***
 * Uploads images to made doge-tastic
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

define([ 'require' ,  'angular'], function (require, angular) {
    'use strict';

    require(['sockjs', 'angular', 'stomp' , 'domReady!'], function (sockjs, angular ) {
        angular.bootstrap(document, [appName]);
    });

    angular.module(appName, [])
        .controller('ClientController', [ '$scope', '$http', '$log',  function ($scope, $http, $log) {

            $scope.users = [];

            $http.get('/users').success(function(data) {
                $scope.users = data;
            });

    }]);
});
