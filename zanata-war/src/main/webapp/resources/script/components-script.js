// Escapes special characters and returns a valid jQuery selector
function jqSelector(str) {
  return str.replace(/([;&,\.\+\*\~':"\!\^#$%@\[\]\(\)=>\|])/g, '\\$1');
}

jQuery(document).ready(function() {
  registerJsTab();
});

var globalMessageTimer;

// automatically clear global message after 5 seconds
function startGlobalMessageTimer() {
  if (zanata) {
    zanata.messages.activate('#messages .message--global');
  } else {
    jQuery('#messages .message--global').addClass('is-active');
  }

  //stop previous timeout counter
  clearTimeout(globalMessageTimer);

  globalMessageTimer = setTimeout(function() {
    if (zanata) {
      zanata.messages.deactivate('#messages .message--global');
    } else {
      jQuery('#messages .message--global').removeClass('is-active');
    }
  }, 5000);
}

function refreshTooltip(wrapperId) {
  jQuery('#' + wrapperId).find('[title]').each(function() {
    zanata.tooltip.init(this);
  });
}

function registerJsTab() {
  jQuery('.js-tab').each(function() {
    jQuery(this).click(function() {
      onTabClick(this);
    });
  });
}

function onTabClick(tab) {
  jQuery(tab).parent().siblings("li").children("a").removeClass('is-active');
  jQuery(tab).addClass("is-active");
  jQuery(tab).parents('.tabs--lined').children('.tabs__content')
      .children('div').addClass('is-hidden');
  jQuery('#' + jQuery(tab).attr('id') + '_content').removeClass('is-hidden');
}

function updateStateFromUrl() {
  crossroads.parse(window.location.pathname);
}

function updateUrl(urlPrefix, suffixToUpdate) {
  var newUrl = window.location.pathname;
  newUrl = newUrl.substring(0, newUrl.indexOf(urlPrefix) + urlPrefix.length)
      + suffixToUpdate;
  var status = {
    path : newUrl
  }
  window.history.pushState(status, document.title, newUrl)
  updateStateFromUrl();
}

jQuery(function() {
  jQuery(window).on("popstate", function(event) {
    var state = event.originalEvent.state
    if (state)
      crossroads.parse(state.path)
  })
})

function checkHashUrl(defaultTabId, defaultSettingsTabId) {
  var originalHashUrl = window.location.hash;

  // if hash equals #settings-{xyz}, update hash to #settings
  if (window.location.hash) {
    if (window.location.hash.substring(0, 9) == '#settings') {
      window.location.hash = "#settings";
    }

    if (elementExists(window.location.hash)) {
      defaultTabId = window.location.hash;
    }
  }
  onTabClick(defaultTabId);
  window.location.hash = defaultTabId;

  if (window.location.hash.substring(0, 9) == "#settings") {
    handleSettingsTab(defaultSettingsTabId, originalHashUrl);
  }
}

function handleSettingsTab(defaultSettingsTabId, hashUrl) {
  var selectedSettingsTabId = defaultSettingsTabId;
  if (elementExists(hashUrl)) {
    selectedSettingsTabId = hashUrl;
  }
  jQuery(selectedSettingsTabId)[0].click();
}

function elementExists(hashId) {
  return jQuery(hashId).length != 0;
}

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
  jQuery('#' + listId).children('li').removeClass('is-active');
  jQuery('#' + listId).children('li').children("a").removeClass('is-active');
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
  } else if (event.keyCode == 40) {
    // key: down
    deselectRow(currentSelected);
    if (currentSelected.length == 0
        || jQuery(currentSelected).next().length == 0) {
      selectRow(jQuery(autocomplete).find('.js-autocomplete__results').children(
          'li').first());
    } else {
      selectRow(jQuery(currentSelected).next("li"));
    }
  } else if (event.keyCode == 38) {
    // key: up
    deselectRow(currentSelected);
    if (currentSelected.length == 0) {
      selectRow(jQuery(autocomplete).find('.js-autocomplete__results').children(
          'li').last());
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
  jQuery(row).parent().parent().parent().children("input").first().val("");
  jQuery(row).parent().remove();
}

function selectRow(row) {
  jQuery(row).addClass("is-selected");
}

function deselectRow(row) {
  jQuery(row).removeClass("is-selected");
}

function isArrowKey(keyCode) {
  return keyCode == 38 || keyCode == 40 || keyCode == 39 || keyCode == 37;
}

function isEnterKey(event) {
  return event.keyCode == 13;
}

function onInputFocus(inputField, renderResultFn) {
  if (jQuery(inputField).next('input').next('input').val() == 'true') {
    renderResultFn(jQuery(inputField).val());
  }
}

function onValueChange(inputField, event, renderResultFn) {
  if (!isArrowKey(event.keyCode)) {
    var minLength = parseInt(jQuery(inputField).next().val());
    if (jQuery(inputField).val().length >= minLength) {
      renderResultFn(jQuery(inputField).val());
    }
  }
}

function registerMouseEvent(autocompleteId, selectItemAction,
    selectItemFunction) {
  jQuery("[id='" + autocompleteId + "']").find('.js-autocomplete__results')
      .children('.js-autocomplete__result').each(function() {
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

  var firstResult = jQuery("[id='" + autocompleteId + "']").find(
      '.js-autocomplete__results').children('.js-autocomplete__result').first();
  if (firstResult.length != 0) {
    selectRow(firstResult);
  }
}

function filterList(input, filterFn) {
  filterFn(jQuery(input).val());
}

/* ----------------------------------------------------------- */
/*----------------- zanata-checkbox component -----------------*/
/* ----------------------------------------------------------- */

function onCheckboxValueChanged(checkbox, jsFunction) {
  var isChecked = !jQuery(checkbox).children(".js-form__checkbox__input").is(
      ':checked');
  var key = jQuery(checkbox).children("input").first().val();
  jsFunction(key, isChecked);
}
