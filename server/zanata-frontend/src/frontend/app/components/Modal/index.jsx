import React, { PropTypes } from 'react'
import { Modal as OverlayModal } from 'react-overlays'
import ModalHeader from './ModalHeader'
import ModalTitle from './ModalTitle'
import ModalBody from './ModalBody'
import ModalFooter from './ModalFooter'
import { Button } from 'react-bootstrap'
import Icon from '../../components'

/*
const commonClasses = {
  b: 'B(0)',
  end: 'End(0)',
  pos: 'Pos(f)',
  start: 'Start(0)',
  t: 'T(0)'
}

const classes = {
  modal: merge({}, commonClasses, {
    ff: 'Ff(zsans)',
    z: 'Z(modal)'
  }),
  backDrop: merge({}, commonClasses, {
    bgc: 'Bgc(#000)',
    op: 'Op(.5)',
    z: 'Z(backDrop)'
  }),
  closeButton: {
    base: {
      pos: 'Pos(a)',
      e: 'End(0)',
      t: 'T(0)',
      p: 'P(rh)',
      z: 'Z(1)'
    }
  },
  closeIcon: {
    base: {
      op: 'Op(.7)'
    }
  },
  container: {
    ai: 'Ai(c)',
    d: 'D(f)',
    h: 'H(100%)',
    jc: 'Jc(c)',
    pos: 'Pos(r)',
    p: 'Py(rh)',
    z: 'Z(modal)'
  },
  inner: {
    bdrs: 'Bdrs(r3q)',
    bgc: 'Bgc(#fff.95)',
    bxsh: 'Bxsh(sh5)',
    d: 'D(f)',
    fld: 'Fld(c)',
    m: 'Mx(rh) My(r1)',
    mah: 'Mah(100%)',
    pos: 'Pos(r)',
    ta: 'Ta(c)',
    w: 'W(r32)'
  }
}

*/

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
      backdropClassName='modal-backdrop'
      className='modal'
    >
      <div className='container'>
        <div className='modal-content'>
          {closeButton ? (
            <Button aria-label={closeLabel}
              bsStyle='link'
              className='modal-close'
              onClick={onHide}>
              <Icon name='cross' className='s2 closeIcon' />
            </Button>
          ) : undefined}
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
  closeLabel: 'Close'
}

export default Modal
