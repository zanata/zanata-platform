import { isUndefined } from 'lodash'
import React from 'react'
import * as t from 'io-ts'
import { sfc } from '../../utils/prop-types-util'

const Props = t.intersection([
  t.type({
      /**
       * The name of the icon.
       * See list.js in the same folder for possible icons.
       */
      name: t.string,
    },
  ), t.partial({
      className: t.string,
      parentClassName: t.string,
      title: t.string,
      // TODO use this to hold attributes for the span element instead of extending HTMLAttributes:
      // spanAttrs: React.HTMLAttributes<HTMLSpanElement>
    },
  )
])

type IconProps = t.TypeOf<typeof Props> & React.HTMLAttributes<HTMLSpanElement>

const Icon = sfc<typeof Props, IconProps>(Props, ({
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
})

export default Icon
