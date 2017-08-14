import React, { Component } from 'react'
import PropTypes from 'prop-types'
import Button from '../Button'
import Dropdown from '../Dropdown'
import { Row } from 'react-bootstrap'
import { Modal } from '../../../components'
import { Icon } from '../../../components'
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
    isOpen: PropTypes.bool.isRequired,
    priority: PropTypes.oneOf(
        [
          'Minor',
          'Major',
          'Critical'
        ]
    ).isRequired,
    textState: PropTypes.oneOf (
        [
            'u-textWarning',
            'u-textDanger'
        ]
    ),
    criteria: PropTypes.oneOf(
        [
          'Translation errors',
          'Language quality',
          'Consistency',
          'Style Guide and Glossary Violations',
          'Format',
          'Other'
        ]
    ).isRequired
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
      isOpen,
      criteria,
      priority,
      textState
    } = this.props


    return (
      <Modal show={show}
        onHide={close}
        key="reject-translation-modal"
        className="suggestions-modal">
        <Modal.Header>
          <Modal.Title>Reject translation</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <span>
            Reason:
          <Dropdown enabled isOpen={this.state.dropdownOpen}
            onToggle={this.toggleDropdown}
            className="dropdown-menu criteria">
            <Dropdown.Button>
            <a className="Dropdown-item">
              {this.props.criteria}
              <Icon className="n1" name="chevron-down" />
              </a>
            </Dropdown.Button>
            <Dropdown.Content>
              <ul>
                <li className="Dropdown-item" onClick={this.toggleDropdown}>Translation Errors</li>
                <li className="Dropdown-item" onClick={this.toggleDropdown}>Language Quality</li>
                <li className="Dropdown-item" onClick={this.toggleDropdown}>Consistency</li>
                <li className="Dropdown-item" onClick={this.toggleDropdown}>Style Guide & Glossary Violations</li>
                <li className="Dropdown-item" onClick={this.toggleDropdown}>Format</li>
                <li className="Dropdown-item" onClick={this.toggleDropdown}>Other</li>
              </ul>
            </Dropdown.Content>
          </Dropdown>
          </span>
          <span className="priority-dd">
            <Icon name="warning" className="s2 u-textWarning" /> Priority:
          <Dropdown enabled isOpen={this.state.dropdownOpen2}
                    onToggle={this.toggleDropdown2}
                    className="dropdown-menu priority">
            <Dropdown.Button>
              <a className="Dropdown-item">
                <span className={textState}>{this.props.priority}</span>
                <Icon className="n1" name="chevron-down" />
              </a>
            </Dropdown.Button>
            <Dropdown.Content>
              <ul>
                <li className="Dropdown-item" onClick={this.toggleDropdown2}>
                  <span>Minor</span></li>
                <li className="Dropdown-item" onClick={this.toggleDropdown2}>
                    <span className="u-textWarning">Major</span></li>
                <li className="Dropdown-item" onClick={this.toggleDropdown2}>
                    <span className="u-textDanger">Critical</span></li>
              </ul>
            </Dropdown.Content>
          </Dropdown>
          </span>
          <textarea ref="input"
             type="comment"
             placeholder="Provide a comment for why this translation has been rejected"
             cols="50"
             rows="10"
             className='InputGroup-input is-focused InputGroup--outlined commenting' />
        </Modal.Body>
        <Modal.Footer>
          <span>
            <Row>
              <Button className="Button Button--large u-rounded Button--secondary">
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
