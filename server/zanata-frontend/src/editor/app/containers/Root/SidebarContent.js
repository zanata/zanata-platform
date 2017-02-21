import React from 'react'
import { Tabs, Tab, FormGroup, InputGroup,
  FormControl } from 'react-bootstrap'
import Icon from '../../../../frontend/app/components/Icon'

const activityTitle = 'Activity'

const glossaryTitle = 'Glossary'

// https://dmfrancisco.github.io/react-icons/

const SidebarContent = React.createClass({
  render () {
    return (
      <div>
        <h1 className="sidebar-heading">
          <Icon name="info" className="s1" /> Details
        </h1>
        <div className="sidebar-wrapper">
          <ul className="sidebar-details">
            <li><span>Resource ID</span></li>
            <li><span>Message Context</span></li>
            <li><span>Reference</span></li>
            <li><span>Flags</span></li>
            <li><span>Source Comment</span></li>
            <li><span>Last Modified</span></li>
          </ul>
          <FormGroup className="trans-link">
            <InputGroup>
              <InputGroup.Addon><Icon name="copy"
                className="s1" />
              </InputGroup.Addon>
              <FormControl type="text" />
            </InputGroup>
          </FormGroup>
        </div>
        <Tabs defaultActiveKey={1}>
          <Tab eventKey={1} title={activityTitle}>
            <div className="sidebar-wrapper" id="tab1">
              Tab 1 content
            </div>
          </Tab>
          <Tab eventKey={2} title={glossaryTitle}>
            <div className="sidebar-wrapper" id="tab2">
              Tab 2 content
            </div>
          </Tab>
        </Tabs>
      </div>
    )
  }
})

export default SidebarContent
