import React, { PropTypes } from 'react'
import { merge } from 'lodash'
import { Base } from '../'

const classes = {
  base: {
    d: 'D(ib)',
    flxs: 'Flxs(0)',
    pos: 'Pos(r)'
  },
  sizes: {
    'n2': {
      w: 'W(msn2)',
      h: 'H(msn2)'
    },
    'n1': {
      w: 'W(msn1)',
      h: 'H(msn1)'
    },
    '0': {
      w: 'W(ms0)',
      h: 'H(ms0)'
    },
    '1': {
      w: 'W(ms1)',
      h: 'H(ms1)'
    },
    '2': {
      w: 'W(ms2)',
      h: 'H(ms2)'
    },
    '3': {
      w: 'W(ms3)',
      h: 'H(ms3)'
    },
    '4': {
      w: 'W(ms4)',
      h: 'H(ms4)'
    },
    '5': {
      w: 'W(ms5)',
      h: 'H(ms5)'
    },
    '6': {
      w: 'W(ms6)',
      h: 'H(ms6)'
    },
    '7': {
      w: 'W(ms7)',
      h: 'H(ms7)'
    },
    '8': {
      w: 'W(ms8)',
      h: 'H(ms8)'
    },
    '9': {
      w: 'W(ms9)',
      h: 'H(ms9)'
    },
    '10': {
      w: 'W(ms10)',
      h: 'H(ms10)'
    }
  }
}

const Icon = ({
  name,
  size = '0',
  theme,
  ...props
}) => {
  const svgIcon = `<use xlink:href="#Icon-${name}" />`
  const themed = merge({},
    classes,
    theme
  )
  const themedState = merge({},
    themed.base,
    themed.sizes[size]
  )
  return (
    <Base
      tagName='span'
      componentName='Icon'
      theme={themedState}
      {...props}>
      <svg dangerouslySetInnerHTML={{ __html: svgIcon }}
        className='Pos(a) H(100%) W(100%) T(0) Start(0)'
        style={{ fill: 'currentColor' }} />
    </Base>
  )
}

Icon.propTypes = {
  /**
   * The name of the icon.
   * See list.js in the same folder for possible icons.
   */
  name: PropTypes.string.isRequired,
  /**
   * The size of the icon based on the modular scale.
   */
  size: PropTypes.oneOf(
    ['n2', 'n1', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '10']
  )
}

export default Icon
