var resultStyle = function () {
  return function(result) {
    if (result == 'Passed') {
      return "label--success";
    } else if (result == 'Ignored') {
      return "label--unsure";
    } else {
      return "label--danger";
    }
  };
};
angular.module('featureFilters', []).filter('resultStyle', resultStyle);