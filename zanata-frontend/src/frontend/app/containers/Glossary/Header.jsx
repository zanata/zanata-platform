import React, { PropTypes } from 'react'
import { merge } from 'lodash'
import {
  Icon, View, Heading, Link, flattenThemeClasses
} from 'zanata-ui'

const wrapperTheme = {
  base: {
    // Adjust right position for scrollbar
    end: 'End(1rem)',
    p: 'Px(rh) Px(r1)--sm',
    pos: 'Pos(f)',
    start: 'Start(0) Start(r3)--sm',
    z: 'Z(100)'
  }
}
const baseClasses = {
  base: {
    bd: 'Bdb(bd2) Bdbc(light)',
    bgc: 'Bgc(#fff)',
    p: 'Pt(rq) Pt(r1)--sm'
  }
}
const innerViewTheme = {
  base: {
    ai: 'Ai(c)',
    fld: ''
  }
}
const logoLinkTheme = {
  base: {
    bd: '',
    d: 'D(n)--sm',
    lh: 'Lh(1)',
    m: 'Mend(rh)'
  }
}
const headingTheme = {
  base: {
    fz: 'Fz(ms1) Fz(ms2)--sm'
  }
}
const headerActionsTheme = {
  base: {
    ai: 'Ai(c)',
    fld: '',
    m: 'Mstart(a)'
  }
}
/**
 * Page top header with Zanata logo
 */
const Header = ({
  children,
  theme,
  title,
  icon,
  tooltip,
  extraElements,
  ...props
}) => {
  return (
    <View theme={merge({}, wrapperTheme, theme)}>
      <div className={flattenThemeClasses(baseClasses)}>
        <View theme={innerViewTheme}>
          <Link link='/' theme={logoLinkTheme}>
            <Icon name='zanata' size='3' />
          </Link>
          <Heading level='1' theme={headingTheme} title={tooltip}>
            {icon && <Icon name={icon} />} {title || 'Title'}
          </Heading>
          <View theme={headerActionsTheme}>
            {extraElements}
          </View>
        </View>
        {children && (
          <View theme={innerViewTheme}>
            {children}
          </View>
        )}
      </div>
    </View>
  )
}

Header.propTypes = {
  children: PropTypes.node,
  theme: PropTypes.object,
  title: PropTypes.string,
  icon: PropTypes.string,
  tooltip: PropTypes.string,
  /**
   * Extra react node (html element) which will be display in the header
   */
  extraElements: PropTypes.oneOfType([
    PropTypes.arrayOf(PropTypes.node),
    PropTypes.node]
  )
}

export default Header
