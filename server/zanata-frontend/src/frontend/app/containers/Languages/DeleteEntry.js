import React, { Component, PropTypes } from 'react'
import ReactDOM from 'react-dom'
import { Button } from 'react-bootstrap'
import {
  Tooltip,
  Overlay
} from 'zanata-ui'

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
        <Overlay
          placement='top'
          target={() => ReactDOM.findDOMNode(this)}
          rootClose
          show={show}
          onHide={() => handleDeleteEntryDisplay(false)}>
          <Tooltip id='delete-glossary' title='Delete language'>
            <p>
            Are you sure you want to delete&nbsp;
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
          </Tooltip>
        </Overlay>
        <Button bsSize='small'
          onClick={() => handleDeleteEntryDisplay(true)}>
          <i className='fa fa-times Mend(ee)'></i>Delete
        </Button>
      </div>
    )
    /* eslint-enable react/jsx-no-bind */
  }
}

DeleteEntry.propTypes = {
  locale: React.PropTypes.object,
  show: React.PropTypes.bool,
  handleDeleteEntryDisplay: PropTypes.func.isRequired,
  handleDeleteEntry: React.PropTypes.func.isRequired
}

export default DeleteEntry
