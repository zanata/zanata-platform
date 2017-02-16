import React, { Component } from 'react'
import { Sidebar } from '../../components'

/**
 * Root component for Explore page
 */
class ProjSidebar extends Component {

  render () {
    /* eslint-disable react/jsx-no-bind, no-return-assign */
    return (
      <div>
        <Sidebar />
        <div className='flextab'>
          <p>This sidebar example has the active tag applied to both the People
          and Languages pages to provide examples of how this design handles
          sidebar links.</p>
          <p>The sidebar nav has been implemented using &nbsp;
            <a href='https://react-bootstrap.github.io/components.html#navs'>
            react bootstrap components</a>.
          </p>
        </div>
      </div>
    )
    /* eslint-enable react/jsx-no-bind, no-return-assign */
  }
}

export default ProjSidebar
