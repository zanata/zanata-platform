import React, { Component, PropTypes } from 'react'
import ReactDOM from 'react-dom'
import {
  LoaderText,
  Icon,
  Tooltip,
  Overlay
} from 'zanata-ui'
import { Button } from 'react-bootstrap'

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
            <span className='button-spacing'>
              <Button bsStyle='default'
                onClick={() => handleDeleteEntryDisplay(false)}>
                Cancel
              </Button>
              <Button bsStyle='danger' type='button'
                disabled={isDeleting}
                onClick={() => {
                  handleDeleteEntry(entry.id)
                  handleDeleteEntryDisplay(false)
                }}>
                <LoaderText loading={isDeleting} size='n1'
                  loadingText='Deleting'>
                  Delete all
                </LoaderText>
              </Button>
            </span>
          </Tooltip>
        </Overlay>
        <Button bsStyle='link' bsSize='small' className='delete-link'
          type='button' disabled={isDeleting}
          onClick={() => handleDeleteEntryDisplay(true)}>
          <LoaderText loading={isDeleting} loadingText='Deleting'>
            <Icon name='trash' atomic={{m: 'Mend(re)'}} />
            <span className='Hidden--lesm'>Delete</span>
          </LoaderText>
        </Button>
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
