import React, { Component, PropTypes } from 'react'
import ReactSidebar from 'react-sidebar'
import SidebarContent from '../SidebarContent'

const defaultState = {
  docked: false,
  open: false,
  pullRight: true,
  shadow: true
}

/**
 * Sidebar component that tracks its own state for open, close, etc. and
 * displays content from SidebarContent.
 */
class Sidebar extends Component {
  constructor () {
    super()
    // have to bind this for es6 classes until property initializers are
    // available in ES7
    this.setSidebarOpen = ::this.setSidebarOpen
    this.state = defaultState
  }

  componentWillMount () {
    const mql = window.matchMedia('(min-width: 800px)')
    mql.addListener(this.mediaQueryChanged.bind(this))
    this.setState({mql: mql, docked: mql.matches})
  }

  componentWillUnmount () {
    this.state.mql.removeListener(this.mediaQueryChanged)
  }

  setSidebarOpen (open) {
    this.setState({open: open})
  }

  mediaQueryChanged () {
    this.setState({docked: this.state.mql.matches})
  }

  render () {
    const content = <SidebarContent />

    return (
      <ReactSidebar
        sidebar={content}
        docked={this.state.docked}
        open={this.state.open}
        pullRight={this.state.pullRight}
        onSetOpen={this.setSidebarOpen}
        shadow={this.state.shadow}
        sidebarClassName="sidebar-editor">
        {this.props.children}
      </ReactSidebar>
    )
  }
}

Sidebar.propTypes = {
  /* The main content display should be passed as children to this component */
  children: PropTypes.any
}

export default Sidebar
