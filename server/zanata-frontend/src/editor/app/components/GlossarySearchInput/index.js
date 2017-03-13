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
import React from 'react'
import { FormGroup, FormControl }
  from 'react-bootstrap'

// FIXME copied from SuggestionSearchInput. Can pull out a common component.

/**
 * Styled text input that displays result count.
 */
const GlossarySearchInput = React.createClass({

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

  render () {
    return (
      <div className="inline-flex-search">
        <div className={cx('InputGroup InputGroup--outlined ' +
            'InputGroup--rounded',
          { 'is-focused': this.state.focused })}>
          <span className="InputGroup-addon"
            onClick={this.focusInput}>
            <Icon name="search"
              title="Search glossary"
              size="n1" />
          </span>
          <input ref="input"
            type="search"
            placeholder="Search glossary…"
            maxLength="100"
            className="InputGroup-input u-sizeLineHeight-1_1-4" />
        </div>
        <FormGroup controlId="formControlsSelect">
          <FormControl componentClass="select" placeholder="Fuzzy">
            <option value="fuzzy">Fuzzy</option>
            <option value="lucene">Lucene</option>
            <option value="phrase">Phrase</option>
          </FormControl>
        </FormGroup>
      </div>
    )
  }
})

export default GlossarySearchInput
