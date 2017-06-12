import React, { Component, PropTypes } from 'react'
import { isEmpty } from 'lodash'
import { EditableText, Icon, LoaderText, Modal } from '../../components'
import { Button, ButtonGroup } from 'react-bootstrap'

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
                <Icon name='language' className='languageicon-neutral s1' />
                <span className='text-muted'>{selectedTransLocale}</span>
              </span>)
              : (<span>
                <Icon name='translate' className='translateicon-neutral s1' />
                <span className='text-muted'>{entry.termsCount}</span>
              </span>)
            }
          </Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <div className='modal-section'>
            <label className='text-bold'>Term</label>
            <EditableText
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
              className='text-state-classes'
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
              maxLength={255}
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
                editing
                maxLength={255}
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
                maxLength={255}
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
            <div className='modal-muted'>{info}</div>
          }
        </Modal.Body>
        <Modal.Footer>
          <ButtonGroup className='pull-right'>
            <Button bsStyle='link'
              onClick={
                () => {
                  handleResetTerm(entry.id); handleEntryModalDisplay(false)
                }
              }>
              Cancel
            </Button>

            {isSaving
              ? (<Button bsStyle='primary'
                type='button' disabled>
                <LoaderText loading loadingText='Updating'>Update</LoaderText>
              </Button>)
              : (<Button bsStyle='primary'
                type='button' onClick={() => handleUpdateTerm(entry)}
                disabled={!canUpdate}>
                  Update
              </Button>)
            }
          </ButtonGroup>
        </Modal.Footer>
      </Modal>
    )
    /* eslint-enable react/jsx-no-bind, react/jsx-boolean-value*/
  }
}

export default EntryModal
