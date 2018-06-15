// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import { isEmpty } from 'lodash'
import { EditableText, Icon, Modal } from '../../components'
import Button from 'antd/lib/button'

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

    const transContent = entry.transTerm ? entry.transTerm.content : ''
    const transSelected = !!selectedTransLocale
    const comment = entry.transTerm ? entry.transTerm.comment : ''

    const enableComment = transSelected &&
      entry.transTerm && !isEmpty(transContent)

    const info = transSelected
      ? this.generateTermInfo(entry.transTerm)
      : this.generateTermInfo(entry.srcTerm)

    /* eslint-disable react/jsx-no-bind, react/jsx-boolean-value */
    return (
      <Modal show={show}
        onHide={() => handleEntryModalDisplay(false)}>
        <Modal.Header>
          <Modal.Title>
            Glossary Term
            {transSelected
              ? (<span>
                <Icon name='language' className='s1'
                  parentClassName='iconLanguage-neutral' />
                <span className='u-textMuted'>{selectedTransLocale}</span>
              </span>)
              : (<span>
                <Icon name='translate' className='s1'
                  parentClassName='iconTranslate-neutral' />
                <span className='u-textMuted'>{entry.termsCount}</span>
              </span>)
            }
          </Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <div className='modal-section'>
            <label className='text-bold'>Term</label>
            <EditableText
              className='textInput'
              editable={false}
              editing={false}>
              {entry.srcTerm.content}
            </EditableText>
          </div>
          <div className='modal-section'>
            <label className='text-bold'>Part of speech</label>
            <EditableText
              editable={!transSelected}
              editing
              className='textState textInput'
              maxLength={255}
              placeholder='Add part of speech…'
              emptyReadOnlyText='No part of speech'
              onChange={(e) => handleTermFieldUpdate('pos', e)}>
              {entry.pos}
            </EditableText>
          </div>
          <div className='modal-section'>
            <label className='text-bold'>Description</label>
            <EditableText
              editable={!transSelected}
              editing
              className='textInput'
              maxLength={500}
              placeholder='Add a description…'
              emptyReadOnlyText='No description'
              onChange={(e) => handleTermFieldUpdate('description', e)}>
              {entry.description}
            </EditableText>
          </div>
          {transSelected ? (
            <div className='modal-section'>
              <label className='text-bold'>Translation</label>
              <EditableText
                editable
                className='textInput'
                editing
                maxLength={500}
                placeholder='Add a translation…'
                emptyReadOnlyText='No translation'
                onChange={(e) => handleTermFieldUpdate('locale', e)}>
                {transContent}
              </EditableText>
            </div>
            ) : ''}

          {transSelected ? (
            <div className='modal-section'>
              <label className='text-bold'>Comment</label><br />
              <EditableText
                maxLength={500}
                className='textInput'
                editable={enableComment}
                editing={enableComment}
                placeholder='Add a comment…'
                emptyReadOnlyText='No comment'
                multiline
                onChange={(e) => handleTermFieldUpdate('comment', e)}>
                {comment}
              </EditableText>
            </div>
          ) : ''}
          {!isEmpty(info) &&
            <div className='modalText-muted'>{info}</div>
          }
        </Modal.Body>
        <Modal.Footer>
          <div className='u-pullRight'>
            <Button className='btn-link' aria-label='button'
              onClick={
                () => {
                  handleResetTerm(entry.id); handleEntryModalDisplay(false)
                }
              }>
              Cancel
            </Button>
            <Button className='btn-primary' aria-label='button'
              type='primary' onClick={() => handleUpdateTerm(entry)}
              disabled={!canUpdate || isSaving} loading={isSaving}>
              Update
            </Button>
          </div>
        </Modal.Footer>
      </Modal>
    )
    /* eslint-enable react/jsx-no-bind, react/jsx-boolean-value*/
  }
}

export default EntryModal
