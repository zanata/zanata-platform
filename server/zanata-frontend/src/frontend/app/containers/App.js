import React, { PropTypes, Component } from 'react'
/**
 * TODO: use react-ally to identify accessibility issue
 * import a11y from 'react-a11y'
 */
import { connect } from 'react-redux'
import Helmet from 'react-helmet'
import { Nav, Icons } from '../components'
import { getContextPath } from '../utils/UrlHelper'

/**
 * TODO: use react-ally to identify accessibility issue in dev mode
 * if (process.env.NODE_ENV === 'development') a11y(React)
 */

class App extends Component {
  render () {
    const {
      children,
      activePath,
      loading
    } = this.props

    const links = {
      'context': getContextPath(),
      '/login': window.config.links.loginUrl,
      '/signup': window.config.links.registerUrl
    }
    return (
      <div className='view H(100vh)! Fld(c) Fld(r)--sm'>
        <Icons />
        <Helmet
          title='Zanata'
          titleTemplate='Zanata: %s'
        />
        <Nav active={activePath} links={links} loading={loading} />
        <div className='container-sidebar'>{children}</div>
      </div>
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
