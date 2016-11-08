import React, { PropTypes } from 'react'

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
    ['n2', 'n1', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '10']
  )
}

export default Icon
