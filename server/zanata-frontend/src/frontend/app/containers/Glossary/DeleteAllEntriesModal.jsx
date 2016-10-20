import React, { Component, PropTypes } from 'react'
import ReactDOM from 'react-dom'
import {
  ButtonLink,
  ButtonRound,
  LoaderText,
  Icon,
  Tooltip,
  Overlay,
  Row
} from 'zanata-ui'

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
            <div className='Mt(rq)'>
              <ButtonLink
                atomic={{m: 'Mend(rh)'}}
                onClick={() => handleDeleteAllEntriesDisplay(false)}>
                Cancel
              </ButtonLink>
              <ButtonRound type='danger' size='n1'
                disabled={isDeleting}
                onClick={() => handleDeleteAllEntries()}>
                <LoaderText loading={isDeleting} size='n1'
                  loadingText='Deleting'>
                  Delete
                </LoaderText>
              </ButtonRound>
            </div>
          </Tooltip>
        </Overlay>
        <ButtonLink type='danger'
          onClick={() => handleDeleteAllEntriesDisplay(true)}
          disabled={isDeleting}>
          <Row>
            <Icon name='trash' atomic={{m: 'Mend(re)'}} />
            <span className='Hidden--lesm'>Delete</span>
          </Row>
        </ButtonLink>
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
