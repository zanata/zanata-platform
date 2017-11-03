import React from 'react'
import PropTypes from 'prop-types'

/**
 * @return {string}
 */
const Icon = ({
  name,
  size,
  ...props
}) => {
  const svgIcon = `<use xlink:href="#Icon-${name}" />`
  return (
    <span {...props}>
      <svg dangerouslySetInnerHTML={{ __html: svgIcon }}
        className={size}
        style={{ fill: 'currentColor' }} /></span>
  )
}

Icon.propTypes = {
  /**
   * The name of the icon.
   * See list.js in the same folder for possible icons.
   */
  name: PropTypes.string.isRequired,
  size: PropTypes.oneOf(
    ['n2', 'n1', 's0', 's1', 's2', 's3', 's4', 's5', 's6', 's7', 's8', 's9',
      's10']
  )
}

export default Icon
