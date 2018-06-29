// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import { LoaderText, Icon } from '../../components'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import Tooltip from 'antd/lib/tooltip'
import 'antd/lib/tooltip/style/css'

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
    const deleteEntry = () => {
      handleDeleteEntry(entry.id)
      handleDeleteEntryDisplay(false)
    }
    const cancelDelete = () => handleDeleteEntryDisplay(false)
    const deleteConfirm = () => handleDeleteEntryDisplay(true)
    const deleteButtons = (
      <span className='button-spacing tc'>
        <Button className='btn-sm btn-default mt3 mr3' aria-label='button'
          onClick={cancelDelete}>
          Cancel
        </Button>
        <Button className='btn-sm btn-danger' aria-label='button'
          disabled={isDeleting} type='danger'
          onClick={deleteEntry}>
          <LoaderText loading={isDeleting} size='n1'
            loadingText='Deleting'>
            Delete all
          </LoaderText>
        </Button>
      </span>
    )
    const info = entry.termsCount > 0 ? (
      <p className='tc'>
        Are you sure you want to delete this term and&nbsp;
        <strong>{entry.termsCount}</strong>&nbsp;
        {entry.termsCount > 1 ? 'translations' : 'translation'} ?
        <br />{deleteButtons}
      </p>
    ) : (<p className='tc'>
      Are you sure you want to delete this term?<br />{deleteButtons}
    </p>)
    /* eslint-disable react/jsx-no-bind */
    return (
      <div className='u-block tc'>
        <Tooltip id='delete-glossary' title={info}
          className='tc' visible={show} placement='left' arrowPointAtCenter>
          <Button className='btn-link btn-sm'
            aria-label='button' disabled={isDeleting}
            onClick={deleteConfirm}>
            <LoaderText loading={isDeleting} loadingText='Deleting'>
              <Icon name='trash' className='txt-error s1' />
              <span className='txt-error fw4 hidden-lesm'>Delete</span>
            </LoaderText>
          </Button>
        </Tooltip>
      </div>
    )
    /* eslint-enable react/jsx-no-bind */
  }
}

export default DeleteEntryModal
