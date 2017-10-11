import React from 'react'
import { storiesOf, action } from '@storybook/react'
import { Tabs, Tab, Row, Col, Nav, NavItem } from 'react-bootstrap'

storiesOf('Tabs', module)
    .add('horizontal', () => (
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
    ))
    .add('vertical', () => (
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
    ))
