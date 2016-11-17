import React, { Component } from 'react'
import { Modal } from '../../components'
import { Button } from 'react-bootstrap'

class StyleGuide extends Component {

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
      <div className='contentViewContainer'>
        <span className='list-inline'>
          <h1>Buttons</h1>
          <Button bsStyle='default'>Default</Button>
          <Button bsStyle='primary'>Primary</Button>
          <Button bsStyle='success'>Success</Button>
          <Button bsStyle='warning'>Warning</Button>
          <Button bsStyle='danger'>Danger</Button>
          <Button bsStyle='info'>Info</Button>
          <Button bsStyle='link'>Link</Button>
        </span>
        <br />
        <span>
          <h1>EditableText</h1>
        </span>
        <span>
          <h1>Modal</h1>
          <Button bsStyle='default'
            onClick={() => this.showModal()}>Launch Modal</Button>
          <Modal
            show={this.state.show}
            onHide={() => this.hideModal()}>
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
        </span>
      </div>)
  }
  /* eslint-enable react/jsx-no-bind */
}

export default StyleGuide
