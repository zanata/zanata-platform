// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import { LoaderText } from '../../components'
import Button from 'antd/lib/button'
import Tooltip from 'antd/lib/tooltip'

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
        <p className='tc'>
        Are you sure you want to delete&nbsp;
          <strong>all entries</strong>&nbsp;?
          <br />
          <Button className='tc btn-default btn-sm mr2' aria-label='button'
            onClick={deleteAllEntries}>
            Cancel
          </Button>
          <Button type='danger' className='tc btn-sm btn-danger'
            aria-label='button' disabled={isDeleting}
            onClick={handleDeleteAllEntries}>
            <LoaderText loading={isDeleting} size='n1'
              loadingText='Deleting'>
              Delete
            </LoaderText>
          </Button>
        </p>
      </span>
    )
    /* eslint-disable react/jsx-no-bind */
    return (
      <div className='u-block'>
        <Tooltip
          placement='left'
          visible={show}
          title={deleteAll}
          className='tc'
          arrowPointAtCenter>
          <Button className='btn-link icon-delete' aria-label='button'
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
