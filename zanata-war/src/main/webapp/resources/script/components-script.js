/* global zanata */

// Escapes special characters and returns a valid jQuery selector
function jqSelector(str) {
  if (str) {
    return str.replace(/([;&,\.\+\*\~':"\!\-\/\^#$%@\[\]\(\)=>\|])/g, '\\$1');
  }
  return str;
}

jQuery(document).ready(function() {
  registerUrlModifiers();
});

function refreshTooltip(wrapperId) {
  jQuery('#' + wrapperId).find('[title]').each(function() {
    zanata.tooltip.init(this);
  });
}

// Registers all elements that modify the browser's url
function registerUrlModifiers() {
  jQuery('a.js-url-mod').click(function(e) {
    e.preventDefault();
    var $this = jQuery(this);
    changeBrowserUrl($this.attr('href'));
  });
}

function validateTab(tab, currentSection, defaultSection) {
  if (jQuery(tab).length === 0) {
    window.location.href = window.location.href.replace(currentSection,
        defaultSection);
    return defaultSection;
  }
  return currentSection;
}

function updateStateFromUrl() {
  crossroads.parse(window.location.pathname + window.location.search);
}

// Add / Update a key-value pair in the URL query parameters
// From https://gist.github.com/niyazpk/f8ac616f181f6042d1e0
function updateUrlParameter(uri, key, value) {
  // remove the hash part before operating on the uri
  var i = uri.indexOf('#');
  var hash = i === -1 ? ''  : uri.substr(i);
  uri = i === -1 ? uri : uri.substr(0, i);

  var re = new RegExp("([?&])" + key + "=.*?(&|$)", "i");
  var separator = uri.indexOf('?') !== -1 ? "&" : "?";
  if (uri.match(re)) {
    uri = uri.replace(re, '$1' + key + "=" + value + '$2');
  } else {
    uri = uri + separator + key + "=" + value;
  }
  return uri + hash;  // finally append the hash as well
}

function changeBrowserUrl(url, refresh) {
  refresh = refresh || false;

  // only if url doesn't start with '#'
  if (window.dswh && window.dswh.windowId && url.indexOf('#') !== 0) {
    url = updateUrlParameter(url, 'dswid', window.dswh.windowId);
  }

  var status = {
    path : url
  };
  window.history.pushState(status, document.title, url);
  if (refresh)
    updateStateFromUrl();
}

jQuery(function() {
  jQuery(window).on("popstate", function(event) {
    var state = event.originalEvent.state;
    if (state)
      crossroads.parse(state.path + state.search)
  })
});

function updateActiveRow(clickedElement) {
  var parent = jQuery(clickedElement).parent();

  jQuery(parent).siblings("li").removeClass('is-active');
  jQuery(parent).siblings("li").children("a").removeClass('is-active');

  jQuery(clickedElement).addClass("is-active");
  jQuery(parent).addClass('is-active');
}

function toggleColumn(tabId) {
  jQuery('#' + tabId).find('.panels--2').toggleClass('panel__active-2');
}

function removeActiveRows(listId) {
  var items = jQuery('#' + listId).children('li');
  items.removeClass('is-active');
  items.children("a").removeClass('is-active');
}

function focusCurrentActiveInput() {
  jQuery(
      jQuery('.js-tabs-nav').children('li.is-active').children('a')
          .attr('href')).find('.js-tabs-nav-focus-input').each(function() {
    jQuery(this).value = "";
    jQuery(this).focus();
  });
}

function clearHTML(listId) {
  jQuery('#' + listId).empty();
}

/**
 * Given any number of jQuery selectors, returns a function that will reveal
 * a given element and ensure that all other selectors passed to this function
 * are hidden.
 */
function showOnlyOneOf() {
    var i, selectors = Array.prototype.slice.call(arguments);
    return function (show) {
        selectors.forEach(function (selector) {
            jQuery(selector).addClass('is-hidden');
        });
        jQuery(show).removeClass('is-hidden');
    }
}

/**
 * Get an ancestor that will not be removed by dynamic element replacement.
 *
 * To indicate that an element is static, add the class 'js-static-element'.
 * This class should only be applied to elements that are not moved or
 * removed dynamically such as in response to user activity or an ajax
 * request.
 *
 * The ancestor can be used as an event delegate for elements that
 * will be refreshed with ajax.
 *
 * return: the static ancestor as a jQuery object.
 */
function getStaticAncestor(jqueryElement) {
    return jqueryElement.closest('.js-static-element, body');
}


/**
 * Attach to keypress event to prevent submission of form when Enter is
 * pressed in a textbox.
 */
function doNotSubmit(event) {
    if (isEnterKey(event)) {
        event.preventDefault();
    }
}

$(document).ready(initListOperations);

/**
 * Attach events for controlling appearance of elements in response to list
 * selections.
 *
 * To make an element's appearance depend on selections in a list, add a data
 * attribute to indicate which list should be used, and a data attribute for
 * each quantity of selections you wish to appearance to change with, and
 * which css classes should be added or removed.
 *
 * Indicate the target list using the attribute data-target-list, with a
 * jQuery selector that matches the list of interest.
 *
 * Classes to toggle are specified as the value of a data attribute. The data
 * attribute name includes the quantifier that determines when the class is
 * present or absent. The data attribute name is in the form
 * data-[quantifier]-selected-class.
 *
 * The following quantifiers are available. Note that an item is considered
 * selected if it has the class 'is-checked':
 *
 *  - none: no items in the list are selected
 *  - one: exactly 1 item in the list is selected
 *  - multiple: 2 or more items in the list are selected
 *  - some: 1 or more items in the list are selected, but not all items.
 *  - any: 1 or more items in the list are selected
 *  - all: there are 1 or more items in the list and all of them are selected
 *
 * Quantifiers can also be prefixed with not- to invert their meaning.
 * e.g. not-any has the same meaning as none.
 *
 * Multiple quantifiers may be used in separate data attributes on the same
 * element. If two quantifiers toggle the same css class, the quantifier with
 * the highest precedence will determine the toggle state (the other is
 * essentially ignored, but may toggle other css classes too). Quantifiers
 * from highest to lowest precedence are:
 *
 *  - not-none
 *  - none
 *  - not-one
 *  - one
 *  - not-all
 *  - all
 *  - not-some
 *  - some
 *  - not-multiple
 *  - multiple
 *  - not-any
 *  - any
 *
 *
 * Example: show a button only when items are selected, and change its label
 *          to singular or plural depending on the number selected. The button
 *          should be highlighted and have red text if all the items are
 *          selected. Assume that all the css classes used are defined and
 *          have the obvious effect.
 *
 *   <button data-target-list="#my-list"
 *           data-none-selected-class="is-hidden"
 *           data-all-selected-class="highlighted red-text">
 *     <span data-target-list="#my-list"
 *           data-not-one-selected-class="is-hidden">
 *       Delete Selected Item</span>
 *     <span data-target-list="#my-list"
 *           data-not-multiple-selected-class="is-hidden">
 *       Delete Selected Items</span>
 *   </button>
 *   <ul id="my-list">
 *     <li />
 *     <li />
 *     <li />
 *   </ul>
 *
 */
function initListOperations() {
    var $ = jQuery;
    var listOperations = $('[data-target-list]');

    while (listOperations.length > 0) {
        listOperations.first().each(function () {
            var targetListSelector = $(this).data('target-list');
            bindOperationToList(targetListSelector);
            // All operations for a list are discovered dynamically, so each target
            // list only needs to be registered once.
            listOperations = listOperations.not('[data-target-list="' + targetListSelector + '"]');
        });
    }

    function bindOperationToList (targetListSelector) {
        // Event handlers are lost if they are in a region that is refreshed from
        // an ajax call. They are instead delegated to an ancestor element that is
        // marked as static by the developer, using the body as a fallback.
        var eventDelegate = getStaticAncestor($(targetListSelector));
        eventDelegate.on('change', targetListSelector, delayedTriggerListRecheck);

        // FIXME this waits for the script that changes the is-checked class on the
        //       item, but this should be triggered directly by that script when
        //       this code is moved to assets.
        function delayedTriggerListRecheck () {
            //console.log('delayedTriggerListRecheck');
            setTimeout(triggerListRecheck, 20);
        }

        function triggerListRecheck() {
            var targetList = $(targetListSelector);
            var totalElements = targetList.find('.js-form__checkbox__input').size();
            var selectedElements = targetList.find('.js-form__checkbox__input:checked').size();

            var none = selectedElements === 0;
            var one = selectedElements === 1;
            var multiple = selectedElements > 1;
            var some = selectedElements > 0 && selectedElements < totalElements;
            var any = selectedElements > 0;
            // all is purposely false for an empty list
            var all = selectedElements === totalElements && totalElements > 0;

            // $('.js-list-operation[data-target-list="' + targetListSelector + '"]')
            $('[data-target-list="' + targetListSelector + '"]')
                .each(function () {
                    var $element = $(this);

                    // specifically ordered so that items dealing with the same class
                    // will have the desired precedence.
                    updateClassesForCondition($element, 'any', any);
                    updateClassesForCondition($element, 'multiple', multiple);
                    updateClassesForCondition($element, 'some', some);
                    updateClassesForCondition($element, 'all', all);
                    updateClassesForCondition($element, 'one', one);
                    updateClassesForCondition($element, 'none', none);
                });

            function updateClassesForCondition($element, condition, state) {
                if ($element.attr('data-' + condition + '-selected-class')) {
                    $element.toggleClass($element.data(condition + '-selected-class'),
                        state);
                }
                if ($element.attr('data-not-' + condition + '-selected-class')) {
                    $element.toggleClass($element.data('not-' + condition + '-selected-class'),
                        !state);
                }
            }
        }
    }
}

/* ----------------------------------------------------------- */
/*----------------zanata-autocomplete component----------------*/
/* ----------------------------------------------------------- */

jQuery(document).ready(function() {
  jQuery(this).click(function(event) {
    // FIXME this will only match if there is a single class only
    if (!jQuery(event.target).hasClass('js-autocomplete__results')) {
      jQuery('.js-autocomplete__results').remove();
    }
  });

  //prevent form submit when enter key pressed in the input field.
  jQuery('.js-autocomplete__input').keydown(function(event) {
    if (isEnterKey(event)) {
      event.preventDefault();
    }
  });
});

function onResultKeyPressed(autocomplete, event, selectItemAction,
    selectItemFunction) {
  var currentSelected = jQuery(autocomplete).find('.js-autocomplete__results')
      .children('.is-selected');

  if (isEnterKey(event)) {
    event.preventDefault();
    if (currentSelected.length != 0) {
      onSelectItem(currentSelected, selectItemAction, selectItemFunction);
    }
  } else if (event.keyCode === 40) {
    // key: down
    deselectRow(currentSelected);
    if (currentSelected.length === 0
        || jQuery(currentSelected).next().length === 0) {
      selectRow(jQuery(autocomplete).find('.js-autocomplete__results')
          .children('li').first());
    } else {
      selectRow(jQuery(currentSelected).next("li"));
    }
  } else if (event.keyCode === 38) {
    // key: up
    deselectRow(currentSelected);
    if (currentSelected.length === 0) {
      selectRow(jQuery(autocomplete).find('.js-autocomplete__results')
          .children('li').last());
    } else {
      selectRow(jQuery(currentSelected).prev("li"));
    }
  }
}

function onSelectItem(row, selectItemAction, selectItemFunction) {
  selectItemAction(jQuery(row).children("input").first().val());
  if (selectItemFunction) {
    selectItemFunction(row);
  }
  jQuery(row).parent().parent().parent().children("input").first().val(
    jQuery(row).children("input").eq(1).val());
  jQuery(row).parent().remove();
}

function selectRow(row) {
  jQuery(row).addClass("is-selected");
}

function deselectRow(row) {
  jQuery(row).removeClass("is-selected");
}

function isArrowKey(keyCode) {
  return keyCode === 38 || keyCode === 40 || keyCode === 39 || keyCode === 37;
}

function isEnterKey(event) {
  return event.keyCode === 13;
}

function isEscapeKey(event) {
  return event.keyCode === 27;
}

function onInputFocus(inputField, renderResultFn) {
  if (jQuery(inputField).next('input').next('input').val() == 'true') {
    renderResultFn(jQuery(inputField).val());
  }
}

function onValueChange(inputField, event, renderResultFn, resetFn) {
  if (event.keyCode === 27) {
    // key: ESC
    jQuery(inputField).select().parent().find('.js-autocomplete__results').remove();
  } else if (hasValueChanged(inputField)) {
    var minLength = parseInt(jQuery(inputField).next().val());
    if (jQuery(inputField).val().length >= minLength) {
      renderResultFn(jQuery(inputField).val());
    }
    else {
      if(resetFn) resetFn()
    }
  }
}

function hasValueChanged(element) {
  var $elem = jQuery(element);
  originalValue = $elem.attr('data-original-value');
  if($elem.val() === originalValue) {
    return false;
  }
  else {
    $elem.attr('data-original-value', $elem.val());
    return true;
  }
}

function registerMouseEvent(autocompleteId, selectItemAction,
    selectItemFunction) {
  var results = jQuery("[id='" + autocompleteId + "']")
      .find('.js-autocomplete__results').children('.js-autocomplete__result');
  results.each(function() {
        jQuery(this).mouseover(function() {
          selectRow(this);
        });

        jQuery(this).mouseout(function() {
          deselectRow(this);
        });

        jQuery(this).click(function() {
          onSelectItem(this, selectItemAction, selectItemFunction);
        });
      });

  var firstResult = results.first();
  if (firstResult.length != 0) {
    selectRow(firstResult);
  }
}

function filterList(input, filterFn) {
  filterFn(jQuery(input).val());
}

/* ----------------------------------------------------------- */
/*------------------zanata-sortlist component------------------*/
/* ----------------------------------------------------------- */
jQuery(document).ready(
    function() {
      jQuery('a.js-sort-option').each(
          function() {
            jQuery(this).click(
                function() {
                  jQuery(this).parent().siblings("li").children(
                      "a.js-sort-option").removeClass('is-active');
                  jQuery(this).addClass("is-active");
                });
          });
    });

/* ----------------------------------------------------------- */
/*----------------- zanata-checkbox component -----------------*/
/* ----------------------------------------------------------- */

function onCheckboxValueChanged(checkbox, jsFunction) {
  var isChecked = !jQuery(checkbox).children(".js-form__checkbox__input").is(
      ':checked');
  var key = jQuery(checkbox).children("input").first().val();
  jsFunction(key, isChecked);
}
