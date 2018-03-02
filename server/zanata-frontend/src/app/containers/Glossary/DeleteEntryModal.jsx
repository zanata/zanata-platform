// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import * as ReactDOM from 'react-dom'
import { LoaderText, Icon } from '../../components'
import { Button, Tooltip, Overlay } from 'react-bootstrap'

class DeleteEntryModal extends Component {
  static propTypes = {
    entry: PropTypes.object,
    show: PropTypes.bool,
    isDeleting: PropTypes.bool,
    handleDeleteEntryDisplay: PropTypes.func.isRequired,
    handleDeleteEntry: PropTypes.func.isRequired
  }

  handleDeleteEntry = (_entryId) => {
    this.props.handleDeleteEntry(this.props.entry.id)
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
      <div className='u-block bstrapReact'>
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
            <Icon name='trash' className='s1' parentClassName='iconDelete' />
            <span className='hidden-lesm'>Delete</span>
          </LoaderText>
        </Button>
      </div>
    )
    /* eslint-enable react/jsx-no-bind */
  }
}

export default DeleteEntryModal
