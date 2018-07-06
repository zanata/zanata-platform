import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import Tooltip from 'antd/lib/tooltip'
import 'antd/lib/tooltip/style/css'

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
        <p className='tc'>Are you sure you want to delete&nbsp;
          <strong>{locale.displayName}</strong>?&nbsp;
        </p>
        <p className='button-spacing tc'>
          <Button className='btn-default btn-sm mr3' aria-label='button'
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
        </p>
      </span>
    )

    return (
      <div className='di'>
        <Tooltip placement='top'
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
