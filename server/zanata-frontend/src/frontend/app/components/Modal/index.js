import React, { Component, PropTypes } from 'react'
import { Modal, Button }
  from 'react-bootstrap'

class ModalOverlay extends Component {

  getInitialState: ModalOverlay() {
    return { showModal: false };
  }

  close () {
    this.setState({ showModal: false })
  }
  open () {
    this.setState({ showModal: true })
  }
  render () {
    const {
      title,
      contents
    } = this.props

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
            <Modal.Title>{title}</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            {contents}
          </Modal.Body>
          <Modal.Footer>
            <Button onClick={this.close}>Close</Button>
          </Modal.Footer>
        </Modal>
      </div>
    )
  }
}

ModalOverlay.propTypes = {
  title: PropTypes.string,
  contents: PropTypes.string
}

export default ModalOverlay
