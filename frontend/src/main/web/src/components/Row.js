import React, { PropTypes } from 'react'
import warning from 'warning'
import { merge } from 'lodash'
import { View } from './'

const classes = {
  base: {
    fld: 'Fld(r)!'
  },
  align: {
    start: {
      ai: 'Ai(fs)'
    },
    end: {
      ai: 'Ai(fe)'
    },
    center: {
      ai: 'Ai(c)'
    },
    baseline: {
      ai: 'Ai(b)'
    },
    stretch: {
      ai: 'Ai(st)'
    }
  }
}
/**
 * A flexbox component that align all elements (props.children) horizontally in a row.
 * Can be used as standalone or as children of an element.
 */
const Row = ({
  align = 'center',
  children,
  className,
  theme = {},
  ...props
}) => {
  warning(!className,
    'Please use `theme` instead of `className` to style Row.')
  const themed = merge({},
    classes,
    theme
  )
  const themeState = {
    base: merge({},
      themed.base,
      themed.align[align]
    )
  }
  return (
    <View
      {...props}
      theme={themeState}>
      {children}
    </View>
  )
}

Row.propType = {
  align: PropTypes.oneOf(['start', 'end', 'center', 'baseline', 'stretch']),
  children: PropTypes.node,
  className: PropTypes.string,
  theme: PropTypes.object
}

export default Row
