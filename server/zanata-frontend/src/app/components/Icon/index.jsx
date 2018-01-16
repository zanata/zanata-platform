import React from 'react'
import PropTypes from 'prop-types'
import { isUndefined } from 'lodash'

/**
 * @return {string}
 */
const Icon = ({
  name,
  parentClassName,
  className,
  ...props
}) => {
  const svgIcon = `<use xlink:href="#Icon-${name}" />`
  const parentCSS = isUndefined(parentClassName) ? '' : parentClassName
  return (
    <span {...props} className={parentCSS}>
      <svg dangerouslySetInnerHTML={{ __html: svgIcon }}
        className={className}
        style={{ fill: 'currentColor' }} /></span>
  )
}

Icon.propTypes = {
  /**
   * The name of the icon.
   * See list.js in the same folder for possible icons.
   */
  name: PropTypes.string.isRequired,
  className: PropTypes.string,
  parentClassName: PropTypes.string
}

export default Icon
