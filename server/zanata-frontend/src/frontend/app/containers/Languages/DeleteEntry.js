import React, { Component, PropTypes } from 'react'
import { Button, ButtonToolbar, OverlayTrigger, Popover } from 'react-bootstrap'

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
      handleDeleteEntryDisplay,
      handleDeleteEntry
    } = this.props
    /* eslint-disable react/jsx-no-bind */
    const popoverTop =
      (<ButtonToolbar>
        <Popover id='popover-positioned-top'>
          <p>Are you sure you want to delete&nbsp;
            <strong>{locale.displayName}</strong>?&nbsp;
          </p>
          <span className='button-spacing'>
            <Button bsStyle='default'
              onClick={() =>
                handleDeleteEntryDisplay(false)
              }>
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
      </ButtonToolbar>)

    return (
      <div className='D(ib)'>
        <OverlayTrigger rootClose trigger='click' placement='top'
          overlay={popoverTop} >
          <Button bsSize='small'
            onClick={() => handleDeleteEntryDisplay(true)}>
            <i className='fa fa-times Mend(ee)'></i>Delete
          </Button>
        </OverlayTrigger>
      </div>
    )
    /* eslint-enable react/jsx-no-bind */
  }
}

DeleteEntry.propTypes = {
  locale: React.PropTypes.object,
  handleDeleteEntryDisplay: PropTypes.func.isRequired,
  handleDeleteEntry: React.PropTypes.func.isRequired
}

export default DeleteEntry
