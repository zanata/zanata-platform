import React, { PropTypes } from 'react'
import cx from 'classnames'
import { isRequiredForA11y } from 'react-prop-types'
import { capitalize } from 'lodash'
import { colors } from '../../constants/styles'

const Tooltip = ({
  placement,
  title,
  inverse,
  positionLeft,
  positionTop,
  arrowOffsetLeft,
  arrowOffsetTop,
  alignment,
  className,
  style,
  children,
  ...props
}) => {
  const arrowSize = 6
  const alignmentClass = 'Ta(' + alignment.charAt(0) + ')'
  let placementInverse = () => {
    switch (placement) {
      case 'top': return 'bottom'
      case 'bottom': return 'top'
      case 'left': return 'right'
      case 'right': return 'left'
      default: return
    }
  }
  let extraTooltipStyle = () => {
    switch (placement) {
      case 'top': return { marginTop: -arrowSize }
      case 'bottom': return { marginTop: arrowSize }
      case 'left': return { marginLeft: -arrowSize }
      case 'right': return { marginLeft: arrowSize }
    }
  }
  const tooltipClasses = cx(
    className,
    'Ff(zsans) Pos(a) Z(tooltip) Fz(msn1)',
    placement,
    alignmentClass
  )
  const tooltipStyle = {
    left: positionLeft,
    top: positionTop,
    maxWidth: '16rem',
    ...extraTooltipStyle(),
    ...style
  }
  const tooltipInnerClasses = cx(
    'Bdrs(rq) Bd(bd1) P(rq) Bxsh(sh3)',
    {
      'C(dark) Bgc(#fff) Bdc(light)': !inverse,
      'C(#fff) Bgc(#000.8) Bdc(t)': inverse
    }
  )
  const tooltipArrowClasses = cx(
    'Pos(a) D(b) W(0) H(0) Bds(s) Bdc(t) Z(tooltipArrow)'
  )
  const tooltipArrowStyle = {
    [placementInverse()]: -arrowSize,
    borderWidth: arrowSize,
    [(placement === 'left' || placement === 'right') ? 'marginTop' : 'marginLeft']: -(arrowSize + 1), // eslint-disable-line max-len
    ['border' + capitalize(placementInverse()) + 'Width']: 0,
    ['border' + capitalize(placement) + 'Color']: inverse
      ? 'transparent' : colors.light
  }
  const tooltipArrowInnerStyle = {
    borderWidth: arrowSize,
    [placementInverse()]: inverse ? 0 : 1,
    [(placement === 'left' || placement === 'right') ? 'bottom' : 'marginLeft']: -arrowSize, // eslint-disable-line max-len
    ['border' + capitalize(placementInverse()) + 'Width']: 0,
    ['border' + capitalize(placement) + 'Color']: inverse
      ? 'rgba(0,0,0,.8)' : '#fff'
  }
  const tooltipTitleClasses = 'Tt(u) Fz(msn2) Mb(rq) Op(.7)'
  const tooltipTitle = title
    ? (<h2 className={tooltipTitleClasses}>{title}</h2>) : undefined

  return (
    <div
      role='tooltip'
      {...props}
      className={tooltipClasses}
      style={tooltipStyle}
    >
      <div
        className={tooltipArrowClasses}
        style={{
          left: arrowOffsetLeft,
          top: arrowOffsetTop,
          ...tooltipArrowStyle
        }}
      >
        <div className={tooltipArrowClasses} style={tooltipArrowInnerStyle} />
      </div>

      <div className={tooltipInnerClasses}>
        {tooltipTitle}
        {children}
      </div>
    </div>
  )
}

Tooltip.propTypes = {
  /**
   * An html id attribute, necessary for accessibility
   * @type {string}
   * @required for A11y
   */
  id: isRequiredForA11y(
    PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.number
    ])
  ),
  /**
   * The direction the tooltip is positioned towards
   */
  placement: PropTypes.oneOf(['top', 'right', 'bottom', 'left']),
  /**
   * The `left` position value for the tooltip
   * TODO: pixels or em?
   */
  positionLeft: PropTypes.number,
  /**
   * The `top` position value for the tooltip
   * TODO: pixels or em?
   */
  positionTop: PropTypes.number,
  /**
   * The `left` position value for the tooltip arrow
   */
  arrowOffsetLeft: PropTypes.oneOfType([
    PropTypes.number,
    PropTypes.string
  ]),
  /**
   * The css attribute `top` position value for the tooltip arrow
   */
  arrowOffsetTop: PropTypes.oneOfType([
    PropTypes.number,
    PropTypes.string
  ]),
  /**
   * How to align the tooltip contents
   */
  alignment: PropTypes.oneOf(['center', 'left', 'right']),
  /**
   * The title of the tooltip
   */
  title: PropTypes.string,
  /**
   * Should this use the dark version of this component
   */
  inverse: PropTypes.bool,
  className: PropTypes.string,
  style: PropTypes.object,
  children: PropTypes.node
}

Tooltip.defaultProps = {
  placement: 'top',
  alignment: 'center'
}

export default Tooltip
