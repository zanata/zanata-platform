import React, { Component, PropTypes } from 'react'
import {
  Button,
  Popover,
  Overlay
} from 'react-bootstrap'
import ReactDOM from 'react-dom'
import { Icon } from '../../components'

class DeleteEntry extends Component {

  handleDeleteEntry (localeId) {
    this.props.handleDeleteEntry(localeId)
    setTimeout(() => {
      this.props.handleDeleteEntryDisplay(false)
    }, 200)
  }

  render () {
    const {
      locale,
      show,
      handleDeleteEntryDisplay,
      handleDeleteEntry
    } = this.props
    /* eslint-disable react/jsx-no-bind */
    return (
      <div className='D(ib)'>
        <Button bsSize='small' className='langdel-btn'
          onClick={() => handleDeleteEntryDisplay(true)}>
          <Icon name='cross' className='n1 crossicon' title='cross' />
          Delete
        </Button>
        <Overlay show={show} placement='top'
          target={() => ReactDOM.findDOMNode(this)}>
          <Popover id='popover-contained' title='Delete language'>
            <p>Are you sure you want to delete&nbsp;
              <strong>{locale.displayName}</strong>?&nbsp;
            </p>
            <span className='button-spacing'>
              <Button bsStyle='default'
                onClick={() => handleDeleteEntryDisplay(false)}>
                Cancel
              </Button>
              <Button bsStyle='danger' type='button'
                onClick={() => {
                  handleDeleteEntry(locale.localeId)
                  handleDeleteEntryDisplay(false)
                }}>
                Delete
              </Button>
            </span>
          </Popover>
        </Overlay>
      </div>
    )
    /* eslint-enable react/jsx-no-bind */
  }
}

DeleteEntry.propTypes = {
  locale: PropTypes.object,
  show: PropTypes.bool,
  handleDeleteEntryDisplay: PropTypes.func.isRequired,
  handleDeleteEntry: React.PropTypes.func.isRequired
}

export default DeleteEntry
