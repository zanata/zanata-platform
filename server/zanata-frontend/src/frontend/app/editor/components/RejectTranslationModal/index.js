import React, { Component } from 'react'
import PropTypes from 'prop-types'
import Button from '../Button'
import Dropdown from '../Dropdown'
import { Row } from 'react-bootstrap'
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
      dropdownOpen: false,
      dropdownOpen2: false
    }
  }

  toggleDropdown = () => {
    this.setState(prevState => ({
      dropdownOpen: !prevState.dropdownOpen
    }))
  }

  toggleDropdown2 = () => {
    this.setState(prevState => ({
      dropdownOpen2: !prevState.dropdownOpen2
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
            className="dropdown-menu">
            <Dropdown.Button>
            <a className="Dropdown-item">
               Other
              <span className="Dropdown-toggleIcon"></span>
              </a>
            </Dropdown.Button>
            <Dropdown.Content>
              <ul>
                <li className="Dropdown-item" onClick={this.toggleDropdown}>Translation Errors</li>
                <li className="Dropdown-item" onClick={this.toggleDropdown}>Language Quality</li>
                <li className="Dropdown-item" onClick={this.toggleDropdown}>Consistency</li>
                <li className="Dropdown-item" onClick={this.toggleDropdown}>Style Guide & Glossary Violations</li>
                <li className="Dropdown-item" onClick={this.toggleDropdown}>Format</li>
              </ul>
            </Dropdown.Content>
          </Dropdown>
          Priority:
          <Dropdown enabled isOpen={this.state.dropdownOpen2}
                    onToggle={this.toggleDropdown2}
                    className="dropdown-menu">
            <Dropdown.Button>
              <a className="Dropdown-item">
                Minor
                <span className="Dropdown-toggleIcon"></span>
              </a>
            </Dropdown.Button>
            <Dropdown.Content>
              <ul>
                <li className="Dropdown-item" onClick={this.toggleDropdown2}>Major</li>
                <li className="Dropdown-item" onClick={this.toggleDropdown2}>Critical</li>
              </ul>
            </Dropdown.Content>
          </Dropdown>
          <input ref="input"
                 type="comment"
                 placeholder="Provide a comment for why this translation has been rejected"
                 maxLength="1000"
                 className="InputGroup-input u-sizeLineHeight-1_1-4" />
        </Modal.Body>
        <Modal.Footer>
          <span>
            <Row>
              <Button className="Button Button--large">
                Cancel
              </Button>
              <Button className="Button Button--large u-rounded Button--primary">
                Reject translation
              </Button>
            </Row>
          </span>
        </Modal.Footer>
      </Modal>
    )
  }
}

export default RejectTranslationModal
