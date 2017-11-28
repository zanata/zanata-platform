import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { isEqual } from 'lodash'
import EntryModal from './EntryModal'
import DeleteEntryModal from './DeleteEntryModal'
import { EditableText, Icon, LoaderText } from '../../components'
import { Button, Row, Table } from 'react-bootstrap'
/**
 * Component to display a GlossaryEntry
 */
class Entry extends Component {
  static propTypes = {
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
        <tr>
          <td>
            <div className='lineClamp'>Loading…</div>
          </td>
        </tr>
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
      <Button bsSize='small' bsStyle='primary'
        disabled={isSaving}
        onClick={() => handleUpdateTerm(entry, transSelected)}>
        <LoaderText loading={isSaving} loadingText='Updating'>
          Update
        </LoaderText>
      </Button>
    )

    const loadingDiv = (
      <div className='lineClamp'>Loading…</div>
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
        <div className='lineClamp'>
          {entry.termsCount}
        </div>
    }

    const cssClass = 'highlight editable' + (selected ? ' selected' : '')

    return (
      <Table className='glossaryEntry'>
        <tbody>
          <tr className={cssClass}
            selected={selected}
            onClick={() => handleSelectTerm(entry.id)}>
            <td className='td-3 tight'>
              {termsLoading
                ? loadingDiv
                : (<EditableText
                  title={entry.srcTerm.content}
                  editable={false}
                  editing={selected}>
                  {entry.srcTerm.content}
                </EditableText>)
              }
            </td>
            <td className='td-3 tight'>
              {secondColumnContent}
            </td>
            <td className='hidesmall td-3'>
            {termsLoading
              ? loadingDiv
              : (<EditableText
                className='textState'
                title={entry.pos}
                editable={!transSelected && editable}
                editing={selected}
                onChange={(e) => handleTermFieldUpdate('pos', e)}
                placeholder='Add part of speech'
                emptyReadOnlyText='No part of speech'>
                {entry.pos}
              </EditableText>)
            }
            </td>
            <td className='td-2'>
              {termsLoading
                ? loadingDiv
                : (<Row className='entry-row'>
                  <Button bsStyle="link"
                    className="btn-link-end"
                    disabled={isDeleting}
                    onClick={() => this.setShowingEntryModal(true)}>
                    <Icon name='info' className='s1 iconInfo-noMargin' />
                  </Button>
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
                  <div className='u-row--transparent row--selected
                    editable-op1'>
                    <div className='hidden-lesm'>
                      <Row className='entry-row'>
                        {updateButton}
                        {displayUpdateButton && !isSaving ? (
                          <Button bsStyle='link' bsSize='small'
                            onClick={() => handleResetTerm(entry.id)}>
                            Cancel
                          </Button>
                        ) : ''}
                      </Row>
                    </div>
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
            </td>
          </tr>
        </tbody>
      </Table>
    )
    /* eslint-enable react/jsx-no-bind */
  }
}

export default Entry
