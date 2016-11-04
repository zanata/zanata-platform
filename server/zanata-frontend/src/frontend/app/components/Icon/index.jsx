import React, { PropTypes } from 'react'

/**
 * @return {string}
 */
const Icon = ({
  name,
  ...props
}) => {
  const svgIcon = `<use xlink:href="#Icon-${name}" />`
  return (
    <span {...props}>
      <svg dangerouslySetInnerHTML={{ __html: svgIcon }}
        style={{ fill: 'currentColor' }} /></span>
  )
}

Icon.propTypes = {
  /**
   * The name of the icon.
   * See list.js in the same folder for possible icons.
   */
  name: PropTypes.string.isRequired
}

export default Icon
