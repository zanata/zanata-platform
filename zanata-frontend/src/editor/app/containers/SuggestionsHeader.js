/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
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

import React, { PropTypes } from 'react'
import { Icon } from 'zanata-ui'
import IconButton from '../components/IconButton'
import IconButtonToggle from '../components/IconButtonToggle'
import SuggestionSearchInput from '../components/SuggestionSearchInput'
import ToggleSwitch from '../components/ToggleSwitch'

/**
 * Header of the suggestions panel, with some controls and
 * the search input.
 */
const SuggestionsHeader = React.createClass({

  propTypes: {
    searchType: PropTypes.oneOf(['phrase', 'text']).isRequired,
    showDiff: PropTypes.bool.isRequired,
    onDiffChange: PropTypes.func.isRequired,
    phraseSelected: PropTypes.bool.isRequired,
    closeSuggestions: PropTypes.func.isRequired,
    search: PropTypes.shape({
      text: PropTypes.string,
      loading: PropTypes.bool.isRequired,
      toggle: PropTypes.func.isRequired,
      clear: PropTypes.func.isRequired,
      changeText: PropTypes.func.isRequired,
      input: PropTypes.shape({
        text: PropTypes.string.isRequired
      }).isRequired,
      searchStrings: PropTypes.arrayOf(PropTypes.string).isRequired,
      suggestions: PropTypes.array.isRequired
    }).isRequired
  },

  getDefaultProps: () => {
    return {
      search: {
        text: ''
      }
    }
  },

  setSearchInput (ref) {
    this.searchInput = ref
  },

  /**
   * Need to access refs to focus after the clear is complete
   */
  clearAndFocus: function () {
    // debugger
    if (this.searchInput) {
      // FIXME getting stack overflow here
      // Call stack alternates between this line and a line in searchInput
      // that calls this.props.clearSearch... just a plain old infinite loop
      this.searchInput.clearSearch()
      this.searchInput.focusInput()
    }
  },

  render: function () {
    const textSearchSelected = this.props.searchType === 'text'
    const showSearch = textSearchSelected || !this.props.phraseSelected
    const searchInput = showSearch
      ? <div className="Editor-suggestionsSearch u-sPB-1-4">
        <SuggestionSearchInput
          ref={this.setSearchInput}
          text={this.props.search.input.text}
          loading={this.props.search.loading}
          hasSearch={this.props.search.searchStrings.length !== 0}
          resultCount={this.props.search.suggestions.length}
          clearSearch={this.props.search.clear}
          onTextChange={this.props.search.changeText} />
      </div>
      : undefined

    return (
      <nav className="Editor-suggestionsHeader u-bgHighest u-sPH-3-4">
        <h2 className="Heading--panel u-sPV-1-4 u-floatLeft u-sizeHeight-1_1-2">
          <span className="u-textMuted">
            <Icon name="suggestions" size="0" />
          </span>
          Suggestions
        </h2>
        <div className="u-floatRight">
          <ul className="u-listHorizontal u-textCenter">
          {/*
            <li className="u-smv-1-4">
              <a className="Link--neutral u-sizeHeight-1_1-2"
                title="Auto-fill" ng-click="suggestions.toggleAutofill()">
                (switch icon)
                Auto-fill
              </a>
            </li>
          */}
            <li className="u-sM-1-4">
              <ToggleSwitch
                id="difference-toggle"
                label="Difference"
                isChecked={this.props.showDiff}
                onChange={this.props.onDiffChange} />
            </li>
            <li className="u-sM-1-8">

              <IconButtonToggle
                icon="search"
                title="Search suggestions"
                onClick={this.props.search.toggle}
                active={showSearch}
                disabled={!this.props.phraseSelected} />
            </li>
            <li>
              <IconButton
                icon="cross"
                title="Close suggestions"
                onClick={this.props.closeSuggestions}
                className="Link--neutral u-sizeHeight-1_1-2 u-sizeWidth-1_1-2"
              />
            </li>
          </ul>
        </div>
        {searchInput}
      </nav>
    )
  }
})

export default SuggestionsHeader
