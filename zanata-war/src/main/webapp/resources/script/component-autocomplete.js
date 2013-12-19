/*
 *
 *  * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
 *  * @author tags. See the copyright.txt file in the distribution for a full
 *  * listing of individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it under the
 *  * terms of the GNU Lesser General Public License as published by the Free
 *  * Software Foundation; either version 2.1 of the License, or (at your option)
 *  * any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  * details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License
 *  * along with this software; if not, write to the Free Software Foundation,
 *  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 *  * site: http://www.fsf.org.
 */

jQuery(document).ready(function() {
  jQuery(this).click(function(event) {
    if (jQuery(event.target).attr("class") != 'autocomplete__results') {
      jQuery('.autocomplete__results').remove();
    }
  });

  //prevent form submit when enter key pressed in the input field.
  jQuery('.autocomplete__input').keydown(function(event) {
    if (isEnterKey(event)) {
      event.preventDefault();
    }
  });
});

function onResultKeyPressed(autocomplete, event, selectItemAction,
    selectItemFunction) {
  var currentSelected = jQuery(autocomplete).find('.autocomplete__results')
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
      selectRow(jQuery(autocomplete).find('.autocomplete__results').children(
          'li').first());
    } else {
      selectRow(jQuery(currentSelected).next("li"));
    }
  } else if (event.keyCode == 38) {
    // key: up
    deselectRow(currentSelected);
    if (currentSelected.length == 0) {
      selectRow(jQuery(autocomplete).find('.autocomplete__results').children(
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
  jQuery("[id='" + autocompleteId + "']").find('.autocomplete__results')
      .children('.autocomplete__result').each(function() {
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
      '.autocomplete__results').children('.autocomplete__result').first();
  if (firstResult.length != 0) {
    selectRow(firstResult);
  }
}
