import React, { Component, PropTypes } from 'react'
import ReactDOM from 'react-dom'
import {
  ButtonLink,
  ButtonRound,
  LoaderText,
  Icon,
  Tooltip,
  Overlay
} from 'zanata-ui'

class DeleteEntryModal extends Component {

  handleDeleteEntry (entryId) {
    this.props.handleDeleteEntry(entryId)
    setTimeout(() => {
      this.props.handleDeleteEntryDisplay(false)
    }, 200)
  }

  render () {
    const {
      entry,
      className,
      show,
      isDeleting,
      handleDeleteEntryDisplay,
      handleDeleteEntry
      } = this.props
    const info = entry.termsCount > 0 ? (
      <p>
        Are you sure you want to delete this term and&nbsp;
        <strong>{entry.termsCount}</strong>&nbsp;
        {entry.termsCount > 1 ? 'translations' : 'translation'} ?
      </p>
    ) : (<p>Are you sure you want to delete this term?</p>)
    /* eslint-disable react/jsx-no-bind */
    return (
      <div className={className + ' D(ib)'}>
        <Overlay
          placement='top'
          target={() => ReactDOM.findDOMNode(this)}
          rootClose
          show={show}
          onHide={() => handleDeleteEntryDisplay(false)}>
          <Tooltip id='delete-glossary' title='Delete term and translations'>
            {info}
            <div className='Mt(rq)'>
              <ButtonLink
                atomic={{m: 'Mend(rh)'}}
                onClick={() => handleDeleteEntryDisplay(false)}>
                Cancel
              </ButtonLink>
              <ButtonRound type='danger' size='n1'
                disabled={isDeleting}
                onClick={() => {
                  handleDeleteEntry(entry.id)
                  handleDeleteEntryDisplay(false)
                }}>
                <LoaderText loading={isDeleting} size='n1'
                  loadingText='Deleting'>
                  Delete all
                </LoaderText>
              </ButtonRound>
            </div>
          </Tooltip>
        </Overlay>
        <ButtonLink type='danger'
          onClick={() => handleDeleteEntryDisplay(true)}
          disabled={isDeleting}>
          <LoaderText loading={isDeleting} loadingText='Deleting'>
            <Icon name='trash' atomic={{m: 'Mend(re)'}} />
            <span className='Hidden--lesm'>Delete</span>
          </LoaderText>
        </ButtonLink>
      </div>
    )
    /* eslint-enable react/jsx-no-bind */
  }
}

DeleteEntryModal.propTypes = {
  className: React.PropTypes.string,
  entry: React.PropTypes.object,
  show: React.PropTypes.bool,
  isDeleting: React.PropTypes.bool,
  handleDeleteEntryDisplay: PropTypes.func.isRequired,
  handleDeleteEntry: React.PropTypes.func.isRequired
}

export default DeleteEntryModal
