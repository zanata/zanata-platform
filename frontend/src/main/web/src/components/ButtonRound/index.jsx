import React, { PropTypes } from 'react'
import { merge } from 'lodash'
import { Base } from '../'
import { classes as buttonClasses } from '../Button'

const classes = {
  base: {
    bdrs: 'Bdrs(r2)',
    bxsh: 'Bxsh(sh1)',
    c: 'C(#fff)',
    fw: 'Fw(600)',
    lh: 'Lh(e1h)',
    p: 'Px(e3q)',
    trs: 'Trs(aeo)',
    hover: {
      bxsh: 'Bxsh(sh2):h',
      op: 'Op(.85):h'
    },
    focus: {
      bxsh: 'Bxsh(sh2):f',
      op: 'Op(.85):f'
    },
    active: {
      bxsh: 'Bxsh(sh1):a',
      op: 'Op(1):a'
    },
    disabled: {
      bxsh: 'Bxsh(n):di'
    }
  },
  default: {
    c: 'C(pri)',
    bxsh: 'Bxsh(ishbd2)',
    hover: {
      bgc: 'Bgc(pri):h',
      c: 'C(#fff):h'
    },
    focus: {
      bgc: 'Bgc(pri):f',
      c: 'C(#fff):f'
    },
    active: {
      bgc: 'Bgc(pri):a',
      c: 'C(#fff):a'
    },
    disabled: {
      bxsh: 'Bxsh(ishbd2):di'
    }
  },
  primary: {
    bgc: 'Bgc(pri)'
  },
  success: {
    bgc: 'Bgc(success)'
  },
  unsure: {
    bgc: 'Bgc(unsure)',
    c: 'C(#000.6)'
  },
  warning: {
    bgc: 'Bgc(warning)'
  },
  danger: {
    bgc: 'Bgc(danger)'
  },
  muted: {
    bgc: 'Bgc(muted)'
  },
  'n1': {
    fz: 'Fz(msn1)'
  },
  '0': {
    fz: 'Fz(ms0)'
  },
  '1': {
    fz: 'Fz(ms1)'
  },
  '2': {
    fz: 'Fz(ms2)'
  }
}

const ButtonRound = ({
  children,
  theme = {},
  type,
  size,
  ...props
}) => {
  const themed = merge({}, buttonClasses, classes, theme)
  const stateTheme = {
    base: merge({},
      themed.base,
      themed[type],
      themed[size]
    )
  }
  return (
    <Base
      {...props}
      componentName='ButtonRound'
      tagName='button'
      theme={stateTheme}
    >
      {children}
    </Base>
  )
}

ButtonRound.propTypes = {
  children: PropTypes.node,
  /**
   * Toggle whether the button is disabled or not. Default is 'false'
   */
  disabled: PropTypes.bool,
  theme: PropTypes.object,
  /**
   * The style of the button based on it's context or state.
   */
  type: PropTypes.oneOf(['default', 'primary', 'success', 'unsure',
    'warning', 'danger', 'muted']),
  /**
   * The size of the button. Default is '0'
   * Uses ems to style padding etc relatively to font-size.
   * n1 - font size: 0.833rem
   * 0 - font size: 1rem
   * 1 - font size: 1.2rem
   * 2 - font size: 1.44rem
   */
  size: PropTypes.oneOf(['n1', '0', '1', '2'])
}

ButtonRound.defaultProps = {
  type: 'default',
  size: '0'
}

export default ButtonRound
