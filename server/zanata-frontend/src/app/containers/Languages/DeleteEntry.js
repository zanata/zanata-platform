import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import {
  Button,
  Tooltip
} from 'antd'

class DeleteEntry extends Component {
  static propTypes = {
    locale: PropTypes.object,
    show: PropTypes.bool,
    handleDeleteEntryDisplay: PropTypes.func.isRequired,
    handleDeleteEntry: PropTypes.func.isRequired
  }

  render () {
    const {
      locale,
      show,
      handleDeleteEntryDisplay,
      handleDeleteEntry
    } = this.props
    /* eslint-disable react/jsx-no-bind */
    const deleteLanguage = (
      <span>
        <p>Are you sure you want to delete&nbsp;
          <strong>{locale.displayName}</strong>?&nbsp;
        </p>
        <span className='button-spacing'>
          <Button className='btn-default btn-sm'
            onClick={() => handleDeleteEntryDisplay(false)}>
            Cancel
          </Button>
          <Button className='btn-danger btn-sm'
            onClick={() => {
              handleDeleteEntry(locale.localeId)
              handleDeleteEntryDisplay(false)
            }}>
            Delete
          </Button>
        </span>
      </span>
    )

    return (
      <div className='u-blockInline'>
        <Tooltip placement='top' className='bstrapReact'
          title={deleteLanguage} onVisibleChange={show}>
          <Button className='btn-sm iconCross btn-default' icon='close'
            onClick={() => handleDeleteEntryDisplay(true)}>
            Delete
          </Button>
        </Tooltip>
      </div>
    )
    /* eslint-enable react/jsx-no-bind */
  }
}

export default DeleteEntry
