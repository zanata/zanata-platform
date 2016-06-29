import React, { cloneElement, Component, PropTypes } from 'react'
import ReactDOM from 'react-dom'
import contains from 'dom-helpers/query/contains'
import warning from 'warning'
import { pick, hasIn } from 'lodash'
import callWithSameArgs from '../../utils/callWithSameArgs'
import Overlay from '../Overlay'

class OverlayTrigger extends Component {
  constructor () {
    super()
    this.state = {
      isOverlayShown: this.props.defaultOverlayShown
    }
  }

  show () {
    this.setState({
      isOverlayShown: true
    })
  }

  hide () {
    this.setState({
      isOverlayShown: false
    })
  }

  toggle () {
    if (this.state.isOverlayShown) {
      this.hide()
    } else {
      this.show()
    }
  }

  /**
   * This is to preserve React context in "overlay" components
   * without resetting up all context.
   * See https://github.com/react-component/dialog/issues/10
   */
  renderOverlay () {
    ReactDOM.unstable_renderSubtreeIntoContainer(
      this, this._overlay, this._mountNode
    )
  }

  getOverlayTarget () {
    return ReactDOM.findDOMNode(this)
  }

  getOverlay () {
    const overlayProps = {
      ...pick(this.props, Object.keys(Overlay.propTypes)),
      show: this.state.isOverlayShown,
      placement: this.props.overlay.props.placement || this.props.placement,
      onHide: this.hide,
      target: this.getOverlayTarget,
      onExit: this.props.onExit,
      onExiting: this.props.onExiting,
      onExited: this.props.onExited,
      onEnter: this.props.onEnter,
      onEntering: this.props.onEntering,
      onEntered: this.props.onEntered
    }

    const overlay = cloneElement(this.props.overlay, {
      placement: overlayProps.placement,
      container: overlayProps.container
    })

    return (
      <Overlay {...overlayProps}>
        {overlay}
      </Overlay>
    )
  }
  handleDelayedShow () {
    if (this.hoverHideTimeoutHandle !== undefined) {
      clearTimeout(this.hoverHideTimeoutHandle)
      this.hoverHideTimeoutHandle = undefined
      return
    }

    if (this.state.isOverlayShown || this._hoverShowDelay !== undefined) {
      return
    }

    const delay = this.props.delayShow === null
      ? this.props.delay : this.props.delayShow

    if (!delay) {
      this.show()
      return
    }

    this._hoverShowDelay = setTimeout(() => {
      this._hoverShowDelay = undefined
      this.show()
    }, delay)
  }

  handleDelayedHide () {
    if (this._hoverShowDelay !== undefined) {
      clearTimeout(this._hoverShowDelay)
      this._hoverShowDelay = undefined
      return
    }

    if (!this.state.isOverlayShown || this.hoverHideTimeoutHandle !== undefined) {
      return
    }

    const delay = this.props.delayHide === undefined
      ? this.props.delay : this.props.delayHide

    if (!delay) {
      this.hide()
      return
    }

    this.hoverHideTimeoutHandle = setTimeout(() => {
      this.hoverHideTimeoutHandle = undefined
      this.hide()
    }, delay)
  }
  // Simple implementation of mouseEnter and mouseLeave.
  // React's built version is broken:
  // https://github.com/facebook/react/issues/4251
  // for cases when the trigger is disabled and mouseOut/Over can
  // cause flicker moving from one child element to another.
  handleMouseOverOut (handler, e) {
    let target = e.currentTarget
    let related = e.relatedTarget || e.nativeEvent.toElement

    if (!related || related !== target && !contains(target, related)) {
      handler(e)
    }
  }

  componentWillMount () {
    this.handleMouseOver =
      this.handleMouseOverOut.bind(null, this.handleDelayedShow)
    this.handleMouseOut =
      this.handleMouseOverOut.bind(null, this.handleDelayedHide)
  }

  componentDidMount () {
    this._mountNode = document.createElement('div')
    this.renderOverlay()
  }

   componentWillUnmount () {
    ReactDOM.unmountComponentAtNode(this._mountNode)
    this._mountNode = null
    clearTimeout(this._hoverShowDelay)
    clearTimeout(this.hoverHideTimeoutHandle)
  }

  componentDidUpdate () {
    if (this._mountNode) {
      this.renderOverlay()
    }
  }

  render () {
    const trigger = React.Children.only(this.props.children)
    const triggerProps = trigger.props

    let props = {
      'aria-describedby': this.props.overlay.props.id
    }

    // create in render otherwise owner is lost...
    this._overlay = this.getOverlay()

    props.onClick = callWithSameArgs(
      triggerProps.onClick,
      this.props.onClick
    )

    if (hasIn(this.props.triggers, 'click')) {
      props.onClick = callWithSameArgs(this.toggle, props.onClick)
    }

    if (hasIn(this.props.triggers, 'hover')) {
      warning(!(this.props.triggers === 'hover'),
        `[zanata] Specifying only the "hover" trigger limits the
        visibilty of the overlay to just mouse users. Consider also including
        the "focus" trigger so that touch and keyboard only users can see
        the overlay as well.`)

      props.onMouseOver = callWithSameArgs(
        this.handleMouseOver,
        this.props.onMouseOver,
        triggerProps.onMouseOver
      )
      props.onMouseOut = callWithSameArgs(
        this.handleMouseOut,
        this.props.onMouseOut,
        triggerProps.onMouseOut
      )
    }

    if (hasIn(this.props.triggers, 'focus')) {
      props.onFocus = callWithSameArgs(
        this.handleDelayedShow,
        this.props.onFocus,
        triggerProps.onFocus
      )
      props.onBlur = callWithSameArgs(
        this.handleDelayedHide,
        this.props.onBlur,
        triggerProps.onBlur
      )
    }

    return cloneElement(
      trigger,
      props
    )
  }

}

OverlayTrigger.propTypes = {
  ...Overlay.propTypes,
   /**
   * Specify which action or actions trigger Overlay visibility
   */
  triggers: PropTypes.arrayOf(PropTypes.oneOf(['click', 'hover', 'focus'])),
  /**
   * A millisecond delay amount to show and hide the Overlay once triggered
   */
  delay: PropTypes.number,
  /**
   * A millisecond delay amount before showing the Overlay once triggered.
   */
  delayShow: PropTypes.number,
  /**
   * A millisecond delay amount before hiding the Overlay once triggered.
   */
  delayHide: PropTypes.number,

  /**
   * The initial visibility state of the Overlay,
   * for more nuanced visibility control consider
   * using the Overlay component directly.
   */
  defaultOverlayShown: PropTypes.bool,

  /**
   * An element or text to overlay next to the target.
   */
  overlay: PropTypes.node.isRequired,

  /**
   * @private
   */
  onBlur: PropTypes.func,
  /**
   * @private
   */
  onClick: PropTypes.func,
  /**
   * @private
   */
  onFocus: PropTypes.func,
  /**
   * @private
   */
  onMouseEnter: PropTypes.func,
  /**
   * @private
   */
  onMouseLeave: PropTypes.func,
  // override specific overlay props
  /**
   * @private
   */
  target () {},
   /**
   * @private
   */
  onHide () {},
  /**
   * @private
   */
  show () {}
}

OverlayTrigger.defaultProps = {
  defaultOverlayShown: false,
  triggers: ['hover', 'focus'],
  delay: 300
}

export default OverlayTrigger
