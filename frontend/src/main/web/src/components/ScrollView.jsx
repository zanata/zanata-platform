import React from 'react'
import { merge } from 'lodash'
import { View } from './'

/**
 * Scrollbar enabled flexbox
 */
const ScrollView = ({
  children,
  theme,
  ...props
}) => {
  const classes = {
    base: {
      flxs: '',
      flxg: 'Flxg(1)',
      ov: 'Ov(a)',
      ovh: 'Ovx(h)',
      ovs: 'Ovs(touch)'
    }
  }
  return (
    <View {...props} theme={merge({}, classes, theme)}>
      <View theme={{
        base: {
          flxg: 'Flxg(1)',
          p: 'Px(rh) Px(r1)--sm',
          pos: 'Pos(r)',
          w: 'W(100%)'
        }
      }}>
        {children}
      </View>
    </View>
  )
}

export default ScrollView
