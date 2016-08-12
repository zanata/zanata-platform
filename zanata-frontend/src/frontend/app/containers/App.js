import React, { PropTypes, Component } from 'react'
/**
 * TODO: use react-ally to identify accessibility issue
 * import a11y from 'react-a11y'
 */
import { connect } from 'react-redux'
import Helmet from 'react-helmet'
import {
  View,
  Icons
} from 'zanata-ui'
import { Nav } from '../components'

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
      loading,
      ...props
    } = this.props

    const links = {
      'context': window.config.baseUrl || '',
      '/login': window.config.links.loginUrl,
      '/signup': window.config.links.registerUrl
    }

    return (
      <View {...props} theme={theme}>
        <Icons />
        <Helmet
          title='Zanata'
          titleTemplate='%s | Zanata'
        />
        <Nav active={activePath} links={links} loading={loading} />
        {children}
      </View>
    )
  }
}

App.propTypes = {
  children: PropTypes.node,
  activePath: PropTypes.string,
  loading: PropTypes.bool
}

function mapStateToProps (state) {
  const exploreLoading = state.explore.loading
  const isExploreLoading = exploreLoading.Project ||
    exploreLoading.LanguageTeam || exploreLoading.Person || exploreLoading.Group
  return {
    activePath: state.routing.location.pathname,
    loading: state.common.loading || state.profile.loading ||
      isExploreLoading || state.glossary.statsLoading
  }
}

export default connect(mapStateToProps)(App)
