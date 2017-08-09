import React, { Component } from 'react'
import PropTypes from 'prop-types'
import Dropdown from '../Dropdown'
import { Modal } from '../../../components'
/**
 * TODO add a concise description of this component
 */
export class RejectTranslationModal extends Component {
  static propTypes = {
    show: PropTypes.bool,
    className: PropTypes.string,
    key: PropTypes.string,
    onHide: PropTypes.func,
    toggleDropdown: PropTypes.func.isRequired,
    isOpen: PropTypes.bool.isRequired
  }

  constructor (props) {
    super(props)
    this.state = {
      dropdownOpen: false
    }
  }

  toggleDropdown = () => {
    this.setState(prevState => ({
      dropdownOpen: !prevState.dropdownOpen
    }))
  }

  render () {
    const {
      show,
      key,
      className,
      onHide,
      isOpen
    } = this.props
    return (
      <Modal show={show}
        onHide={close}
        key="reject-translation-modal"
        className="suggestions-modal">
        <Modal.Header>
          <Modal.Title><small><span className="pull-left">
          Reject translation</span></small></Modal.Title>
        </Modal.Header>
        <Modal.Body>
          Reason:
          <Dropdown enabled isOpen={this.state.dropdownOpen}
            onToggle={this.toggleDropdown}
            className="Dropdown--right u-sMV-1-2">
            <Dropdown.Button>
              <a className="Dropdown-item">
                Dropdown button
              </a>
            </Dropdown.Button>
            <Dropdown.Content>
              <ul>
                <li onClick={this.toggleDropdown}>Cat</li>
                <li onClick={this.toggleDropdown}>Dog</li>
                <li onClick={this.toggleDropdown}>Honey Badger</li>
                <li onClick={this.toggleDropdown}>Walrus</li>
              </ul>
            </Dropdown.Content>
          </Dropdown>
        </Modal.Body>
      </Modal>
    )
  }
}

export default RejectTranslationModal
