// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import { LoaderText } from '../../components'
import { Button, Tooltip } from 'antd'

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
    const deleteAllEntries = () => handleDeleteAllEntriesDisplay(false)
    const deleteAll = (
      <span>
        <p>
        Are you sure you want to delete&nbsp;
          <strong>all entries</strong>&nbsp;?
        </p>
        <span className='button-spacing'>
          <Button className='btn-default btn-sm'
            onClick={deleteAllEntries}>
            Cancel
          </Button>
          <Button className='btn-sm btn-danger' type='button'
            disabled={isDeleting}
            onClick={handleDeleteAllEntries}>
            <LoaderText loading={isDeleting} size='n1'
              loadingText='Deleting'>
              Delete
            </LoaderText>
          </Button>
        </span>
      </span>
    )
    /* eslint-disable react/jsx-no-bind */
    return (
      <div className='u-block'>
        <Tooltip
          placement='bottom'
          visible={show}
          title={deleteAll}>
          <Button className='btn-link icon-delete' type='button'
            onClick={() => handleDeleteAllEntriesDisplay(true)}
            disabled={isDeleting} icon='delete'>
            <span className='hidden-lesm'>Delete</span>
          </Button>
        </Tooltip>
      </div>
    )
    /* eslint-enable react/jsx-no-bind */
  }
}

export default DeleteAllEntriesModal
