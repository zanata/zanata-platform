import React, { Component } from 'react'
import { Modal } from '../../components'
import { Button } from 'react-bootstrap'

class TestModal extends Component {

  constructor () {
    super()
    this.state = {
      show: false
    }
  }

  hideModal () {
    this.setState({show: false})
  }
  showModal () {
    this.setState({show: true})
  }
  /* eslint-disable react/jsx-no-bind */
  render () {
    return (
      <div>
        <Button bsStyle='default'
          onClick={() => this.showModal()}>Launch Modal</Button>
        <Modal
          show={this.state.show}
          onHide={this.hideModal}>
          <Modal.Header>
            <Modal.Title>Example Modal</Modal.Title>
          </Modal.Header>
          <Modal.Body>Hi There</Modal.Body>
          <Modal.Footer>
            <Button bsStyle='link'
              onClick={() => this.hideModal()}>Cancel</Button>
            <Button bsStyle='primary' onClick={() => this.hideModal()}>
              Submit
            </Button>
          </Modal.Footer>
        </Modal>
      </div>)
  }
  /* eslint-enable react/jsx-no-bind */
}

export default TestModal
