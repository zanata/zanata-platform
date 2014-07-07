var featureControllers = angular.module('featureControllers', []);

var FeatureListCtrl = function($scope, $http, $location, $filter) {
  $http.get('features.json').success(function(data) {
    $scope.features = data;
  });

  $scope.orderProp = 'category';
  $scope.featureDetail = function(index) {
    $location.path("/features/" + index);
  };
};

var FeatureDetailCtrl = function($scope, $http, $routeParams) {
  $http.get('features.json').success(function(data) {
    $scope.feature = data[$routeParams.featureId];
  });
};

featureControllers.controller('FeatureListCtrl', [ '$scope', '$http',
    '$location', '$filter', FeatureListCtrl ]);

featureControllers.controller('FeatureDetailCtrl', [ '$scope', '$http',
    '$routeParams', FeatureDetailCtrl ]);
