import React, { PropTypes } from 'react'
import Icon from '../../components'

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
  ...props
}) => {
  return (
    <span
      componentName='Loader'
      {...props}>
      <Icon name='dot' className='dot dot-first' />
      <Icon name='dot' className='dot dot-second' />
      <Icon name='dot' className='dot dot-third' />
    </span>
  )
}

Loader.propTypes = {

}


export default Loader
