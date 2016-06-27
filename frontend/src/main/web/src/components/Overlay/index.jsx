import React, { cloneElement, PropTypes } from 'react'
import BaseOverlay from 'react-overlays/lib/Overlay'
import { elementType } from 'react-prop-types'
/**
 * Custom overlay component that based of
 * http://react-bootstrap.github.io/react-overlays/. See
 * http://react-bootstrap.github.io/react-overlays/examples/ for documentation.
 */
const Overlay = ({
  children,
  transition,
  ...props
}) => {
  const child = transition
    ? children
    : cloneElement(children, {
      className: 'Op(1) ' + children.props.className
    })
  return (
    <BaseOverlay
      {...props}
      transition={transition}
    >
      {child}
    </BaseOverlay>
  )
}

Overlay.propTypes = {
  ...BaseOverlay.propTypes,
  children: PropTypes.node,
  /**
   * A react-overlays/lib/Transition component to use for
   * the dialog and backdrop components.
   * See http://react-bootstrap.github.io/react-overlays/examples/#transition
   * for documentation.
   */
  transition: PropTypes.element,
  /**
   * Set the visibility of the Overlay
   */
  show: PropTypes.bool,
  /**
   * Specify whether the overlay should trigger
   * onHide when the user clicks outside the overlay
   */
  rootClose: PropTypes.bool,
  /**
   * A Callback fired by the Overlay when it wishes to be hidden.
   */
  onHide: PropTypes.func,
  /**
   * Use animation. Boolean is enabling default animation.
   * elementType - react-overlays/lib/Overlay/Transitions to handle animation.
   *
   * See react-overlays/lib/BaseOverlay for more information
   */
  animation: PropTypes.oneOfType([
    PropTypes.bool,
    elementType
  ]),
  /**
   * Callback fired before the Overlay transitions in
   */
  onEnter: PropTypes.func,
  /**
   * Callback fired as the Overlay begins to transition in
   */
  onEntering: PropTypes.func,
  /**
   * Callback fired after the Overlay finishes transitioning in
   */
  onEntered: PropTypes.func,
  /**
   * Callback fired right before the Overlay transitions out
   */
  onExit: PropTypes.func,
  /**
   * Callback fired as the Overlay begins to transition out
   */
  onExiting: PropTypes.func,
  /**
   * Callback fired after the Overlay finishes transitioning out
   */
  onExited: PropTypes.func
}

Overlay.defaultProps = {
  transition: null,
  rootClose: false,
  show: false
}

export default Overlay
