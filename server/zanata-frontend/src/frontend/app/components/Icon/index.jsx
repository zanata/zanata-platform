import React, { PropTypes } from 'react'

/**
 * @return {string}
 */
const Icon = ({
  name,
  classes,
  ...props
}) => {
  const svgIcon = `<use xlink:href="#Icon-${name}" />`
  return (
    <span {...props}>
      <svg dangerouslySetInnerHTML={{ __html: svgIcon }}
        className={classes}
        style={{ fill: 'currentColor' }} /></span>
  )
}

Icon.propTypes = {
  /**
   * The name of the icon.
   * See list.js in the same folder for possible icons.
   */
  name: PropTypes.string.isRequired,

  classes: PropTypes.string.isRequired
  /**
   * The size of the icon based on the modular scale.
   */
}

export default Icon
