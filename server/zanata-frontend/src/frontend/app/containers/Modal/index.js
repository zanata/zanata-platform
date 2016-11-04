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
      <div>
        <p>Click to get the full Modal experience!</p>

        <Button
          bsStyle='primary'
          bsSize='large'
          onClick={this.open}
        >
          Launch demo modal
        </Button>

        <Modal show={this.state.showModal} onHide={this.close}>
          <Modal.Header closeButton>
            <Modal.Title>Modal heading</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <h4>Text in a modal</h4>
            <p>Duis mollis, est non commodo luctus,
            nisi erat porttitor ligula.</p>

            <h4>Tooltips in a modal</h4>
            <p>there is a <OverlayTrigger overlay={tooltip}>
              <a href='#'>tooltip
              </a></OverlayTrigger> here</p>
          </Modal.Body>
          <Modal.Footer>
            <Button onClick={this.close}>Close</Button>
          </Modal.Footer>
        </Modal>
      </div>
    )
  }
})

export default ModalOverlay
