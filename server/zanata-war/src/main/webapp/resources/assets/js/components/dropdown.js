jQuery(function () {
  'use strict';

  var collapseActiveDropdowns,
      toggleThisCollapseOthers,
      collapseActiveDropdownsOld,
      toggleThisCollapseOthersOld;

  collapseActiveDropdowns = function () {
    jQuery('.js-dropdown.is-active .js-dropdown__toggle').click();
  };

  toggleThisCollapseOthers = function (e) {
    e.preventDefault();
    jQuery(this).blur();
    var $dropdown = jQuery(this).parents('.js-dropdown');
    // $dropdown.removeClass('is-hover');
    jQuery('.js-dropdown.is-active')
      .not($dropdown)
      .removeClass('is-active')
      .parents('.js-dropdown__container')
      .removeClass('is-active');
    $dropdown.toggleClass('is-active')
      .parents('.js-dropdown__container')
      .toggleClass('is-active');
    e.stopPropagation();
  };

  // Add this back when old dropdowns are removed

  // Don't toggle dropdown when clicking links inside it
  jQuery('.js-dropdown__toggle a')
    .bind('click', function (e) {
      e.stopPropagation();
    });

  jQuery(document)
    .bind('click', collapseActiveDropdowns);

  jQuery(document)
    .on('click', '.js-dropdown__toggle', toggleThisCollapseOthers);

});
