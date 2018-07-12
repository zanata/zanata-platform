/* eslint-disable */
// @ts-nocheck
import React from 'react'
import { action } from '@storybook/addon-actions'
import { storiesOf } from '@storybook/react'
import { Tabs, Tab, Row, Col, Nav, NavItem, Well } from 'react-bootstrap'

storiesOf('Tabs', module)
    .add('horizontal', () => (
        <span>
          <h2><img src="https://i.imgur.com/v4qLk4p.png" width="42px" />Tabs - horizontal</h2>
          <Well bsSize="large">Add quick, dynamic tab functionality to transition through panes of local content.
          <hr /><ul><li><a href="">Props for react-bootstrap Tabs</a></li></ul></Well>
        <Tabs defaultActiveKey={2} onSelect={action('onSelect')}
              id='uncontrolled-tab-example'>
          <Tab eventKey={1} title='Tab 1' className='contentViewContainer'>
            Tab 1 content</Tab>
          <Tab eventKey={2} title='Tab 2' className='contentViewContainer'>
            Tab 2 content</Tab>
          <Tab eventKey={3} title='Tab 3' disabled
               className='contentViewContainer'>
            Tab 3 content</Tab>
        </Tabs>
          <hr />
          <h3>Custom tab layouts</h3>
          <p>For more complex layouts the flexible <code>TabContainer</code>, <code>TabContent</code>, and <code>TabPane</code> components along with any style of <code>Nav</code> allow you to quickly piece together your own Tabs component with additional markup needed.
            Just create a set of <code>NavItems</code> each with an <code>eventKey</code> corresponding to the eventKey of a <code>TabPane</code>. Wrap the whole thing in a <code>TabContainer</code> and you have fully functioning custom tabs component. Check out the below example making use of the grid system and pills.</p>
        </span>
    ))
    .add('vertical', () => (
        <span>
                            <h2><img src="https://i.imgur.com/v4qLk4p.png" width="42px" />Tabs - vertical</h2>

        <Tab.Container id='left-tabs-example' defaultActiveKey='first'
            onSelect={action('onSelect')} className='contentViewContainer'>
          <Row className='clearfix'>
            <Col sm={4}>
              <Nav bsStyle='pills' stacked>
                <NavItem eventKey='first'>
                  Tab 1
                </NavItem>
                <NavItem eventKey='second'>
                  Tab 2
                </NavItem>
              </Nav>
            </Col>
            <Col sm={8}>
              <Tab.Content animation>
                <Tab.Pane eventKey='first'>
                  Tab 1 content
                </Tab.Pane>
                <Tab.Pane eventKey='second'>
                  Tab 2 content
                </Tab.Pane>
              </Tab.Content>
            </Col>
          </Row>
        </Tab.Container>
          <hr />
        <p><code>Nav bsStyle="pills" stacked</code></p>
    </span>
    ))
