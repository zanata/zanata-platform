import { isUndefined } from 'lodash'
import * as PropTypes from 'prop-types'
import React from 'react'

interface IconProps extends React.HTMLAttributes<HTMLSpanElement> {
  className?: string,
  name: string,
  parentClassName?: string,
  title?: string,
  // TODO use this to hold attributes for the span element instead of extending HTMLAttributes:
  // spanAttrs: React.HTMLAttributes<HTMLSpanElement>
}

const Icon: React.StatelessComponent<IconProps> = ({
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
  className: PropTypes.string,
  /**
   * The name of the icon.
   * See list.js in the same folder for possible icons.
   */
  name: PropTypes.string.isRequired,
  parentClassName: PropTypes.string,
}

export default Icon
