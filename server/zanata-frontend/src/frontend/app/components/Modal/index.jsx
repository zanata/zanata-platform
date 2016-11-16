import React, { PropTypes } from 'react'
import { Modal as OverlayModal } from 'react-overlays'
import ModalHeader from './ModalHeader'
import ModalTitle from './ModalTitle'
import ModalBody from './ModalBody'
import ModalFooter from './ModalFooter'
import { Button } from 'react-bootstrap'
import { Icon } from '../../components'

const Modal = ({
  children,
  closeButton,
  closeLabel,
  onHide,
...props
}) => {
  return (
    <OverlayModal
      {...props}
      containerClassName='has-modal'
      className='modal'
    >
      <div className='container'>
        <div className='modal-content'>
          {closeButton && (
            <Button aria-label={closeLabel}
              className='close s0'
              onClick={onHide}>
              <Icon name='cross' className='s2 closeIcon' />
            </Button>
          )}
          {children}
        </div>
      </div>
    </OverlayModal>
  )
}

Modal.Header = ModalHeader
Modal.Title = ModalTitle
Modal.Body = ModalBody
Modal.Footer = ModalFooter

Modal.propTypes = {
  children: PropTypes.node,
  /**
   * Wether or not to show a close button
   */
  closeButton: PropTypes.bool,
  /**
   * What aria-label to use on the close button
   */
  closeLabel: PropTypes.string,
  /**
   * The function to call when clicking the close button
   */
  onHide: PropTypes.func
}
Modal.defaultProps = {
  closeButton: true,
  closeLabel: 'cross'
}

export default Modal
