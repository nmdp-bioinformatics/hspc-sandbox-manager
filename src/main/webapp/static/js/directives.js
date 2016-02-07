/* Directives */

angular.module('sandManApp.directives', []).directive('resize', function ($window) {
    return function (scope, element, attr) {

        var w = angular.element($window);
        scope.$parent.$watch(function () {
            return {
                'h': w.height(),
                'w': w.width()
            };
        }, function (newValue, oldValue) {
            scope.$parent.windowHeight = newValue.h;
            scope.$parent.windowWidth = newValue.w;

            scope.$parent.resizeWithOffset = function (offsetH, offsetW) {

                scope.$parent.$eval(attr.notifier);

                var newSize = {
                    'height': (newValue.h - offsetH) + 'px'
                };

                if (offsetW !== undefined) {
                    newSize.width = (newValue.w - offsetW) + 'px'
                }

                return newSize;
            };
            scope.$parent.resizeTable = function (offsetH, minH) {

                scope.$parent.$eval(attr.notifier);

                var height = (newValue.h - offsetH);
                if (height < minH) {
                    height = minH;
                }

                return height + 'px'
            };

        }, true);

        w.bind('resize', function () {
            scope.$parent.$apply();
        });
    }
}).directive('screenSize', function ($window) {
        return function (scope, element, attr) {

            var w = angular.element($window);
            scope.$parent.$watch(function () {
                return {
                    'h': w.height(),
                    'w': w.width()
                };
            }, function (newValue, oldValue) {
                scope.$parent.windowHeight = newValue.h;
                scope.$parent.windowWidth = newValue.w;

                scope.$parent.scrollScreen = function (height, width) {

                    scope.$parent.$eval(attr.notifier);

                    return (newValue.h < height) || (newValue.w < width)
                };

            }, true);

            w.bind('screenSize', function () {
                scope.$parent.$apply();
            });
        }
    }).directive('center', function ($window) {
        return function (scope, element, attr) {

            var w = angular.element($window);
            scope.$parent.$watch(function () {
                return {
                    'h': w.height(),
                    'w': w.width()
                };
            }, function (newValue, oldValue) {
                scope.$parent.windowHeight = newValue.h;
                scope.$parent.windowWidth = newValue.w;

                scope.$parent.centerWithOffset = function (offsetW) {

                    scope.$parent.$eval(attr.notifier);

                    return {
                        'left': (newValue.w/2) - offsetW
                    };
                };
            }, true);

            w.bind('center', function () {
                scope.$parent.$apply();
            });
        }
    }).directive('enterKey', function () {
        return function (scope, element, attrs) {
            element.bind("keydown keypress", function (event) {
                var key = typeof event.which === "undefined" ? event.keyCode : event.which;
                if(key === 13) {
                    scope.$apply(function (){
                        scope.$eval(attrs.enterKey);
                    });

                    event.preventDefault();
                }
            });
        };
    }).directive( 'tableHeaderInner', function() {
        return {
            link: function( scope, elem, attrs ) {
                scope.$watch(function () {
                        return {
                            width: elem.parent().width()
                        }
                    },
                    function( width ) {
                        var newWidth = elem.parent()[0].clientWidth;
                        if (newWidth < 50){
                            newWidth = 50;
                        }
                        elem.css({
                            width: newWidth + 'px'
                        });
                    }, //listener
                    true  //deep watch
                );
            }
        }
    }).directive( 'tableContextHeaderInner', function() {
        return {
            link: function( scope, elem, attrs ) {
                scope.$watch(function () {
                        return {
                            width: elem.parent().width()
                        }
                    },
                    function( width ) {
                        var newWidth = elem.parent()[0].clientWidth;
                        if (newWidth < 100){
                            newWidth = 100;
                        }
                        elem.css({
                            width: newWidth + 'px'
                        });
                    }, //listener
                    true  //deep watch
                );
            }
        }
    }).directive( 'tableFixedWidthColumn', function() {
        return {
            link: function( scope, elem, attrs ) {
                scope.$watch(function () {
                        return {
                            width: elem.parent().width()
                        }
                    },
                    function( width ) {
                        elem.css({
                            width: elem.parent()[0].clientWidth + 'px'
                        });
                    }, //listener
                    true  //deep watch
                );
            }
        }
    }).directive('arrowSelector',['$document',function($document){
    return{
        restrict:'A',
        link:function(scope,elem,attrs,ctrl){
            $document.bind('keydown',function(e){
                    if(e.keyCode == 38){
                        scope.$parent.arrowUpDownResourceTable("up");
                        scope.$parent.$apply();
                        e.preventDefault();
                    }
                    if(e.keyCode == 40){
                        scope.$parent.arrowUpDownResourceTable("down");
                        scope.$parent.$apply();
                        e.preventDefault();
                    }
            });
        }
    };
}]).directive("scrollableTable", function () {
        return {
            restrict: 'E',
            templateUrl: 'static/js/templates/scrollableTable.html'
        };
    }).directive('notification', function($timeout, $compile){

        return {
            restrict: 'A',
            template: '<div></div>',
            replace: true,
            link: function(scope, element) {
                var el = angular.element('<span/>');

                scope.message.isVisible = true;
                switch(scope.message.type) {
                    case 'error':
                        el.append('<div ng-if="message.isVisible" ng-click="message.isVisible=false" class="message_error"><div>{{message.text}}</div></div>');
                        break;
                    case 'message':
                        el.append('<div ng-if="message.isVisible" ng-click="message.isVisible=false" class="message_info"><div>{{message.text}}</div></div>');
                        break;
                }
                $compile(el)(scope);
                element.append(el);
                $timeout(function (){
                    scope.message.isVisible = false;
                }, 3000);
            }
        }
    });
