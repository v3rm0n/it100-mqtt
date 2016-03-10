var app = angular.module('it100', ['ngWebSocket']);

var template = "\
 |--------------------------------|\n\
1|                                |\n\
2|                                |\n\
 |--------------------------------|";

app.factory('IT100', function ($websocket, $rootScope, $location) {

	var dataStream = $websocket('ws://'+$location.host()+':'+$location.port()+'/it100');

	dataStream.onMessage(function (message) {
		var data = JSON.parse(message.data);
		if (data.led) {
			$rootScope.$broadcast('it100:led', data);
		} else if (data.text) {
			$rootScope.$broadcast('it100:text', data);
		} else if (data.cursor) {
			$rootScope.$broadcast('it100:cursor', data);
		}
	});

	return {
		click: function (button) {
			dataStream.send(JSON.stringify({button: button}));
		}
	};
});

app.controller('IT100Controller', function ($scope, IT100) {

	$scope.display = template;

	var padding = '                                ';

	function pad(str, padLeft) {
		if (typeof str === 'undefined')
			return padding;
		if (padLeft) {
			return (padding + str).slice(-32);
		} else {
			return (str + padding).substring(0, 32);
		}
	}

	var fromTemplate = function (line, column, text) {
		var current = $scope.display;
		if (line === 0) {
			return current.replace(/1\|[^|]*/, "1|" + pad(text));
		}
		return current.replace(/2\|[^|]*/, "2|" + pad(text));
	};

	$scope.leds = new Array(20).fill(0);

	$scope.$on('it100:text', function (e, data) {
		$scope.display = fromTemplate(data.line, data.column, data.text);
	});

	$scope.$on('it100:led', function (e, data) {
		$scope.$apply(function () {
			$scope.leds[data.led] = data.status;
		});
	});

	$scope.click = function (button) {
		IT100.click(button);
	};

	$scope.ledClass = function (led) {
		var status = $scope.leds[led];
		if (status === 0) {
			return 'led-off';
		} else if (status === 2) {
			return 'blinking';
		}
		return '';
	};

});
