import React, { PropTypes } from 'react'
import { merge } from 'lodash'
import {
  Base,
  Icon
} from '../'

const classes = {
  base: {
    ai: 'Ai(c)',
    d: 'D(if)',
    jc: 'Jc(sb)'
  },
  'n1': {
    w: 'W(ms1)',
    h: 'H(ms1)'
  },
  '0': {
    w: 'W(ms2)',
    h: 'H(ms2)'
  },
  '1': {
    w: 'W(ms3)',
    h: 'H(ms3)'
  },
  '2': {
    w: 'W(ms4)',
    h: 'H(ms4)'
  },
  '3': {
    w: 'W(ms5)',
    h: 'H(ms5)'
  },
  '4': {
    w: 'W(ms6)',
    h: 'H(ms6)'
  },
  '5': {
    w: 'W(ms7)',
    h: 'H(ms7)'
  },
  '6': {
    w: 'W(ms8)',
    h: 'H(ms8)'
  },
  '7': {
    w: 'W(ms9)',
    h: 'H(ms9)'
  },
  '8': {
    w: 'W(ms10)',
    h: 'H(ms10)'
  },
  '9': {
    w: 'W(ms11)',
    h: 'H(ms11)'
  },
  '10': {
    w: 'W(ms12)',
    h: 'H(ms12)'
  }
}

const dotClasses = {
  base: {
    animdur: 'Animdur(0.9s)',
    animic: 'Animic(i)',
    animtf: 'Animtf(eob)',
    animn: 'Animn(anibd)',
    h: 'H(27.8%)',
    w: 'W(27.8%)'
  },
  second: {
    animdel: 'Animdel(0.15s)'
  },
  third: {
    animdel: 'Animdel(0.3s)'
  }
}

const Loader = ({
  size,
  theme,
  ...props
}) => {
  const themed = merge({}, classes, theme)
  const themedState = merge({},
    themed.base,
    themed[size]
  )
  return (
    <Base
      componentName='Loader'
      theme={themedState}
      {...props}>
      <Icon name='dot' atomic={dotClasses.base} />
      <Icon name='dot'
        atomic={merge({}, dotClasses.base, dotClasses.second)}
      />
      <Icon name='dot'
        atomic={merge({}, dotClasses.base, dotClasses.third)}
      />
    </Base>
  )
}

Loader.propTypes = {
  /**
   * The size of each loader, based on modular scale. Default is '0'
   */
  size: PropTypes.oneOf(
    ['n1', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '10']
  ),
  theme: PropTypes.object
}

Loader.defaultProps = {
  size: '0'
}

export default Loader
