// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import { connect } from 'react-redux'
import { cloneDeep, isEmpty } from 'lodash'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import Form from 'antd/lib/form'
import 'antd/lib/form/style/'
import Input from 'antd/lib/input'
import 'antd/lib/input/style/'
import Modal from 'antd/lib/modal'
import 'antd/lib/modal/style/css'

import {
  glossaryToggleNewEntryModal,
  glossaryCreateNewEntry
} from '../../actions/glossary-actions'
import update from 'immutability-helper'

class NewEntryModal extends Component {
  static propTypes = {
    entry: PropTypes.object,
    isSaving: PropTypes.bool,
    show: PropTypes.bool,
    handleNewEntryDisplay: PropTypes.func,
    handleNewEntryCreate: PropTypes.func
  }

  constructor (props) {
    super(props)
    this.state = {
      entry: cloneDeep(props.entry)
    }
  }

  handleContentChanged = (e) => {
    const content = e.target.value
    this.setState(prevState => ({
      entry: update(prevState.entry,
        {srcTerm:
          {content: {$set: content}}
        })
    }))
  }

  handlePosChanged = (e) => {
    const { entry } = this.state
    this.setState({
      entry: {
        ...entry,
        pos: e.target.value
      }
    })
  }

  handleDescChanged = (e) => {
    const { entry } = this.state
    this.setState({
      entry: {
        ...entry,
        description: e.target.value
      }
    })
  }

  handleCancel = () => {
    this.resetFields()
    this.props.handleNewEntryDisplay(false)
  }

  resetFields = () => {
    this.setState({
      entry: cloneDeep(this.props.entry)
    })
  }

  render () {
    const {
      show,
      isSaving,
      handleNewEntryDisplay,
      handleNewEntryCreate
      } = this.props
    const isAllowSave = !isEmpty(this.state.entry.srcTerm.content)

    /* eslint-disable react/jsx-no-bind, react/jsx-boolean-value */
    return (
      <Modal
        title={'New Term'}
        visible={show}
        onCancel={() => { this.handleCancel(); handleNewEntryDisplay(false) }}
        footer={[
          <Button
            key='back'
            aria-label='button'
            disabled={isSaving}
            onClick={() => this.handleCancel()}>
            Cancel
          </Button>,
          <Button
            key='ok'
            aria-label='button'
            type='primary'
            className='btn-primary'
            disabled={!isAllowSave || isSaving} loading={isSaving}
            onClick={
              () => {
                handleNewEntryCreate(this.state.entry); this.resetFields()
              }
            }>
            Save
          </Button>
        ]}>
        <Form layout='vertical'>
          <Form.Item label={'Term'} title={'Term'}>
            <Input
              className='textInput'
              maxLength={500}
              onChange={this.handleContentChanged.bind(this)}
              placeholder='The new term'
              value={this.state.entry.srcTerm.content} />
          </Form.Item>
          <Form.Item label={'Part of speech'} title={'Part of speech'}>
            <Input
              className='textInput'
              maxLength={255}
              onChange={this.handlePosChanged.bind(this)}
              placeholder='Noun, Verb, etc'
              value={this.state.entry.pos} />
          </Form.Item>
          <Form.Item label={'Description'} title={'Description'}>
            <Input
              className='textInput'
              maxLength={500}
              onChange={this.handleDescChanged.bind(this)}
              placeholder='The definition of this term'
              value={this.state.entry.description} />
          </Form.Item>
        </Form>
      </Modal>)
    /* eslint-enable react/jsx-no-bind, react/jsx-boolean-value */
  }
}

const mapStateToProps = (state) => {
  const { entry, isSaving, show } = state.glossary.newEntry
  return { entry, isSaving, show }
}

const mapDispatchToProps = (dispatch) => {
  return {
    dispatch,
    handleNewEntryDisplay: (display) =>
      dispatch(glossaryToggleNewEntryModal(display)),
    handleNewEntryCreate: (entry) => dispatch(glossaryCreateNewEntry(entry))
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(NewEntryModal)
