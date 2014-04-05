require.config({
    baseUrl: 'lib',
    paths: {

        stomp: 'stomp-websocket/lib/stomp',
        sockjs: 'sockjs/sockjs'


        /*  domReady: 'lib/requirejs-domready/domReady',
         angular: 'lib/angular/angular',
         jquery: 'lib/jquery/jquery',
         bootstrap: 'lib/bootstrap/bootstrap',
         ngResource: 'lib/angular-resource/angular-resource',
         ngRoute: 'lib/angular-route/angular-route',
         msgs : 'lib/msgs/msgs',
         sockjs : 'lib/sockjs/sockjs'*/

    },
    shim: {
        /*  angular: {
         exports: 'angular'
         },
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
    'require'
//    'angular',
    // 'app'
], function (require) {
    'use strict';

    require(['sockjs', 'stomp'], function (sockjs, stomp) {
        console.log('loaded sockjs and stomp!');
        var socket = new SockJS('/doge');
        console.log('created new SockJS pointing to /doge');
        var client = Stomp.over(socket);
        console.log('created new Stomp client on top of SockJS');

        client.connect({}, function (frame) {
            console.log('Connected ' + frame);
            client.subscribe("/topic/alarms", function (message) {
                var uri = JSON.parse( message.body).dogePhotoUri ;
                console.log ( uri  +'');
                window.alert('A new doge-ified image has been posted! '+ uri );
            });
        }, function (error) {
            console.log("STOMP protocol error " + error);
        });


    });


    /* require(['domReady!'], function (document) {
     angular.bootstrap(document, ['doge']);
     console.log('just called angular.bootstrap!')
     });*/

});

