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

// FIXME copied from SuggestionSearchInput. Can pull out a common component.

/**
 * Styled text input that displays result count.
 */
class GlossarySearchInput extends React.Component {
  static propTypes = {
    text: PropTypes.string.isRequired,
    onTextChange: PropTypes.func.isRequired
  }

  constructor (props) {
    super(props)
    this.state = {
      // FIXME one other component is interested in this state
      //       just deal with that when I get to it
      focused: false
    }
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

  setInput = (input) => {
    this.input = input
    if (this.input && this.state.focused) {
      this.input.focus()
    }
  }

  focusInput = () => {
    // may not need to actually set focused=true, mainly using for
    // callback, which gets around issues with the component not being
    // properly in the DOM yet
    this.setState({
      focused: true
    }, () => {
      if (this.input) {
        this.input.focus()
      }
    })
  }

  componentDidMount () {
    this.focusInput()
  }

  render () {
    return (
      <div className="InlineSearch">
        <div className={cx('EditorInputGroup EditorInputGroup--outlined ' +
            'EditorInputGroup--rounded',
          { 'is-focused': this.state.focused })}>
          <span className="EditorInputGroup-addon"
            onClick={this.focusInput}>
            <Icon name="search" className="n1"
              title="Search glossary" />
          </span>
          <input ref={this.setInput}
            type="search"
            placeholder="Search glossary…"
            maxLength="100"
            value={this.props.text}
            onChange={this.props.onTextChange}
            className="EditorInputGroup-input rtl u-sizeLineHeight-1_1-4" />
        </div>
      </div>
    )
  }
}

export default GlossarySearchInput
