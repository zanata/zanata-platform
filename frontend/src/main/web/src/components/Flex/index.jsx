import React, { PropTypes } from 'react'
import { merge } from 'lodash'
import {
  Base
} from '../'

const classes = {
  base: {
    d: 'D(f)'
  },
  align: {
    fs: 'Ai(fs)',
    fe: 'Ai(fe)',
    c: 'Ai(c)',
    b: 'Ai(b)',
    st: 'Ai(st)'
  },
  alignContent: {
    fs: 'Ac(fs)',
    fe: 'Ac(fe)',
    c: 'Ac(c)',
    st: 'Ac(st)',
    sb: 'Ac(sb)',
    sa: 'Ac(sa)'
  },
  dir: {
    r: 'Fld(r)',
    rr: 'Fld(rr)',
    c: 'Fld(c)',
    cr: 'Fld(cr)'
  },
  justify: {
    fs: 'Jc(fs)',
    fe: 'Jc(fe)',
    c: 'Jc(c)',
    sb: 'Jc(sb)',
    sa: 'Jc(sa)'
  },
  wrap: {
    nw: 'Flw(nw)',
    w: 'Flw(w)',
    wr: 'Flw(wr)'
  }
}

const Flex = ({
  align,
  alignContent,
  children,
  dir,
  justify,
  wrap,
  theme = {},
  ...props
}) => {
  const themed = merge({}, classes, theme)
  const stateTheme = merge({},
    themed.base,
    {
      ai: themed.align[align],
      ac: themed.alignContent[alignContent],
      fld: themed.dir[dir],
      jc: themed.justify[justify],
      flw: themed.wrap[wrap]
    }
  )
  return (
    <Base
      componentName='Flex'
      theme={stateTheme}
      {...props}
    >
      {children}
    </Base>
  )
}

Flex.propTypes = {
  /**
   * This defines the default behaviour for how flex items are laid out along
   * the cross axis on the current line. Think of it as the justify-content
   * version for the cross-axis (perpendicular to the main-axis).
   * See http://acss.io/reference for atomic class letter code meanings.
   */
  align: PropTypes.oneOf(['fs', 'fe', 'c', 'b', 'st']),
  /**
   * This aligns a flex container's lines within when there is extra space in
   * the cross-axis, similar to how justify-content aligns individual items
   * within the main-axis.
   * See http://acss.io/reference for atomic class letter code meanings.
   * **Note:** this property has no effect when there is only one line of flex
   * items.
   */
  alignContent: PropTypes.oneOf(['fs', 'fe', 'c', 'st', 'sb', 'sa']),
  children: PropTypes.node,
  /**
   * This establishes the main-axis, thus defining the direction flex items are
   * placed in the flex container. Flexbox is (aside from optional wrapping) a
   * single-direction layout concept. Think of flex items as primarily laying
   * out either in horizontal rows or vertical columns.
   * See http://acss.io/reference for atomic class letter code meanings.
   */
  dir: PropTypes.oneOf(['r', 'rr', 'c', 'cr']),
  /**
   * This defines the alignment along the main axis. It helps distribute extra
   * free space left over when either all the flex items on a line are
   * inflexible, or are flexible but have reached their maximum size. It also
   * exerts some control over the alignment of items when they overflow
   * the line.
   * See http://acss.io/reference for atomic class letter code meanings.
   */
  justify: PropTypes.oneOf(['fs', 'fe', 'c', 'sb', 'sa']),
  /**
   * By default, flex items will all try to fit onto one line. You can change
   * that and allow the items to wrap as needed with this property. Direction
   * also plays a role here, determining the direction new lines are stacked in.
   * See http://acss.io/reference for atomic class letter code meanings.
   */
  wrap: PropTypes.oneOf(['nw', 'w', 'wr']),
  theme: PropTypes.object
}

Flex.defaultProps = {
  align: 'fs',
  alignContent: 'fs',
  dir: 'r',
  justify: 'fs',
  wrap: 'nw'
}

export default Flex
