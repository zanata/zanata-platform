import React, { Component, PropTypes } from 'react'
import ReactSidebar from 'react-sidebar'
import SidebarContent from '../SidebarContent'

const defaultState = {
  docked: true
}

/**
 * Sidebar component that tracks its own state for open, close, etc. and
 * displays content from SidebarContent.
 */
class Sidebar extends Component {
  constructor () {
    super()
    this.setOpen = ::this.setOpen
    this.close = ::this.close
    this.state = defaultState
  }

  // These functions are to dock/undock the sidebar when depending on screen
  // width, but the undocked sidebar has display issues at the moment so this is
  // disabled.
  // componentWillMount () {
  //   const mql = window.matchMedia('(min-width: 800px)')
  //   mql.addListener(this.mediaQueryChanged.bind(this))
  //   this.setState({mql: mql, docked: mql.matches})
  // }
  //
  // componentWillUnmount () {
  //   this.state.mql.removeListener(this.mediaQueryChanged)
  // }
  //
  // mediaQueryChanged () {
  //   this.setState({docked: this.state.mql.matches})
  // }

  setOpen (open) {
    this.props.setSidebarVisible(open)
  }

  close () {
    this.props.setSidebarVisible(false)
  }

  render () {
    const content = <SidebarContent
      close={this.close}
    />

    return (
      <ReactSidebar
        sidebar={content}
        docked={this.props.open && this.state.docked}
        open={this.props.open}
        pullRight
        onSetOpen={this.setOpen}
        shadow
        sidebarClassName="sidebar-editor">
        {this.props.children}
      </ReactSidebar>
    )
  }
}

Sidebar.propTypes = {
  open: PropTypes.bool.isRequired,
  setSidebarVisible: PropTypes.func.isRequired,
  /* The main content display should be passed as children to this component */
  children: PropTypes.any
}

export default Sidebar
