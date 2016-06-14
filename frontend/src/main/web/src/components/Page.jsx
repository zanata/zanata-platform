import React from 'react'
import { merge } from 'lodash'
import { View } from './'

const classes = {
  base: {
    flxs: '',
    flx: 'Flx(flx1)',
    ov: 'Ov(h)'
  }
}
/**
 * Pre styled View.jsx component with overflow hidden (no scrollbar)
 */
const Page = ({
  children,
  theme,
  ...props
}) => {
  return (
    <View theme={merge({}, classes, theme)} {...props}>
      {children}
    </View>
  )
}

export default Page
