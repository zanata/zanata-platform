import React from 'react'
import { OverlayTrigger, Button, Tooltip, Modal }
  from 'react-bootstrap'

const ModalOverlay = React.createClass({
  getInitialState () {
    return { showModal: false }
  },

  close () {
    this.setState({ showModal: false })
  },

  open () {
    this.setState({ showModal: true })
  },

  render () {
    const tooltip = (
      <Tooltip id='modal-tooltip'>
        wow.
      </Tooltip>
    )

    return (
      <div className='container contentViewContainerTheme'>
        <h1>Demo modal</h1>
        <Button
          bsStyle='primary'
          bsSize='large'
          onClick={this.open}
        >
          Launch
        </Button>

        <Modal show={this.state.showModal} onHide={this.close}>
          <Modal.Header closeButton>
            <Modal.Title>Modal heading</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <h4>Text in a modal</h4>
            <p>Duis mollis, est non commodo luctus,
            nisi erat porttitor ligula.</p>
            <p>There is a <OverlayTrigger overlay={tooltip}>
              <a href='#'>tooltip
              </a></OverlayTrigger> here</p>
          </Modal.Body>
          <Modal.Footer>
            <Button onClick={this.close} className='center-block'>
            Close</Button>
          </Modal.Footer>
        </Modal>
      </div>
    )
  }
})

export default ModalOverlay
