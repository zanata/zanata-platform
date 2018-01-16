import React from 'react'
import PropTypes from 'prop-types'

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
  return (
    <span {...props} className={parentClassName ? parentClassName : ''}>
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
