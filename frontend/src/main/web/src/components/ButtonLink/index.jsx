import React, { PropTypes } from 'react'
import { merge } from 'lodash'
import { Base } from '../'
import { classes as buttonClasses } from '../Button'

const classes = {
  base: {
    c: 'C(pri)',
    trs: 'Trs(aeo)',
    hover: {
      filter: 'Brightness(.75):h'
    },
    focus: {
      filter: 'Brightness(.75):f'
    },
    active: {
      filter: 'Brightness(.5):a'
    }
  },
  states: {
    active: {
      filter: 'Brightness(.5):a'
    }
  },
  types: {
    default: {
      base: {
        c: 'C(pri)'
      }
    },
    plain: {
      base: {
        c: ''
      }
    },
    primary: {
      base: {
        c: 'C(pri)'
      }
    },
    success: {
      base: {
        c: 'C(success)'
      }
    },
    unsure: {
      base: {
        c: 'C(unsure)'
      }
    },
    warning: {
      base: {
        c: 'C(warning)'
      }
    },
    danger: {
      base: {
        c: 'C(danger)'
      }
    },
    muted: {
      base: {
        c: 'C(muted)'
      }
    }
  }
}

const ButtonLink = ({
  children,
  theme = {},
  type,
  ...props
}) => {
  const themed = merge({}, buttonClasses, classes, theme)
  const themedState = {
    base: merge({}, themed.base, themed.types[type].base),
    states: merge({}, themed.states, themed.types[type].states)
  }
  return (
    <Base
      {...props}
      tagName='button'
      componentName='ButtonLink'
      theme={themedState}
    >
      {children}
    </Base>
  )
}

ButtonLink.propTypes = {
  children: PropTypes.node,
  /**
   * Toggle whether the button is disabled or not. Default is 'false'
   */
  disabled: PropTypes.bool,
  /**
   * Used to override the default theme.
   */
  theme: PropTypes.object,
  /**
   * The style of the link based on it's context or state.
   */
  type: PropTypes.oneOf(['default', 'plain', 'primary', 'success', 'unsure',
    'warning', 'danger', 'muted']).isRequired
}

ButtonLink.defaultProps = {
  type: 'default'
}

export default ButtonLink
