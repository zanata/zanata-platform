/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

/* Functions used in the language settings template for projects and versions.
 *
 * Uses helper functions from components-script.js
 */


/**
 * Show the inline form to enter a locale alias on the row of a specified
 * locale.
 *
 * @param currentAlias the current alias that should be shown in the input
 *        textbox initially.
 */
function showLocaleAliasInput(localeId, currentAlias) {
    var input = localeAliasInput(localeId);
    input.val(currentAlias);
    localeAliasForm(localeId).removeClass('is-hidden');
    showLocaleAliasCancelEdit(localeId);
    input.focus();
}

/**
 * Inverse of showLocaleAliasInput.
 */
function hideLocaleAliasInput(localeId) {
    localeAliasForm(localeId).addClass('is-hidden');
}

/**
 * Get the locale alias input form for a given locale.
 *
 * @returns jQuery object of the form
 */
function localeAliasForm(localeId) {
    var id = '#locale-alias-form-' + localeId;
    return jQuery(id);
}

/**
 * Get the locale alias input textbox for a given locale.
 *
 * @returns jQuery object of the textbox
 */
function localeAliasInput(localeId) {
    return localeAliasForm(localeId).find('input:text');
}

/**
 * Show the actions menu for a locale.
 *
 * This hides the locale alias loader and cancel button.
 */
function showLocaleActions(localeId) {
    var localeActions = '#language-actions-' + localeId;
    showOnlyOneOfLocaleActionStatus(localeId)(localeActions)
}

/**
 * Show the locale alias loading indicator for a locale.
 *
 * This hides the actions menu and cancel button.
 */
function showLocaleAliasLoader(localeId) {
    var aliasLoader = '#language-processing-' + localeId;
    showOnlyOneOfLocaleActionStatus(localeId)(aliasLoader)
}

/**
 * Show the button that cancels editing a locale alias.
 *
 * This hides the actions menu and loading indicator.
 */
function showLocaleAliasCancelEdit(localeId) {
    var cancelEdit = '#edit-alias-cancel-' + localeId;
    showOnlyOneOfLocaleActionStatus(localeId)(cancelEdit)
}

/**
 * Generate a function for a given locale that will show a specified element
 * in the actions area of that locale's row.
 *
 * The returned function takes a jQuery selector for the element to show.
 *
 * Usage examples:
 *
 *     // Create and use function immediately
 *     showOnlyOneOfLocaleActionStatus('en')('#language-processing-en');
 *
 *     // Create and cache function, then use it later
 *     var showOnly = showOnlyOneOfLocaleActionStatus('jp');
 *     // ...
 *     showOnly('#language-actions-jp');
 *
 * @returns function that takes a selector
 */
function showOnlyOneOfLocaleActionStatus (localeId) {
    return showOnlyOneOf('#language-actions-' + localeId,
        '#language-processing-' + localeId,
        '#edit-alias-cancel-' + localeId)
}

/**
 * Check a keydown event for Enter or Esc to submit or cancel a new locale alias.
 *
 * @param localeId which locale the alias is for
 * @param event 'keydown' event on the textbox
 * @returns {boolean} false if Enter or Esc was pressed, otherwise undefined
 */
function onLocaleAliasInputKeyDown(localeId, event) {

    if (isEnterKey(event)) {
        event.preventDefault();
        localeAliasForm(localeId).find('input:submit').click();
        return false;
    }
    if (isEscapeKey(event)) {
        event.preventDefault();
        jQuery('#edit-alias-cancel-' + localeId).find('button').click();
        return false;
    }
}

/**
 * Trigger removal of a locale alias.
 *
 * This just simulates entering nothing in the locale alias textbox and
 * submitting it.
 *
 * @param localeId locale for which to remove the alias
 * @returns {boolean} false
 */
function deleteLocaleAlias(localeId) {
    var aliasForm = localeAliasForm(localeId);
    var input = aliasForm.find('input:text');
    input.val('');
    var submitButton = aliasForm.find('input:submit');
    submitButton.click();
    return false;
}

/**
 * Get a function to handle keyup on locale filter textboxes.
 *
 * Uses an IIFE to generate a closure so that 'oninput' support does not have
 * to be checked on every invocation (it is assumed that the browser will not
 * gain additional functionality during execution).
 */
var onFilterLocalesTextboxKeyUp = (function () {
    var inputEventSupported = isInputEventSupported();

    return onFilterLocalesTextboxKeyUp;

    /**
     * Respond to user typing in a locale filter textbox.
     *
     * This just prevents Enter from submitting, but if the 'oninput' event is
     * not supported it will also trigger filtering of the list.
     *
     * textboxSelector: jQuery selector for the textbox that the user typed in
     * targetListSelector: jQuery selector for the list that is to be filtered
     * event: the event generated by the user typing (e.g. keyUp).
     */
    function onFilterLocalesTextboxKeyUp(textboxSelector, targetListSelector, event) {
        if (isEnterKey(event)) {
            event.preventDefault();
            return false;
        } else if (!inputEventSupported) {
            filterLocalesFromTextBox(textboxSelector, targetListSelector);
        }
    }

    /**
     * Determine whether the 'input' event is supported by the current browser.
     */
    function isInputEventSupported() {
        var eventName = 'oninput';
        var el = document.createElement('input');
        var isSupported = (eventName in el);
        if (!isSupported) {
            el.setAttribute(eventName, 'return;');
            isSupported = typeof el[eventName] === 'function';
        }
        el = null;
        return isSupported;
    }
})();

/**
 * Filter a given locale list based on the value of a given textbox.
 */
function filterLocalesFromTextBox(textboxSelector, targetListSelector) {
    var $ = jQuery;

    var textbox = $(textboxSelector).first();
    var filterText = textbox.val();
    var previousFilterText = textbox.data('previous-value');

    // previousFilterText will be undefined initially, so the filter will always
    // run the first time.
    if (filterText === previousFilterText) {
        return;
    }

    // going ahead with a new filter value, store the new filter value to
    // compare next time.
    textbox.data('previous-value', filterText);

    var targetList = $(targetListSelector);

    if (filterText.length === 0) {
        // no filter, display all
        setLanguageListItemsVisibility(targetList, function () { return true; });
    } else {
        setLanguageListItemsVisibility(targetList, function (listItem) {
            var name = listItem.find('.js-locale-name').text();
            var code = listItem.find('.js-locale-id').text();
            var alias = listItem.find('.js-locale-alias').text();

            return matches(name) || matches(code) || matches(alias);

            function matches (text) {
                text = text.toLowerCase();
                var filter = filterText.toLowerCase();
                return text.indexOf(filter) !== -1;
            }
        });
    }

    // Make sure hidden checkboxes are not checked (sets flags for check state).
    unselectHiddenItems(targetListSelector);

    // Make sure revealed checkboxes have their previous check state restored.
    setCheckStateFromFlags(targetListSelector);

    /**
     * Set list items to is-hidden or not based on a predicate.
     *
     * If the predicate returns true for the element, it will not have the class
     * is-hidden, otherwise the element will have the class is-hidden.
     *
     * The predicate should expect a jquery object of a single element.
     */
    function setLanguageListItemsVisibility(targetList, predicate) {
        var $ = jQuery;
        var showAny = false;

        var items = targetList.children();

        items.each(function (index, element) {
            var $element = $(element);
            var show = predicate($element);
            $element.toggleClass('is-hidden', !show);
            showAny = showAny || show;
        });

        var emptyMessageSelector = targetList.data('empty-results-indicator');
        var emptyMessage = $(emptyMessageSelector);
        var searchTextHolder = emptyMessage.find('.js-search-term');
        // filterText is defined in the parent scope of this function.
        searchTextHolder.text(filterText);
        emptyMessage.toggleClass('is-hidden', showAny);
        // The list itself is shown or hidden so that its specific styles do not
        // make the layout look strange.
        targetList.toggleClass('is-hidden', !showAny);
    }

    /**
     * Given a selector for a list of items, ensure that no items with class
     * is-hidden are selected.
     */
    function unselectHiddenItems(listSelector) {
        var checkedItems = $(listSelector)
            .find('input.js-form__checkbox__input:hidden:checked');
        checkedItems.data('checked-when-visible', true);
        checkedItems.prop('checked', false)
            .change();
    }

    /*
     * Finds checkboxes in the list that are visible and flagged to be checked
     * when visible, makes sure they are checked and removes the flag.
     */
    function setCheckStateFromFlags(listSelector) {
        var list = $(listSelector);
        var items = list.find('input.js-form__checkbox__input:visible');
        items.each(function (i, elem) {
            var checkbox = $(elem);
            if (checkbox.data('checked-when-visible')) {
                checkbox.removeData('checked-when-visible');
                checkbox.prop('checked', true).change();
            }
        });
    }
}
