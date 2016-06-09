import React, { Component } from 'react'
/**
 * TODO: use react-ally to identify accessibility issue
 * import a11y from 'react-a11y'
 */
import { connect } from 'react-redux'
import Helmet from 'react-helmet'
import {
  Nav,
  View,
  Icons
} from '../components'

/**
 * TODO: use react-ally to identify accessibility issue in dev mode
 * if (process.env.NODE_ENV === 'development') a11y(React)
 */

class App extends Component {
  render () {
    const theme = {
      base: {
        h: 'H(100vh)',
        fld: 'Fld(c) Fld(r)--sm'
      }
    }
    const {
      children,
      activePath,
      ...props
    } = this.props

    const links = {
      'context': config.baseUrl || '',
      '/login': config.links.loginUrl,
      '/signup': config.links.registerUrl
    }

    return (
      <View {...props} theme={theme}>
        <Icons />
        <Helmet
          title='Zanata'
          titleTemplate='%s | Zanata'
        />
        <Nav active={activePath} links={links} />
        {children}
      </View>
    )
  }
}

function mapStateToProps (state) {
  return {
    activePath: state.routing.location.pathname
  }
}

export default connect(mapStateToProps)(App)
