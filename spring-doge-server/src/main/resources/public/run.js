require.config({
    baseUrl: 'lib',
    paths: {

        stomp: 'stomp-websocket/lib/stomp',
        sockjs: 'sockjs/sockjs',
        angular: 'angular/angular',
        domReady: 'requirejs-domready/domReady'
        /*
         ,
         jquery: 'lib/jquery/jquery',
         bootstrap: 'lib/bootstrap/bootstrap',
         ngResource: 'lib/angular-resource/angular-resource',
         ngRoute: 'lib/angular-route/angular-route',
         msgs : 'lib/msgs/msgs',
         sockjs : 'lib/sockjs/sockjs'*/

    },
    shim: {
        angular: {
            exports: 'angular'
        }
        /*
         bootstrap: {
         deps: ['jquery']
         },
         cgBusy: {
         deps: ['promiseTracker']
         },
         'promiseTracker': {
         deps: ['angular']
         },
         'ngResource': {
         deps: ['angular']
         }*/
    }
});


define([
    'require' ,
    'angular',
    '../app'
], function (require) {
    'use strict';

    require(['sockjs', 'angular', 'stomp' , 'domReady!'], function (sockjs, angular, stomp) {
        console.log('sockjs, angular and stomp loaded, continuing..');
        angular.bootstrap(document, ['doge']);
        console.log('just called angular.bootstrap!')
    });



});
