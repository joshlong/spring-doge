define([ 'require' , 'angular'], function (require, angular) {


    var doge = angular.module('doge', []);

    doge.controller('DogeController', [ '$scope', '$http', '$log',
        function ($scope, $http, $log) {

            $scope.imgSource = "";

            require(['sockjs', 'stomp'], function (sockjs, stomp) {
                console.log('loaded sockjs and stomp!');
                var socket = new SockJS('/doge');
                console.log('created new SockJS pointing to /doge');
                var client = Stomp.over(socket);
                console.log('created new Stomp client on top of SockJS');

                client.connect({}, function (frame) {
                    console.log('Connected ' + frame);
                    client.subscribe("/topic/alarms", function (message) {
                        var uri = JSON.parse(message.body).dogePhotoUri;

                        $scope.$apply(function () {
                            $scope.onDoge(uri);
                        });
                    });
                }, function (error) {
                    console.log("STOMP protocol error " + error);
                });
            });


            $scope.onDoge = function (uri) {
                console.log(uri + '');
                window.alert('A new doge-ified image has been posted! ' + uri);
            };
        }]);
});