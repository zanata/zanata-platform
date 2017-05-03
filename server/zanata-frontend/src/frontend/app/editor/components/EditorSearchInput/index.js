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
import IconButton from '../IconButton'
import React, { PropTypes } from 'react'
import { Panel, Button } from 'react-bootstrap'
import { map } from 'lodash'

const fields = {
  resourceId: {
    label: 'Resource ID',
    description: 'exact Resource ID for a string'
  },
  lastModifiedBy: {
    label: 'Last modified by',
    description: 'username'
  },
  lastModifiedBefore: {
    label: 'Last modified before',
    description: 'date in format yyyy/mm/dd'
  },
  lastModifiedAfter: {
    label: 'Last modified after',
    description: 'date in format yyyy/mm/dd'
  },
  sourceComment: {
    label: 'Source comment',
    description: 'source comment text'
  },
  translationComment: {
    label: 'Translation comment',
    description: 'translation comment text'
  },
  msgctxt: {
    label: 'msgctxt (gettext)',
    description: 'exact Message Context for a string'
  },
}

/**
 * Multiple-field search input that will suggest fields as the user types.
 *
 * Includes an advanced search panel that can be shown to input fields using
 * more appropriate widgets (e.g. username field that suggests usernames, date
 * input with calendar widget).
 */
const EditorSearchInput = React.createClass({

  propTypes: {
    advanced: PropTypes.bool.isRequired,
    search: PropTypes.shape({
      text: PropTypes.string.isRequired,
      resourceId: PropTypes.string.isRequired,
      lastModifiedBy: PropTypes.string.isRequired,
      lastModifiedBefore: PropTypes.string.isRequired,
      lastModifiedAfter: PropTypes.string.isRequired,
      sourceComment: PropTypes.string.isRequired,
      translationComment: PropTypes.string.isRequired,
      msgctxt: PropTypes.string.isRequired
    }).isRequired,
    toggleAdvanced: PropTypes.func.isRequired,
    updateSearch: PropTypes.func.isRequired
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

  toggleAdvanced: function () {
    this.props.toggleAdvanced()
    // click on Advanced steals focus, so give focus back.
    this.focusInput()
  },

  clearAllAdvancedFields: function () {
    this.props.updateSearch({
      resourceId: '',
      lastModifiedBy: '',
      lastModifiedBefore: '',
      lastModifiedAfter: '',
      sourceComment: '',
      translationComment: '',
      msgctxt: ''
    })
    // click on "Clear all" removes focus from input fields, so give focus back.
    this.focusInput()
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
          onClick={() => this.props.updateSearch({
            text: '' // FIXME include all the other search parameters?
          })}/>
      </span>
    )
  },

  render: function () {
    const { advanced } = this.props

    const advancedFields = map(fields, (value, key) => (
      <li key={key} className="inline-search-list" title={value.description}>
        {value.label + ':'}
        <div
          className="InputGroup--outlined InputGroup--wide InputGroup--rounded">
          <input ref={key}
            type="text"
            placeholder={value.description}
            className="InputGroup-input"
            value={this.props.search[key]}
            onChange={(event) => this.props.updateSearch({
              [key]: event.target.value
            })}
          />
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
              className="n1" />
          </span>
          <input ref="input"
            type="search"
            placeholder="Search source and target text"
            maxLength="1000"
            value={this.props.search.text}
            onChange={(event) =>
              this.props.updateSearch({ text: event.target.value})}
            onClick={this.state.open}
            className="InputGroup-input u-sizeLineHeight-1_1-4" />
          {this.clearButtonElement()}
          <span className="InputGroup-addon btn-xs advsearch btn-link"
            onClick={this.toggleAdvanced}>
            {advanced ? 'Hide advanced' : 'Advanced'}</span>
        </div>
        <Panel collapsible expanded={this.props.advanced && this.state.focused}>
          <ul>
            {advancedFields}
          </ul>
          <Button bsStyle="link" bsSize="xsmall" className="clearadvsearch"
            onClick={this.clearAllAdvancedFields}>
            Clear all
          </Button>
        </Panel>
      </div>
    )
  }
})

export default EditorSearchInput
