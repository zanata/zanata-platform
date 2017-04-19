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
import IconButton from '../IconButton'
import React, { PropTypes } from 'react'
import { Panel } from 'react-bootstrap'

const { func } = PropTypes

/**
 * Multiple-field search input that will suggest fields as the user types.
 *
 * Includes an advanced search panel that can be shown to input fields using
 * more appropriate widgets (e.g. username field that suggests usernames, date
 * input with calendar widget).
 */
const EditorSearchInput = React.createClass({

  propTypes: {
    toggleDisplay: func.isRequired,
    text: PropTypes.string.isRequired,
    updateText: PropTypes.func.isRequired
  },

  getDefaultProps: () => {
    return {
      focused: false
    }
  },

  getInitialState: () => {
    return {
      focused: false,
      open: false
    }
  },

  onFocus: function () {
    this.setState({
      focused: true
    })
  },

  onBlur: function (event) {
    // Note: Not strictly needed since it will blur then immediately focus when
    //       changing focus to another child of the div, but this is just in
    //       case there will be some delay that could cause a flicker in the UI.
    if (!event.currentTarget.contains(document.activeElement)) {
      this.setState({
        focused: false
      })
    }


  },

  openPanel: function () {
    this.setState({
      open: true
    }, () => {
      this.refs.input.open()
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
    return (
      <span className="InputGroup-addon">
        <IconButton icon="cross"
          title="Clear search"
          iconSize="n1"
          onClick={() => this.props.updateText('')}/>
      </span>
    )
  },

  render: function () {
    const fields = [
      { key: 'text', placeholder: 'source and target text'},
      { key: 'resource-id', placeholder: 'exact Resource ID for a string'},
      { key: 'last-modified-by', placeholder: 'username'},
      { key: 'last-modified-before', placeholder: 'date in format yyyy/mm/dd'},
      { key: 'last-modified-after', placeholder: 'date in format yyyy/mm/dd'},
      { key: 'source-comment', placeholder: 'source comment text'},
      { key: 'translation-comment', placeholder: 'translation comment text'},
      { key: 'msgctxt', placeholder: 'exact Message Context for a string'},
    ]

    const items = fields.map(field => (
      <li key={field.key} className="inline-search-list">
        {field.key + ':'}
        <div
          className="InputGroup--outlined InputGroup--wide InputGroup--rounded">
          <input ref={field.key}
             type="text"
             placeholder={field.placeholder}
             className="InputGroup-input" />
        </div>
      </li>
    ))

    return (
      <div
        onBlur={this.onBlur}
        onFocus={this.onFocus}>
        <div className={
          cx('InputGroup InputGroup--outlined InputGroup--rounded',
            { 'is-focused': this.state.focused })}>
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
            onChange={(event) => this.props.updateText(event.target.value)}
            onClick={this.state.open}
            className="InputGroup-input u-sizeLineHeight-1_1-4" />
            {this.clearButtonElement()}
        </div>
        <Panel collapsible expanded={this.state.focused}>
          <ul>
            {items}
          </ul>
        </Panel>
      </div>
    )
  }
})

export default EditorSearchInput
