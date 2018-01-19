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

import cx from 'classnames'
import { Icon } from '../../../components'
import React from 'react'
import PropTypes from 'prop-types'

/**
 * Styled text input that displays result count.
 */
class SuggestionSearchInput extends React.Component {
  static propTypes = {
    text: PropTypes.string.isRequired,
    onTextChange: PropTypes.func.isRequired,
    loading: PropTypes.bool.isRequired,
    resultCount: PropTypes.number,
    hasSearch: PropTypes.bool.isRequired,
    clearSearch: PropTypes.func.isRequired
  }

  static defaultProps = {
    focused: false
  }

  constructor (props) {
    super(props)
    this.state = {
      // FIXME one other component is interested in this state
      //       just deal with that when I get to it
      focused: false
    }
  }

  clearSearch = () => {
    this.props.clearSearch()
    this.focusInput()
  }

  onFocus = () => {
    this.setState({
      focused: true
    })
  }

  onBlur = () => {
    this.setState({
      focused: false
    })
  }

  focusInput = () => {
    // TODO different approach for React 0.14

    // may not need to actually set focused=true, mainly using for
    // callback, which gets around issues with the component not being
    // properly in the DOM yet
    this.setState({
      focused: true
    }, () => {
      this.refs.input.focus()
    })
  }

  componentDidMount () {
    this.focusInput()
  }

  loadingResultsElement = () => {
    return (
      <span onClick={this.focusInput}
        className="Editor-suggestionsSearchLoader">
        {/* TODO proper loader */}
        Loading…
      </span>
    )
  }

  resultCountElement = () => {
    return (
      <span onClick={this.focusInput}
        className="Editor-suggestionsSearchResults">
        {this.props.resultCount} results
      </span>
    )
  }

  resultsElement = () => {
    if (!this.props.loading && !this.props.hasSearch) {
      return undefined
    }

    const innerElement = this.props.loading
      ? this.loadingResultsElement()
      : this.resultCountElement()

    return (
      <span className="EditorInputGroup-addon">
        {innerElement}
      </span>
    )
  }

  render () {
    return (
      <div className={cx('EditorInputGroup EditorInputGroup--outlined' +
          ' EditorInputGroup--rounded',
                         { 'is-focused': this.state.focused })}>
        <span className="EditorInputGroup-addon"
          onClick={this.focusInput}>
          <Icon name="search"
            title="Search suggestions"
            className="n1" />
        </span>
        <input ref="input"
          type="search"
          placeholder="Search suggestions…"
          maxLength="1000"
          value={this.props.text}
          onChange={this.props.onTextChange}
          className="EditorInputGroup-input u-sizeLineHeight-1_1-4" />
        {this.resultsElement()}
      </div>
    )
  }
}

export default SuggestionSearchInput
