import React from 'react'
import { Tabs, Tab } from 'react-bootstrap'
import Icon from '../../components/Icon'

const activityTitle = 'Activity'

const glossaryTitle = 'Glossary'

// https://dmfrancisco.github.io/react-icons/

const SidebarContent = React.createClass({
  render () {
    return (
      <div>
        <h1><Icon name="info" className="s1" /> Details</h1>
        <ul>
          <li>Resource ID</li>
          <li>Message Context</li>
          <li>Reference</li>
          <li>Flags</li>
          <li>Source Comment</li>
          <li>Last Modified</li>
        </ul>
        <Tabs defaultActiveKey={1}>
          <Tab eventKey={1} title={activityTitle}>
            Tab 1 content
          </Tab>
          <Tab eventKey={2} title={glossaryTitle}>
            Tab 2 content
          </Tab>
        </Tabs>
      </div>
    )
  }
})

export default SidebarContent
