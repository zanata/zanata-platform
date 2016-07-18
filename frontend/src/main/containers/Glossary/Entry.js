import React, { Component, PropTypes } from 'react'
import { isEqual } from 'lodash'

import {
  ButtonLink,
  ButtonRound,
  EditableText,
  Icon,
  LoaderText,
  TableCell,
  TableRow,
  Row
} from 'zanata-ui'
import EntryModal from './EntryModal'
import DeleteEntryModal from './DeleteEntryModal'
/**
 * Component to display a GlossaryEntry
 */
class Entry extends Component {
  constructor () {
    super()
    this.state = {
      showEntryModal: false,
      showDeleteModal: false
    }
  }

  setShowingEntryModal (showing) {
    this.setState({
      showEntryModal: showing
    })
  }

  setShowingDeleteEntryModal (showing) {
    this.setState({
      showDeleteModal: showing
    })
  }

  shouldComponentUpdate (nextProps, nextState) {
    return !isEqual(this.props, nextProps) || !isEqual(this.state, nextState)
  }

  render () {
    const {
      entry,
      handleSelectTerm,
      handleTermFieldUpdate,
      handleDeleteTerm,
      handleResetTerm,
      handleUpdateTerm,
      isSaving,
      isDeleting,
      permission,
      selectedTransLocale,
      selected,
      termsLoading
    } = this.props

    const transContent = entry && entry.transTerm
      ? entry.transTerm.content : ''
    const transSelected = !!selectedTransLocale

    if (!entry) {
      return (
        <TableRow>
          <TableCell>
            <div className='LineClamp(1,24px) Px(rq)'>Loading…</div>
          </TableCell>
        </TableRow>
      )
    }

    const isTermModified = transSelected
      ? (entry.status && entry.status.isTransModified)
      : (entry.status && entry.status.isSrcModified)
    const displayUpdateButton = permission.canUpdateEntry &&
      ((isTermModified && selected) || isSaving)
    const editable = permission.canUpdateEntry && !isSaving

    /* eslint-disable react/jsx-no-bind */
    const updateButton = displayUpdateButton && (
      <ButtonRound atomic={{m: 'Mend(rh)'}}
        type='primary'
        disabled={isSaving}
        onClick={() => handleUpdateTerm(entry, transSelected)}>
        <LoaderText loading={isSaving} loadingText='Updating'>
          Update
        </LoaderText>
      </ButtonRound>
    )

    const loadingDiv = (
      <div className='LineClamp(1,24px) Px(rq)'>Loading…</div>
    )

    let secondColumnContent
    if (termsLoading) {
      secondColumnContent = loadingDiv
    } else if (transSelected) {
      secondColumnContent =
        <EditableText
          title={transContent}
          editable={transSelected && editable}
          editing={selected}
          onChange={(e) => handleTermFieldUpdate('locale', e)}
          placeholder='Add a translation…'
          emptyReadOnlyText='No translation'>
          {transContent}
        </EditableText>
    } else {
      secondColumnContent =
        <div className='LineClamp(1,24px) Px(rq)'>
          {entry.termsCount}
        </div>
    }

    return (
      <TableRow highlight
        className='editable'
        selected={selected}
        onClick={() => handleSelectTerm(entry.id)}>
        <TableCell size='3' tight>
          {termsLoading
            ? loadingDiv
            : (<EditableText
              title={entry.srcTerm.content}
              editable={false}
              editing={selected}>
              {entry.srcTerm.content}
            </EditableText>)
          }
        </TableCell>
        <TableCell size={'3'} tight={transSelected}>
          {secondColumnContent}
        </TableCell>
        <TableCell hideSmall>
          {termsLoading
            ? loadingDiv
            : (<EditableText
              title={entry.pos}
              editable={!transSelected && editable}
              editing={selected}
              onChange={(e) => handleTermFieldUpdate('pos', e)}
              placeholder='Add part of speech…'
              emptyReadOnlyText='No part of speech'>
              {entry.pos}
            </EditableText>)
          }
        </TableCell>
        <TableCell size='2'>
          {termsLoading
            ? loadingDiv
            : (<Row>
              <ButtonLink atomic={{m: 'Mend(rq)'}}
                disabled={isDeleting}
                onClick={() => this.setShowingEntryModal(true)}>
                <Icon name='info' />
              </ButtonLink>
              <EntryModal entry={entry}
                show={this.state.showEntryModal}
                isSaving={isSaving}
                selectedTransLocale={selectedTransLocale}
                canUpdate={displayUpdateButton}
                handleEntryModalDisplay={(display) =>
                  this.setShowingEntryModal(display)}
                handleResetTerm={(entryId) => handleResetTerm(entryId)}
                handleTermFieldUpdate={(field, e) =>
                  handleTermFieldUpdate(field, e)}
                handleUpdateTerm={(entry) =>
                  handleUpdateTerm(entry, false)} />
                {updateButton}
              <div
                className='Op(0) row--selected_Op(1) editable:h_Op(1) Trs(eo)'>
                {displayUpdateButton && !isSaving ? (
                  <ButtonLink
                    onClick={() => handleResetTerm(entry.id)}>
                    Cancel
                  </ButtonLink>
                ) : ''}
                {!transSelected && permission.canDeleteEntry && !isSaving &&
                !displayUpdateButton && (
                  <DeleteEntryModal entry={entry}
                    isDeleting={isDeleting}
                    show={this.state.showDeleteModal}
                    handleDeleteEntryDisplay={(display) =>
                  this.setShowingDeleteEntryModal(display)}
                    handleDeleteEntry={handleDeleteTerm} />)
                }
              </div>
            </Row>)
          }
        </TableCell>
      </TableRow>
    )
    /* eslint-enable react/jsx-no-bind */
  }
}

Entry.propTypes = {
  entry: PropTypes.object,
  handleSelectTerm: PropTypes.func,
  handleTermFieldUpdate: PropTypes.func,
  handleDeleteTerm: PropTypes.func,
  handleResetTerm: PropTypes.func,
  handleUpdateTerm: PropTypes.func,
  index: PropTypes.number,
  isSaving: PropTypes.bool,
  isDeleting: PropTypes.bool,
  permission: PropTypes.object,
  selectedTransLocale: PropTypes.string,
  selected: PropTypes.bool,
  termsLoading: PropTypes.bool
}

export default Entry
