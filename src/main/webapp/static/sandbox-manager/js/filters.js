'use strict';

/* Filters */

angular.module('sandManApp.filters', []).filter('formatAttribute', function ($filter) {
        return function (input) {
            if (Object.prototype.toString.call(input) === '[object Date]') {
                return $filter('date')(input, 'MM/dd/yyyy HH:mm');
            } else {
                return input;
            }
        };
});