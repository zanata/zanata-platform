var featureApp = angular.module('featureApp', [
  'ngRoute',
  'featureControllers',
  'featureFilters'
]);

featureApp.config(['$routeProvider',
  function($routeProvider) {
    $routeProvider.
      when('/features', {
        templateUrl: 'partials/list.html',
        controller: 'FeatureListCtrl'
      }).
      when('/features/:featureId', {
        templateUrl: 'partials/detail.html',
        controller: 'FeatureDetailCtrl'
      }).
      otherwise({
        redirectTo: '/features'
      });
  }]);
