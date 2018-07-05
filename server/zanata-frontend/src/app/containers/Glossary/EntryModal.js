// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import { isEmpty } from 'lodash'
import { Icon } from '../../components'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import Form from 'antd/lib/form'
import 'antd/lib/form/style/'
import Input from 'antd/lib/input'
import 'antd/lib/input/style/'
import Modal from 'antd/lib/modal'
import 'antd/lib/modal/style/index.less'

/**
 * Popup windows to display a glossary entry
 */
class EntryModal extends Component {
  static propTypes = {
    entry: PropTypes.object,
    show: PropTypes.bool,
    isSaving: PropTypes.bool,
    canUpdate: PropTypes.bool,
    selectedTransLocale: PropTypes.string,
    handleResetTerm: PropTypes.func,
    handleEntryModalDisplay: PropTypes.func,
    handleUpdateTerm: PropTypes.func,
    handleTermFieldUpdate: PropTypes.func
  }

  generateTermInfo = (term) => {
    if (term) {
      const person = term.lastModifiedBy
      const date = term.lastModifiedDate

      const isPersonEmpty = isEmpty(person)
      const isDateEmpty = isEmpty(date)

      if (!isPersonEmpty || !isDateEmpty) {
        let parts = ['Last updated']
        if (!isPersonEmpty) {
          parts.push('by:')
          parts.push(person)
        }
        if (!isDateEmpty) {
          parts.push(date)
        }
        return parts.join(' ')
      }
    }
    return undefined
  }

  render () {
    const {
      entry,
      canUpdate,
      selectedTransLocale,
      show,
      isSaving,
      handleEntryModalDisplay,
      handleResetTerm,
      handleUpdateTerm,
      handleTermFieldUpdate
    } = this.props
    /* eslint-disable react/jsx-no-bind, react/jsx-boolean-value */

    const transContent = entry.transTerm ? entry.transTerm.content : ''
    const transSelected = !!selectedTransLocale
    const comment = entry.transTerm ? entry.transTerm.comment : ''

    const enableComment = transSelected &&
      entry.transTerm && !isEmpty(transContent)

    const info = transSelected
      ? this.generateTermInfo(entry.transTerm)
      : this.generateTermInfo(entry.srcTerm)

    const onSubmit = () => {
      handleUpdateTerm(entry)
    }
    const onCancel = () => {
      handleResetTerm(entry.id)
      handleEntryModalDisplay(false)
    }

    return (
      <Modal
        title={
          <span>
            Glossary Term {transSelected
              ? (<span>
                <Icon name='language' className='s1'
                  parentClassName='iconLanguage-neutral' />
                <span className='txt-muted'>{selectedTransLocale}</span>
              </span>)
              : (<span>
                <Icon name='translate' className='s1'
                  parentClassName='iconTranslate-neutral' />
                <span className='u-textMuted'>{entry.termsCount}</span>
              </span>)}
          </span>
        }
        visible={show}
        onCancel={() => handleEntryModalDisplay(false)}
        footer={
          <React.Fragment>
            <Button className='btn-link' aria-label='button'
              onClick={onCancel}>
              Cancel
            </Button>
            <Button className='btn-primary' aria-label='button'
              type='primary' onClick={onSubmit}
              loading={isSaving}
              disabled={!canUpdate}>
              Update
            </Button>
          </React.Fragment>
        }>
        <Form layout='vertical'>
          <Form.Item label={'Term'} title={'Term'}>
            <Input
              disabled
              value={entry.srcTerm.content} />
          </Form.Item>
          <Form.Item label={'Part of speech'} title={'Part of speech'}>
            <Input
              disabled={transSelected}
              maxLength={255}
              onChange={(e) => handleTermFieldUpdate('pos', e)}
              value={entry.pos} />
          </Form.Item>
          <Form.Item label={'Description'} title={'Description'}>
            <Input
              disabled={transSelected}
              maxLength={500}
              onChange={(e) => handleTermFieldUpdate('description', e)}
              value={entry.description} />
          </Form.Item>
          {transSelected ? (
            <Form.Item label={'Translation'} title={'Translation'}>
              <Input
                maxLength={500}
                onChange={(e) => handleTermFieldUpdate('locale', e)}
                placeholder='Add a translation…'
                value={transContent} />
            </Form.Item>
          ) : ''}
          {transSelected ? (
            <Form.Item label={'Comment'} title={'Comment'}>
              <Input.TextArea
                disabled={!enableComment}
                maxLength={500}
                onChange={(e) => handleTermFieldUpdate('comment', e)}
                placeholder='Add a comment…'
                value={comment} />
            </Form.Item>
          ) : ''}
        </Form>
        {!isEmpty(info) &&
          <div className='modalText-muted'>{info}</div>
        }
      </Modal>
    )
    /* eslint-enable react/jsx-no-bind, react/jsx-boolean-value*/
  }
}

export default EntryModal
