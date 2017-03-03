import React, { PropTypes } from 'react'
import { Button } from 'react-bootstrap'
import Icon from '../../../frontend/app/components/Icon'

const SidebarSettings = React.createClass({

  propTypes: {
    /* close the sidebar */
    close: PropTypes.func.isRequired
  },

  sidebarDetails () {
    return (
      <p>TEST</p>
    )
  },

  render () {
    return (
      <div>
        <h1 className="sidebar-heading">
          <Icon name="settings" className="s1" /> Settings
          <span className="s1 pull-right">
            <Button bsStyle="link" onClick={this.props.close}>
              <Icon name="cross" />
            </Button>
          </span>
        </h1>
        <div className="sidebar-wrapper">
          {this.sidebarDetails()}
        </div>
      </div>
    )
  }
})

export default SidebarSettings
