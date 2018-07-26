// @ts-nocheck
/* eslint-disable max-len */
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

import React from 'react'
import * as PropTypes from 'prop-types'
import { Component } from 'react'
import { connect } from 'react-redux'
import { injectIntl, intlShape, defineMessages } from 'react-intl'
import cx from 'classnames'
import { map } from 'lodash'
import {
  toggleAdvanced,
  updatePhraseFilter
} from '../../actions/phrases-filter-actions'
import Collapse from 'antd/lib/collapse'
import 'antd/lib/collapse/style/css'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
const Panel = Collapse.Panel

/**
 * Multiple-field search input that will suggest fields as the user types.
 *
 * Includes an advanced search panel that can be shown to input fields using
 * more appropriate widgets (e.g. username field that suggests usernames, date
 * input with calendar widget).
 */
export class EditorSearchInput extends Component {
  static propTypes = {
    intl: intlShape,
    showAdvanced: PropTypes.bool.isRequired,
    search: PropTypes.shape({
      searchString: PropTypes.string.isRequired,
      resId: PropTypes.string.isRequired,
      lastModifiedByUser: PropTypes.string.isRequired,
      changedBefore: PropTypes.string.isRequired,
      changedAfter: PropTypes.string.isRequired,
      sourceComment: PropTypes.string.isRequired,
      transComment: PropTypes.string.isRequired,
      msgContext: PropTypes.string.isRequired
    }).isRequired,
    toggleAdvanced: PropTypes.func.isRequired,
    updateSearch: PropTypes.func.isRequired
  }

  constructor (props) {
    super(props)
    this.state = {
      focused: false
    }
  }

  onFocus = () => {
    this.setState({
      focused: true
    })
  }

  onBlur = (event) => {
    // Note: Not strictly needed since it will blur then immediately focus when
    //       changing focus to another child of the div, but this is just in
    //       case there will be some delay that could cause a flicker in the UI.
    if (!event.currentTarget.contains(document.activeElement)) {
      this.setState({
        focused: false
      })
    }
  }

  toggleAdvanced = () => {
    this.props.toggleAdvanced()
    // click on Advanced steals focus, so give focus back.
    this.focusInput()
  }

  clearAllAdvancedFields = () => {
    this.props.updateSearch({
      resId: '',
      lastModifiedByUser: '',
      changedBefore: '',
      changedAfter: '',
      sourceComment: '',
      transComment: '',
      msgContext: ''
    })
    // click on "Clear all" removes focus from input fields, so give focus back.
    this.focusInput()
  }

  setInput = (input) => {
    this.input = input
    // Since the input component is now loaded, make sure it focuses if needed.
    if (this.state.focused) {
      this.focusInput()
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

  clearSearch = () => this.props.updateSearch({
    searchString: '' // FIXME include all the other search parameters?
  })

  updateSearchText = (event) => {
    this.props.updateSearch({ searchString: event.target.value })
  }

  render () {
    const { showAdvanced, intl } = this.props
    const messages = defineMessages({
      resIdLabel: { id: 'EditorSearchInput.resIdLabel', defaultMessage: 'Resource ID' },
      resIdDesc: { id: 'EditorSearchInput.resIdDesc', defaultMessage: 'exact Resource ID for a string' },
      lastModifiedLabel: { id: 'EditorSearchInput.lastModifiedLabel', defaultMessage: 'Last modified by' },
      lastModifiedDesc: { id: 'EditorSearchInput.lastModifiedDesc', defaultMessage: 'username' },
      changedBeforeLabel: { id: 'EditorSearchInput.changedBeforeLabel', defaultMessage: 'Last modified before' },
      changedAfterLabel: { id: 'EditorSearchInput.changedAfterLabel', defaultMessage: 'Last modified after' },
      sourceCommentLabel: { id: 'EditorSearchInput.sourceCommentLabel', defaultMessage: 'Source comment' },
      sourceCommentDesc: { id: 'EditorSearchInput.sourceCommentDesc', defaultMessage: 'source comment text' },
      msgContextLabel: { id: 'EditorSearchInput.msgContextLabel', defaultMessage: 'msgctxt (gettext)' },
      msgContextDesc: { id: 'EditorSearchInput.msgContextDesc', defaultMessage: 'exact Message Context for a string' },
      dateFormat: { id: 'EditorSearchInput.dateFormat', defaultMessage: 'date in format yyyy/mm/dd' },
      searchPlaceholder: { id: 'EditorSearchInput.placeholder', defaultMessage: 'Search source and target text' },
      showAdvanced: { id: 'EditorSearchInput.showAdvanced', defaultMessage: 'Advanced' },
      hideAdvanced: { id: 'EditorSearchInput.hideAdvanced', defaultMessage: 'Hide advanced' },
      clearAll: { id: 'EditorSearchInput.clearAll', defaultMessage: 'Clear all' },
    })
    const fields = {
      resId: {
        label: intl.formatMessage(messages.resIdLabel),
        description: intl.formatMessage(messages.resIdDesc)
      },
      lastModifiedByUser: {
        label: intl.formatMessage(messages.lastModifiedLabel),
        description: intl.formatMessage(messages.lastModifiedDesc)
      },
      changedBefore: {
        label: intl.formatMessage(messages.changedBeforeLabel),
        description: intl.formatMessage(messages.dateFormat)
      },
      changedAfter: {
        label: intl.formatMessage(messages.changedAfterLabel),
        description: intl.formatMessage(messages.dateFormat)
      },
      sourceComment: {
        label: intl.formatMessage(messages.sourceCommentLabel),
        description: intl.formatMessage(messages.sourceCommentDesc)
      },
      msgContext: {
        label: intl.formatMessage(messages.msgContextLabel),
        description: intl.formatMessage(messages.msgContextDesc),
      }
      // transComment: {
      //   label: 'Translation comment',
      //   description: 'translation comment text'
      // },
    }
    const advancedFields = map(fields, (field, key) => (
      <AdvancedField key={key}
        id={key}
        field={field}
        value={this.props.search[key]}
        updateSearch={this.props.updateSearch} />
    ))

    return (
      <div
        onBlur={this.onBlur}
        onFocus={this.onFocus}>
        <div className={
          cx('EditorInputGroup EditorInputGroup--outlined' +
              ' EditorInputGroup--rounded',
            { 'is-focused': this.state.focused })}>
          <input ref={this.setInput}
            type="search"
            placeholder={intl.formatMessage(messages.searchPlaceholder)}
            maxLength="1000"
            value={this.props.search.searchString}
            onChange={this.updateSearchText}
            className="EditorInputGroup-input u-sizeLineHeight-1_1-4" />
          <span className="EditorInputGroup-addon btn-xs btn-link n1"
            onClick={this.toggleAdvanced}>
            {showAdvanced
              ? intl.formatMessage(messages.hideAdvanced)
              : intl.formatMessage(messages.showAdvanced)}
          </span>
        </div>
        <Collapse activeKey={showAdvanced ? '1' : null} onChange={this.toggleAdvanced}
          style={{
            zIndex: '1000',
            position: 'absolute',
            marginBottom: '0.5rem',
            width: '100%'
          }}>
          <Panel key='1' header={undefined} showArrow={false}>
            <span>
              {advancedFields}
              <Button size={'small'} aria-label='button' className="AdvSearch-clear"
                onClick={this.clearAllAdvancedFields}>
                {intl.formatMessage(messages.clearAll)}
              </Button>
            </span>
          </Panel>
        </Collapse>
      </div>
    )
  }
}

class AdvancedField extends Component {
  static propTypes = {
    id: PropTypes.any.isRequired,
    field: PropTypes.shape({
      label: PropTypes.string.isRequired,
      description: PropTypes.string.isRequired
    }).isRequired,
    value: PropTypes.string.isRequired,
    updateSearch: PropTypes.func.isRequired
  }

  updateSearch = (event) => this.props.updateSearch({
    [this.props.id]: event.target.value
  })

  render () {
    const { id, field, value } = this.props
    const { label, description } = field
    return (
      /* eslint-disable max-len */
      <div key={id} title={description} className="u-sPB-1-2">
        <label className="u-textSecondary u-sPB-1-4">{label}</label>
        <input ref={id}
          type="text"
          placeholder={description}
          className="u-bgHighest u-sizeFull u-inputFlat u-sP-1-2 u-rounded u-sMH-1-4 u-sMV-1-8"
          value={value}
          onChange={this.updateSearch} />
      </div>
      /* eslint-enable max-len */
    )
  }
}

function mapStateToProps ({ phrases: { filter: { showAdvanced, advanced } } }) {
  return {
    showAdvanced,
    search: advanced
  }
}

function mapDispatchToProps (dispatch) {
  return {
    toggleAdvanced: () => dispatch(toggleAdvanced()),
    updateSearch: (newValues) => dispatch(updatePhraseFilter(newValues))
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(
  injectIntl(EditorSearchInput))
