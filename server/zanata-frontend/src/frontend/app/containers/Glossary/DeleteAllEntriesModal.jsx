import React, { Component, PropTypes } from 'react'
import ReactDOM from 'react-dom'
import {
  LoaderText,
  Icon,
  Tooltip,
  Overlay,
  Row
} from 'zanata-ui'
import { Button } from 'react-bootstrap'

/**
 * Confirmation modal dialog for delete all glossary entries
 */
class DeleteAllEntriesModal extends Component {
  render () {
    const {
      show,
      isDeleting,
      handleDeleteAllEntriesDisplay,
      handleDeleteAllEntries
      } = this.props

    /* eslint-disable react/jsx-no-bind */
    return (
      <div className='D(ib)'>
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
            <div className='Mt(rq) pull-right'>
              <Button bsStyle='default'
                atomic={{m: 'Mend(rh)'}}
                onClick={() => handleDeleteAllEntriesDisplay(false)}>
                Cancel
              </Button>
              <Button bsStyle='danger' type='danger'
                disabled={isDeleting}
                onClick={() => handleDeleteAllEntries()}>
                <LoaderText loading={isDeleting} size='n1'
                  loadingText='Deleting'>
                  Delete
                </LoaderText>
              </Button>
            </div>
          </Tooltip>
        </Overlay>
        <Button bsStyle='link danger' type='danger'
          onClick={() => handleDeleteAllEntriesDisplay(true)}
          disabled={isDeleting}>
          <Row>
            <Icon name='trash' atomic={{m: 'Mend(re)'}} />
            <span className='Hidden--lesm'>Delete</span>
          </Row>
        </Button>
      </div>
    )
    /* eslint-enable react/jsx-no-bind */
  }
}

DeleteAllEntriesModal.propTypes = {
  show: React.PropTypes.bool,
  isDeleting: React.PropTypes.bool,
  handleDeleteAllEntriesDisplay: PropTypes.func.isRequired,
  handleDeleteAllEntries: React.PropTypes.func.isRequired
}

export default DeleteAllEntriesModal
