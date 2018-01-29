import * as React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import * as ReactDOM from 'react-dom'
import { LoaderText, Icon } from '../../components'
import { Button, Tooltip, Overlay } from 'react-bootstrap'

/**
 * Confirmation modal dialog for delete all glossary entries
 */
class DeleteAllEntriesModal extends Component {
  static propTypes = {
    show: PropTypes.bool,
    isDeleting: PropTypes.bool,
    handleDeleteAllEntriesDisplay: PropTypes.func.isRequired,
    handleDeleteAllEntries: PropTypes.func.isRequired
  }

  render () {
    const {
      show,
      isDeleting,
      handleDeleteAllEntriesDisplay,
      handleDeleteAllEntries
    } = this.props

    /* eslint-disable react/jsx-no-bind */
    return (
      <div className='u-block'>
        <Overlay
          placement='bottom'
          target={() => ReactDOM.findDOMNode(this)}
          rootClose
          show={show}
          onHide={() => handleDeleteAllEntriesDisplay(false)}>
          <Tooltip id='delete-entries' title='Delete all glossary entries'>
            <p>
              Are you sure you want to delete&nbsp;
              <strong>all entries</strong>&nbsp;?
            </p>
            <span className='button-spacing'>
              <Button bsStyle='default' className='btn-sm'
                onClick={() => handleDeleteAllEntriesDisplay(false)}>
                Cancel
              </Button>
              <Button bsStyle='danger' className='btn-sm' type='button'
                disabled={isDeleting}
                onClick={handleDeleteAllEntries}>
                <LoaderText loading={isDeleting} size='n1'
                  loadingText='Deleting'>
                  Delete
                </LoaderText>
              </Button>
            </span>
          </Tooltip>
        </Overlay>
        <Button bsStyle='link' type='button'
          onClick={() => handleDeleteAllEntriesDisplay(true)}
          disabled={isDeleting}>
          <span>
            <Icon name='trash' className='s1' parentClassName='iconDelete' />
            <span className='hidden-lesm'>Delete</span>
          </span>
        </Button>
      </div>
    )
    /* eslint-enable react/jsx-no-bind */
  }
}

export default DeleteAllEntriesModal
