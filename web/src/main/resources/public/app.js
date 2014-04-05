define([ 'angular', 'ngResource' ], function (angular) {
    'use strict';


    var doge = angular.module('doge', [ 'ngResource' ]);

    doge.controller('DogeController', ['$scope', '$http', '$log', '$injector',
        function ($scope, $http,  $log,  $injector) {




        }]);

    return doge;
});