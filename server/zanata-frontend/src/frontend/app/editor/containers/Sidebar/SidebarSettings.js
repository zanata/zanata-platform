import React, {PropTypes} from 'react'
import {Button, Checkbox} from 'react-bootstrap'
import Icon from '../../../frontend/app/components/Icon'

const SidebarSettings = React.createClass({

  propTypes: {
    /* close the sidebar */
    close: PropTypes.func.isRequired
  },

  sidebarDetails () {
    return (
      <div>
        <ul>
          <li>
            <Checkbox checked>
              Communicate with server for validation
            </Checkbox>
          </li>
          <li>
            <Checkbox>
              Validate when translation is saved
            </Checkbox>
          </li>
          <li>
            <Checkbox>
              Display validation warning/error in editor
            </Checkbox>
          </li>
          <li>
            <Checkbox checked>
              Visual indicator of warning or error
            </Checkbox>
          </li>
          <li>
            <Checkbox checked>
              Handle translation rollback if validation failed
            </Checkbox>
          </li>
          <li>
            <Checkbox checked>
              Show notifications
            </Checkbox>
          </li>
        </ul>
        <div className="settings-reftrans">
          <label>Show reference translations from:</label>
          <br />
          <select className="settings-select">
            <option value="None">None</option>
            <option value="Option 1">Option 1</option>
            <option value="Option 2">Option 2</option>
          </select>
        </div>
        <h2 className="validation">Validation options</h2>
        <ul>
          <li>
            <Checkbox checked>
              HTML/XML tags
            </Checkbox>
          </li>
          <li>
            <Checkbox checked>
              Java variables
            </Checkbox>
          </li>
          <li>
            <Checkbox checked>
              Leading/trailing newline (\n)
            </Checkbox>
          </li>
          <li>
            <Checkbox>
              Positional printf (XSI extension)
            </Checkbox>
          </li>
          <li>
            <Checkbox>
              Printf variables
            </Checkbox>
          </li>
          <li>
            <Checkbox checked>
              Tab characters (\t)
            </Checkbox>
          </li>
          <li>
            <Checkbox checked>
              XML entity reference
            </Checkbox>
          </li>
        </ul>
        <Button title="Save"
          className="Button Button--spacing u-rounded Button--primary">
          Save
        </Button>
        <Button title="Load"
          className="Button Button--spacing u-rounded Button--primary">
          Load
        </Button>
        <Button title="Restore defaults"
          className="Button u-rounded Button--secondary">
          Restore defaults
        </Button>
      </div>
    )
  },

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
          {this.sidebarDetails()}
        </div>
      </div>
    )
  }
})

export default SidebarSettings
