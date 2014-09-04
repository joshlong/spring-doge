var appName = 'monitor';

/*******************************************************************************
 * Displays newly uploaded images communicated using websockets
 */
require.config({
	paths : {
		doge : 'doge',
		stomp : doge.jsUrl('stomp-websocket/lib/stomp'),
		sockjs : doge.jsUrl('sockjs/sockjs'),
		angular : doge.jsUrl('angular/angular'),
		domReady : doge.jsUrl('requirejs-domready/domReady')
	},
	shim : {
		angular : {
			exports : 'angular'
		}
	}
});

define([ 'require', 'angular' ], function(require, angular) {
	'use strict';
	require([ 'sockjs', 'angular', 'stomp', 'domReady!' ], function(sockjs,
			angular, stomp) {
		angular.bootstrap(document, [ appName ]);
	});

	var doge = angular.module(appName, []);

	doge.controller('MonitorController', [
			'$scope',
			'$http',
			'$log',
			function($scope, $http, $log) {

				$scope.imgSource = "";

				require([ 'sockjs', 'stomp' ], function(sockjs, stomp) {
					var socket = new SockJS('/doge');
					var client = Stomp.over(socket);
					client.connect({}, function(frame) {
						console.log('Connected ' + frame);
						client.subscribe("/topic/alarms", function(message) {
                            var body = JSON.parse(message.body);
                            var uri = body.dogePhotoUri;
                           // console.log (  'the body is ' + body )

							$scope.$apply(function() {
								$scope.onDoge(uri);
							});
						});
					}, function(error) {
						console.log("STOMP protocol error " + error);
					});
				});

				$scope.createImage = function(src, alt, title) {
					var img = document.createElement('img');
					img.src = src;
					if (alt != null)
						img.alt = alt;
					if (title != null)
						img.title = title;
					return img;
				};

				$scope.onDoge = function(uri) {
					//console.log( uri + '');
					var element = document.getElementById('imgPreview');
					var image = $scope.createImage(uri, 'Such Doge!', 'Such Boot!');
					image.style.visibility = "hidden";
					element.insertBefore(image, element.firstChild);
					image.className = "fadeIn";
				};
				$scope.imageContainer = null;
			} ]);
});
