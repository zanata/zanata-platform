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
import { Icon } from 'zanata-ui'
import IconButtonToggle from '../IconButtonToggle'
import IconButton from '../IconButton'
import React, { PropTypes } from 'react'

const { func } = PropTypes

/**
 * Styled text input that displays result count.
 */
const EditorSearchInput = React.createClass({

  propTypes: {
    toggleDisplay: func.isRequired,
    text: PropTypes.string.isRequired,
    hasSearch: PropTypes.bool.isRequired,
    clearSearch: PropTypes.func.isRequired,
    showPanel: PropTypes.bool
  },

  clearSearch: function () {
    this.props.clearSearch()
    this.focusInput()
  },

  getDefaultProps: () => {
    return {
      focused: false
    }
  },

  getInitialState: () => {
    return {
      // FIXME one other component is interested in this state
      //       just deal with that when I get to it
      focused: false
    }
  },

  onFocus: function () {
    this.setState({
      focused: true
    })
  },

  onBlur: function () {
    this.setState({
      focused: false
    })
  },

  focusInput: function () {
    // TODO different approach for React 0.14

    // may not need to actually set focused=true, mainly using for
    // callback, which gets around issues with the component not being
    // properly in the DOM yet
    this.setState({
      focused: true
    }, () => {
      this.refs.input.focus()
    })
  },

  componentDidMount: function () {
    this.focusInput()
  },

  clearButtonElement: function () {
    if (!this.props.hasSearch) {
      return undefined
    }
    return (
      <span className="InputGroup-addon">
        <IconButton icon="cross"
          title="Clear search"
          iconSize="n1"
          onClick={this.clearSearch} />
      </span>
    )
  },

  render: function () {
    return (
      <div>
        <div className={cx('InputGroup InputGroup--outlined' +
        ' InputGroup--rounded', { 'is-focused': this.state.focused })}>
          <span className="InputGroup-addon"
            onClick={this.focusInput}>
            <Icon name="search" title="Search"
              size="n1" />
          </span>
          <input ref="input"
            type="search"
            placeholder="Search"
            maxLength="1000"
            value={this.props.text}
            className="InputGroup-input u-sizeLineHeight-1_1-4" />
            {this.clearButtonElement()}
        </div>
        <div className="help-icon">
          <IconButtonToggle icon="help"
            onClick={this.props.toggleDisplay}
            active={this.props.showPanel} />
        </div>
      </div>
    )
  }
})

export default EditorSearchInput
