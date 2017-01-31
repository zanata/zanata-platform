import React, { Component, PropTypes } from 'react'
import ReactDOM from 'react-dom'
import { LoaderText, Icon } from '../../components'
import { Button, Tooltip, Overlay } from 'react-bootstrap'

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
      <div className='block'>
        <Overlay
          placement='top'
          target={() => ReactDOM.findDOMNode(this)}
          rootClose
          show={show}
          onHide={() => handleDeleteEntryDisplay(false)}>
          <Tooltip id='delete-glossary' title='Delete term and translations'>
            {info}
            <span className='button-spacing'>
              <Button bsStyle='default' className='btn-sm'
                onClick={() => handleDeleteEntryDisplay(false)}>
                Cancel
              </Button>
              <Button bsStyle='danger' type='button' className='btn-sm'
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
            <Icon name='trash' className='deleteicon s1' />
            <span className='hidden-lesm'>Delete</span>
          </LoaderText>
        </Button>
      </div>
    )
    /* eslint-enable react/jsx-no-bind */
  }
}

DeleteEntryModal.propTypes = {
  entry: React.PropTypes.object,
  show: React.PropTypes.bool,
  isDeleting: React.PropTypes.bool,
  handleDeleteEntryDisplay: PropTypes.func.isRequired,
  handleDeleteEntry: React.PropTypes.func.isRequired
}

export default DeleteEntryModal
