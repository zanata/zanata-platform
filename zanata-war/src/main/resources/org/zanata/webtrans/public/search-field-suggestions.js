
// could offer to close quotes if quotes are open

// could offer an 'exact match' option if a range is selected, which would place quotes around it (if it is not already quoted)
// when no range selected, could have an exact match option that inserts "exact search text here" with the contents of the quotes in the selected range

window.searchSuggestions = (function () {

  function init (wrapper, valueChangeCallback) {
    var i;
    var resultList = wrapper.querySelector('.js-suggest__results');
    var waitingList = document.createElement('ul');
    waitingList.className = 'is-invisible';
    // insert waitingList after resultList
    wrapper.insertBefore(waitingList, resultList.nextSibling);

    var resultElements = resultList.querySelectorAll('.js-suggest__result');

    for (i=0; i < resultElements.length; i++) {
      waitingList.appendChild(resultElements[i]);
    }

    function selectTargetResult (result) {
      clearResultSelection();
      result.classList.add('is-selected');
    }

    function clearResultSelection () {
      for (var i=0; i < resultElements.length; i++) {
        resultElements[i].classList.remove('is-selected');
      }
    }

    function insertTargetResult (result) {
      result.classList.remove('is-selected');
      insertKey(result.dataset.key);
    }

    var input = wrapper.querySelector('.js-suggest__input');

    function insertKey (key) {
      var currentCursor = input.selectionStart;
      // TODO damason adding the colon here doesn't seem right, seems too fragile. Find a more sensible place to add it.
      var newValue = replaceTargetWord(input.value, currentCursor, key + ':');
      var newCursor = getEndPosOfCursorWord (newValue, currentCursor);
      updateQueryAndCursor(newValue, newCursor);
    }

    function updateQueryAndCursor(query, cursorPos) {
      input.value = query;
      input.setSelectionRange(cursorPos, cursorPos);
      calculateSuggestions();
    }

    function attachResultEvents (element) {
      element.addEventListener('mousemove', function () { selectTargetResult(element); });
      element.addEventListener('mousedown', function () { insertTargetResult(element); });
    }

    for (i=0; i < resultElements.length; i++) {
      attachResultEvents(resultElements[i]);
    }

    var suggest = suggestFromElements(resultElements);

    function calculateSuggestions () {
      var ele,
          suggested = suggest(getCursorWord(input.value, input.selectionStart));
      showSuggestions();
      // move non-matching onto waitinglist
      for (var i=0; i < resultElements.length; i++) {
        ele = resultElements[i];
        if (suggested.indexOf(ele) === -1) {
          ele.classList.remove('is-selected');
          waitingList.appendChild(ele);
        }
      }
      // move matching onto resultlist in order
      for (i=0; i<suggested.length; i++) {
        resultList.appendChild(suggested[i]);
      }
    }

    function hideSuggestions () {
      resultList.classList.add('is-invisible');
    }

    function showSuggestions () {
      resultList.classList.remove('is-invisible');
    }

    input.addEventListener('keyup', function (e) {
      if (e.keyCode === 13) {
          e.preventDefault();
      }
      calculateSuggestions();
    });
    input.addEventListener('keypress', function (e) {
      if (e.keyCode === 13) {
        e.preventDefault();
      }
    });

    input.addEventListener('focus', calculateSuggestions);
    input.addEventListener('blur', function () {
      // clicking a suggestion will cause blur, but hiding suggestions
      // interferes with the click event on the suggestion.
      // if a suggestion was clicked, focus will return to the input.
      setTimeout(function () {
        if (document.activeElement !== input) {
          hideSuggestions();
        }
      }, 100);

    });
    input.addEventListener('click', function () {
      clearResultSelection();
      calculateSuggestions();
    });

    // FIXME this function looks slightly daunting, make it more elegant
    input.addEventListener('keydown', function (e) {
      var key = e.keyCode,
          selected = resultList.querySelector('.is-selected'),
          nextElement;
      if (key === 27) { // Esc
        selected.classList.remove('is-selected');
      } else if ((key === 13 || key === 39) && selected) { // Enter or Right-arrow
        e.preventDefault();
        insertTargetResult(selected);
      } else if (key === 13) { // Enter with no selection, trigger search
        valueChangeCallback(input.value);
      } else if (key === 38 && selected) { // Up arrow
        e.preventDefault();
        nextElement = selected.previousElementSibling;
        if (nextElement) {
          selectTargetResult(nextElement);
        } else {
          clearResultSelection();
        }
      } else if (key === 40) { // Down arrow
        e.preventDefault();
        if (selected) {
          nextElement = selected.nextElementSibling;
          if (nextElement) {
            selectTargetResult(nextElement);
          }
        } else {
          nextElement = resultList.querySelector('.js-suggest__result');
          if (nextElement) {
            nextElement.classList.add('is-selected');
          }
        }
      }
    });


    /*
     * Returns the word that the cursor is touching. Empty string if the cursor is not touching a word. null if the cursor is in a quoted section.
     * TODO damason returning '' or null is a bit crude, find a better way.
     *
     * cursor: int position within query of cursor
     */
    function getCursorWord (query, cursor) {
      if (cursor < 0 || cursor > query.length) {
        console.error("cursor is outside of string");
      }

      var left = query.slice(0, cursor),
          right = query.slice(cursor);

      // This will match only balanced strings (FIXME name better)
      var isQuoted = /^(?:[^\\"]|\\.|"(?:[^\\"]|\\.)*?")*$/g.exec(left);
      if (!isQuoted) {
        return null; // no key contains null, so nothing is suggested in quoted sections.
      }

      var leftMatch = /^.*?(\S*)$/g.exec(left)[1];
      if (leftMatch.length === 0) {
        // in front of word, counts as no word
        return '';
      }

      // join non-whitespace before the cursor to non-whitespace after the cursor
      return leftMatch + /^([^:\s]*):?.*?$/g.exec(right)[1];
    }

    // replacement already has : on it
    function replaceTargetWord (query, cursor, replacement) {
      if (cursor < 0 || cursor > query.length) {
        // if outside range, just append it
        return query + replacement;
      }

      var left = query.slice(0, cursor),
          leftMatches = /^(.*?)(\S*)$/g.exec(left),
          right = query.slice(cursor);

      if (leftMatches[2].length === 0) {
        return left + replacement + right;
      } else {
        return leftMatches[1] + replacement + /^[^:\s]*:?(.*?)$/g.exec(right)[1];
      }
    }

    function getEndPosOfCursorWord (query, cursor) {
      if (cursor < 0 || cursor > query.length) {
        // if outside range, just put cursor at the end
        return query.length;
      }
      var right = query.slice(cursor);
      return cursor + /^([^:\s]*:?).*?$/g.exec(right)[1].length
    }

    /*
     * returns a function that will take a word and return suggestions from the given elements
     * elements is a node list with data-key holding the keys to suggest.
     */
    function suggestFromElements(elements) {
      return (function (word) {
        var i, prefixed = [], contained = [], element, key, pos;
        // keys is what I need to search in

        // first, show keys that start with word
        // second, show keys that match but don't start with the word

        for (i = 0; i < elements.length; i++) {
          element = elements[i];
          key = element.dataset.key;
          pos = key.indexOf(word);
          if (pos === 0) {
            prefixed.push(element);
          } else if (pos > 0) {
            contained.push(element);
          }
        }
        return prefixed.concat(contained);
      });
    }

  }

  return {
    init: init
  };
})();
