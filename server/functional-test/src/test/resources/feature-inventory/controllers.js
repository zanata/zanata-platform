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

var FeatureDetailCtrl = function($scope, $http, $routeParams, filterFilter) {
  $http.get('features.json').success(function(data) {
    var filtered = filterFilter(data, {testName: $routeParams.testName});
    if (filtered.length == 1) {
      $scope.feature = filtered[0];
    } else {
      $scope.warning =
        "Something wrong with the URL. Can't find test name in features.";
    }
  });
};

featureControllers.controller('FeatureListCtrl', [ '$scope', '$http',
    '$location', '$filter', FeatureListCtrl ]);

featureControllers.controller('FeatureDetailCtrl', [ '$scope', '$http',
    '$routeParams', 'filterFilter', FeatureDetailCtrl ]);
