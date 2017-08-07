import React, { Component } from 'react'
import PropTypes from 'prop-types'
import {Button} from 'react-bootstrap'
import Icon from '../../components/Icon'
import ValidationOptions from '../components/ValidationOptions'

class SidebarSettings extends Component {
  static propTypes = {
    /* close the sidebar */
    close: PropTypes.func.isRequired
  }

  render () {
    return (
      <div className="sidebar-settings">
        <h1 className="sidebar-heading">
          <Icon name="settings" className="s1" /> Settings
          <span className="s1 pull-right">
            <Button bsStyle="link" onClick={this.props.close}>
              <Icon name="cross" />
            </Button>
          </span>
        </h1>
        <div className="sidebar-wrapper">
          <ValidationOptions />
        </div>
      </div>
    )
  }
}

export default SidebarSettings
