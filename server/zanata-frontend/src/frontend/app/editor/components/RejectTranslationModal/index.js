import React from 'react'
import PropTypes from 'prop-types'
import { Modal } from '../../../components'
/**
 * TODO add a concise description of this component
 */
const RejectTranslationModal = ({
  show,
  key,
  className,
  onHide
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

        </Modal.Body>
      </Modal>
  )
}

RejectTranslationModal.propTypes = {
  show: PropTypes.bool,
  className: PropTypes.string,
  key: PropTypes.string,
  onHide: PropTypes.func
}

export default RejectTranslationModal
