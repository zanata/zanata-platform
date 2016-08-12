import React, { Component, PropTypes } from 'react'
import { isEmpty } from 'lodash'
import {
  ButtonLink,
  ButtonRound,
  EditableText,
  Icon,
  LoaderText,
  Modal,
  Row
} from 'zanata-ui'

/**
 * Popup windows to display a glossary entry
 */
class EntryModal extends Component {
  generateTermInfo (term) {
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
                <Icon name='language'
                  atomic={{m: 'Mstart(rq) Mend(re)', c: 'C(neutral)'}} />
                <span className='C(muted)'>{selectedTransLocale}</span>
              </span>)
              : (<span>
                <Icon name='translate'
                  atomic={{m: 'Mstart(rq) Mend(re)', c: 'C(neutral)'}} />
                <span className='C(muted)'>{entry.termsCount}</span>
              </span>)
            }
          </Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <div className='Mb(rh)'>
            <label className='Fw(b)'>Term</label>
            <EditableText
              editable={false}
              editing={false}>
              {entry.srcTerm.content}
            </EditableText>
          </div>
          <div className='Mb(rh)'>
            <label className='Fw(b)'>Part of speech</label>
            <EditableText
              editable={!transSelected}
              editing={true}
              maxLength={255}
              placeholder='Add part of speech…'
              emptyReadOnlyText='No part of speech'
              onChange={(e) => handleTermFieldUpdate('pos', e)}>
              {entry.pos}
            </EditableText>
          </div>
          <div className='Mb(rh)'>
            <label className='Fw(b)'>Description</label>
            <EditableText
              editable={!transSelected}
              editing={true}
              maxLength={255}
              placeholder='Add a description…'
              emptyReadOnlyText='No description'
              onChange={(e) => handleTermFieldUpdate('description', e)}>
              {entry.description}
            </EditableText>
          </div>
          {transSelected ? (
            <div className='Mb(rh)'>
              <label className='Fw(b)'>Translation</label>
              <EditableText
                editable={true}
                editing={true}
                maxLength={255}
                placeholder='Add a translation…'
                emptyReadOnlyText='No translation'
                onChange={(e) => handleTermFieldUpdate('locale', e)}>
                {transContent}
              </EditableText>
            </div>
            ) : ''}

          {transSelected ? (
            <div className='Mb(rh)'>
              <label className='Fw(b)'>Comment</label>
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
            <div className='C(muted) Pt(rq)'>{info}</div>
          }
        </Modal.Body>
        <Modal.Footer>
          <Row theme={{ base: {j: 'Jc(c)'} }}>
            <ButtonLink atomic={{m: 'Mstart(rh)'}}
              onClick={
                () => {
                  handleResetTerm(entry.id); handleEntryModalDisplay(false)
                }
              }>
              Cancel
            </ButtonLink>

            {isSaving
              ? (<ButtonRound atomic={{m: 'Mstart(rh)'}}
                type='primary' disabled={true}>
                <LoaderText loading loadingText='Updating'>Update</LoaderText>
              </ButtonRound>)
              : (<ButtonRound atomic={{m: 'Mstart(rh)'}} type='primary'
                onClick={() => handleUpdateTerm(entry)} disabled={!canUpdate}>
                  Update
              </ButtonRound>)
            }
          </Row>
        </Modal.Footer>
      </Modal>
    )
    /* eslint-enable react/jsx-no-bind, react/jsx-boolean-value*/
  }
}

EntryModal.propTypes = {
  entry: PropTypes.object,
  show: PropTypes.bool,
  isSaving: PropTypes.bool,
  canUpdate: PropTypes.bool,
  selectedTransLocale: PropTypes.object,
  handleResetTerm: PropTypes.func,
  handleEntryModalDisplay: PropTypes.func,
  handleUpdateTerm: PropTypes.func,
  handleTermFieldUpdate: PropTypes.func
}

export default EntryModal
