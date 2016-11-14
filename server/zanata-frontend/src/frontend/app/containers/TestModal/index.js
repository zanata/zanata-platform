import React, { Component } from 'react'

import {
  ButtonLink,
  ButtonRound
} from 'zanata-ui'

import { Modal } from '../../components'

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
        <ButtonRound onClick={() => this.showModal()}>Launch Modal</ButtonRound>
        <Modal
          show={this.state.show}
          onHide={this.hideModal}>
          <Modal.Header>
            <Modal.Title>Example Modal</Modal.Title>
          </Modal.Header>
          <Modal.Body>Hi There</Modal.Body>
          <Modal.Footer>
            <ButtonLink onClick={() => this.hideModal()}>Cancel</ButtonLink>
            <ButtonRound onClick={() => this.hideModal()}>
              Submit
            </ButtonRound>
          </Modal.Footer>
        </Modal>
      </div>)
  }
  /* eslint-enable react/jsx-no-bind */
}

export default TestModal
