'use strict';

angular.module('tabs.controllers')
		.controller('asTrainerController', ['$scope', 'tabsService', function ($scope, tabsService) {
			var type = 'trainingsAsTrainer';
			$scope.loading = true;
			tabsService.get(type)
					.then(function (data) {
						$scope.asTrainerTrainings = tabsService.parse(data.data, type);
					}, function (data) {
						console.error(data.status + ': ' + data.statusText + ' (' + data.config.url + ')');
					})
					.finally(function () {
						$scope.loading = false;
					});
		}]);