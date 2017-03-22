/**
 * Modal to display the details for a group of suggestion matches.
 */
import React, { Component, PropTypes } from 'react'
import { Modal } from 'zanata-ui'

class GlossaryTermModal extends Component {

  render () {
    return (
      <Modal
        show
        className="suggestions-modal">
        <Modal.Header>
          <Modal.Title><small><span className="pull-left">
          Translation Memory Details</span></small></Modal.Title>
        </Modal.Header>
        <Modal.Body>
          Blah
        </Modal.Body>
      </Modal>
    )
  }
}

export default GlossaryTermModal
