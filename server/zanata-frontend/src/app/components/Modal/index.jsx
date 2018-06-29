// @ts-nocheck
import React from 'react'
import * as PropTypes from 'prop-types'
import { Modal as OverlayModal } from 'react-overlays'
import ModalHeader from './ModalHeader'
import ModalTitle from './ModalTitle'
import ModalBody from './ModalBody'
import ModalFooter from './ModalFooter'
import { Button } from 'react-bootstrap'
import { Icon } from '../../components'
import specialKeys from 'combokeys/helpers/special-keys-map'

const Modal = ({
  children,
  closeButton,
  backdrop,
  keyboard,
  closeLabel,
  onHide,
...props
}) => {
  // Close modal on escape key pressed
  const handleKeyDown = (e) => {
    if (specialKeys[e.keyCode] === 'esc') {
      onHide()
    }
  }
  const handleClickOutside = (e) => {
    const classname = e.target.className
    const clickedBackdrop = (classname === 'modal' || classname === 'container')
    const clickedOutsideComponent = (e.target === e.currentTarget)
    if (clickedOutsideComponent || clickedBackdrop) {
      onHide()
    }
  }
  return (
    <OverlayModal
      {...props}
      onKeyDown={keyboard && handleKeyDown}
      containerClassName='has-modal'
      className='modal'
      onClick={backdrop && handleClickOutside}
    >
      <div className='container'>
        <div className='modal-content' tabIndex="0">
          {closeButton && (
            <Button aria-label={closeLabel}
              className='close s0'
              onClick={onHide}>
              <Icon name='cross' className='s2' parentClassName='iconClose' />
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
   * Whether or not to show a close button
   */
  closeButton: PropTypes.bool,
  /**
   * Whether or not to trigger onHide when clicking outside the modal
   */
  backdrop: PropTypes.bool,
  /**
   * Whether or not to trigger onHide when pressing 'esc' key
   */
  keyboard: PropTypes.bool,
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
  backdrop: false,
  keyboard: false,
  closeLabel: 'cross'
}

export default Modal
