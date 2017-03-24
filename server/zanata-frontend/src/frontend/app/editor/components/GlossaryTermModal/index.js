/**
 * Modal to display the details for a group of suggestion matches.
 */
import React, { Component, PropTypes } from 'react'
import { Modal, Button } from 'react-bootstrap'

class GlossaryTermModal extends Component {

  render () {
    return (
      <div className="static-modal">
        <Modal show
               onHide={action('close')}>
          <Modal.Header closeButton>
            <Modal.Title>Glossary details</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <Panel>
              Source
            </Panel>
            <Panel>
              Target
            </Panel>
          </Modal.Body>
        </Modal>
      </div>
  )
  }
}

export default GlossaryTermModal
