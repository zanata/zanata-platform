jQuery(function () {
  'use strict';
  jQuery(document).on('click', '.js-example__setter', function () {
    var exampleState = jQuery(this).attr('data-example');
    // Reset class and apply new one
    jQuery(this).parents('.js-example').find('.js-example__target')
           .attr('class', 'js-example__target').addClass(exampleState);
  });
});
