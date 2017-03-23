/**
 * Modal to display the details for a group of suggestion matches.
 */
import React, { Component, PropTypes } from 'react'
import { Modal, Button } from 'react-bootstrap'

class GlossaryTermModal extends Component {

  render () {
    return (
      <div className="static-modal">
        <Modal.Dialog>
          <Modal.Header>
            <Modal.Title>Modal title</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            One fine body...
          </Modal.Body>
          <Modal.Footer>
            <Button>Close</Button>
            <Button bsStyle="primary">Save changes</Button>
          </Modal.Footer>
        </Modal.Dialog>
      </div>
    )
  }
}

export default GlossaryTermModal
