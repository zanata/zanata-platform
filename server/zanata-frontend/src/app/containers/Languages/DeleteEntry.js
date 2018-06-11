import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import Button from 'antd/lib/button'
import Tooltip from 'antd/lib/tooltip'

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
          <Button className='btn-default btn-sm' aria-label='button'
            onClick={() => handleDeleteEntryDisplay(false)}>
            Cancel
          </Button>
          <Button className='btn-danger btn-sm' type='danger'
            aria-label='button'
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
        <Tooltip placement='top'
          // @ts-ignore
          className='bstrapReact'
          title={deleteLanguage} onVisibleChange={show}>
          <Button className='btn-sm iconCross btn-default' icon='close'
            aria-label='button'
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
