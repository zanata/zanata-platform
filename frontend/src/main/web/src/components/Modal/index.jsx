import React, { PropTypes } from 'react'
import { Modal as OverlayModal } from 'react-overlays'
import { merge } from 'lodash'
import ModalHeader from './ModalHeader'
import ModalTitle from './ModalTitle'
import ModalBody from './ModalBody'
import ModalFooter from './ModalFooter'
import {
  Base,
  ButtonLink,
  Icon
} from '../'
import { flattenThemeClasses } from '../../utils/styleUtils'

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
      backdropClassName={flattenThemeClasses(classes.backDrop)}
      className={flattenThemeClasses(classes.modal)}
    >
      <Base theme={classes.container}>
        <Base theme={classes.inner}>
          {closeButton ? (
            <ButtonLink aria-label={closeLabel}
              theme={classes.closeButton}
              type='muted'
              onClick={onHide}>
              <Icon name='cross' size='2' theme={classes.closeIcon}/>
            </ButtonLink>
          ) : undefined}
          {children}
        </Base>
      </Base>
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
