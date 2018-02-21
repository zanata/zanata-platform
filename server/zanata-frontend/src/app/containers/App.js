// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
/**
 * TODO: use react-ally to identify accessibility issue
 * import a11y from 'react-a11y'
 */
import { connect } from 'react-redux'
import Helmet from 'react-helmet'
import { Nav, Icons } from '../components'
import { serverUrl, links as configLinks } from '../config'

/**
 * TODO: use react-ally to identify accessibility issue in dev mode
 * if (process.env.NODE_ENV === 'development') a11y(React)
 */

class App extends Component {
  static propTypes = {
    children: PropTypes.node,
    activePath: PropTypes.string,
    loading: PropTypes.bool
  }

  render () {
    const {
      children,
      activePath,
      loading
    } = this.props

    const links = {
      'context': serverUrl,
      '/login': configLinks.loginUrl,
      '/signup': configLinks.registerUrl
    }
    return (
      <div className='bstrapReact'>
        <div className='view H(100vh)! Fld(c) Fld(r)--sm'>

          <Helmet
            title='Zanata'
            titleTemplate='Zanata: %s'
          />
          <Nav active={activePath} links={links} loading={loading} />
          <div className='containerSidebar'>{children}</div>
        </div>
      </div>
    )
  }
}

// FIXME checking if ownProps will work here, as react-router-redux should pass
//       location info in to routed components that way.
function mapStateToProps (state, { location }) {
  const exploreLoading = state.explore.loading
  const isExploreLoading = exploreLoading.Project ||
    exploreLoading.LanguageTeam || exploreLoading.Person || exploreLoading.Group
  return {
    activePath: location.pathname,
    loading: state.common.loading || state.profile.loading ||
      isExploreLoading || state.glossary.statsLoading
  }
}

export default connect(mapStateToProps)(App)
