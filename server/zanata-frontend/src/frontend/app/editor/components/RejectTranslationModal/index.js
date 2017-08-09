import React from 'react'
import PropTypes from 'prop-types'
import Dropdown from '../Dropdown'
import { Modal } from '../../../components'
/**
 * TODO add a concise description of this component
 */
const toggleDropdown = () => {}
const RejectTranslationModal = ({
  show,
  key,
  className,
  onHide,
  isOpen
}) => {
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
          <Dropdown
              onToggle={toggleDropdown()}
              isOpen={true}
              enabled={true}
              className="Dropdown--right u-sMV-1-2">
          <Dropdown.Button>
            <a className="Dropdown-item">
              Dropdown button
            </a>
          </Dropdown.Button>
          <Dropdown.Content>
            <ul>
              <li>Cat</li>
              <li>Dog</li>
              <li>Honey Badger</li>
              <li>Walrus</li>
            </ul>
          </Dropdown.Content>
        </Dropdown>
        </Modal.Body>
      </Modal>
  )
}

RejectTranslationModal.propTypes = {
  show: PropTypes.bool,
  className: PropTypes.string,
  key: PropTypes.string,
  onHide: PropTypes.func,
  toggleDropdown: PropTypes.func.isRequired,
  isOpen: PropTypes.bool.isRequired
}

export default RejectTranslationModal
