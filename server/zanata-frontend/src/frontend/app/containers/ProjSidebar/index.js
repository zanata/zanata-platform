import React, { Component } from 'react'
import Helmet from 'react-helmet'
import { Sidebar } from '../../components'

/**
 * Root component for Explore page
 */
class ProjSidebar extends Component {

  render () {
    /* eslint-disable react/jsx-no-bind, no-return-assign */
    return (
      <div className='page scroll-view-theme' id='sidebartest'>
        <Helmet title='ProjSidebar' />
        <Sidebar />
        <div className='flextab'>
          <p>Blah</p>
        </div>
      </div>
    )
    /* eslint-enable react/jsx-no-bind, no-return-assign */
  }
}

export default ProjSidebar
