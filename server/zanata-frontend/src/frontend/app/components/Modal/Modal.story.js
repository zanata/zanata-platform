import React from 'react'
import { storiesOf } from '@kadira/storybook'
import { action, decorateAction } from '@kadira/storybook-addon-actions'
import { Button, Panel, Row, Table, Well, Checkbox, InputGroup, Col,
  FormControl, DropdownButton, MenuItem, ListGroup, ListGroupItem, PanelGroup }
  from 'react-bootstrap'
import { Icon, Modal } from '../../components'
import Lorem from 'react-lorem-component'

storiesOf('Modal', module)
    .addDecorator((story) => (
          <div className="static-modal">
            {story()}
          </div>
    ))
    .add('default', () => (
        <Modal
            show={true}
            onHide={action('onHide')}>
          <Modal.Header>
            <Modal.Title>Modal heading</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <Lorem />
          </Modal.Body>
          <Modal.Footer>
          <span className='bootstrap pull-right'>
            <Row>
              <Button bsStyle='link'
                className='btn-left'
                onClick={action('onClick')}>
                Close
              </Button>
              <Button
                  bsStyle='primary'
                  onClick={action('onClick')}>
                Save
              </Button>
            </Row>
          </span>
          </Modal.Footer>
        </Modal>
    ))
    .add('version-merge', () => (
        <Modal
            show={true}
            onHide={action('onHide')}>
          <Modal.Header>
            <Modal.Title>Version Merge</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <div>
            <p>This feature copies existing translations from similar
            documents in other projects and versions into this project version.
              The translation state will be preserved (
              <span className='text-success'>translated</span> or
              <span className='text-info'> approved</span>).
            </p>
            <Well>
              <Checkbox>
                If metadata matches but source string does not, copy as
                <span className='vmerge-fuzzytxt'> fuzzy</span>.
              </Checkbox>
            </Well>
              <Col xs={2}>
                <span>Language</span>
              </Col>
              <Col xs={5}>
                <DropdownButton bsStyle='default' bsSize='small'
                   title='Dropdown button'
                   id='dropdown-basic'>
                  <MenuItem onClick={action('onClick')} eventKey='1'>
                    Action</MenuItem>
                  <MenuItem onClick={action('onClick')} eventKey='2'>
                    Another action</MenuItem>
                  <MenuItem  onClick={action('onClick')}eventKey='3' active>
                    Active Item</MenuItem>
                </DropdownButton>
              </Col>
              <Col xs={12}>
              <div className='vmerge-target'>
                To Target
                <ul>
                  <li>
                    Current project
                  </li>
                  <li>
                    Current version
                  </li>
                </ul>
              </div>
              </Col>
              <Col xs={2}>
                <span>From Source</span>
              </Col>
              <Col xs={10}>
              <InputGroup>
              <InputGroup.Addon>
                <Icon name='search'
                 className='s0'
                 title='search' />
              </InputGroup.Addon>
              <FormControl type='text'
                value='Project'
                 />
              </InputGroup>
              </Col>
            <div>
              <Col xs={6}>
                <PanelGroup defaultActiveKey="1" accordion>
                  <Panel header="Panel 1" eventKey="1">
                    <ListGroup fill>
                      <ListGroupItem className='v'><Checkbox>2.0</Checkbox>
                      </ListGroupItem>
                      <ListGroupItem className='v'><Checkbox>1.0</Checkbox>
                      </ListGroupItem>
                    </ListGroup>
                  </Panel>
                  <Panel header="Panel 2" eventKey="2">
                    <ListGroup fill>
                    <ListGroupItem className='v'><Checkbox>1.0</Checkbox>
                    </ListGroupItem>
                    </ListGroup>
                  </Panel>
                </PanelGroup>
              </Col>
                <Col xs={6}>
                  <ListGroup>
                    <ListGroupItem><h4>Adjust priority</h4>
                      <Icon name='info' className='s1' />
                    </ListGroupItem>
                    <ListGroupItem className='v'>Item 2</ListGroupItem>
                    <ListGroupItem className='v'>...</ListGroupItem>
                  </ListGroup>
                </Col>
            </div>
            </div>
          </Modal.Body>
          <Modal.Footer>
          <span className='bootstrap pull-right'>
            <Row>
              <Button bsStyle='link'
                      className='btn-left link-danger'
                      onClick={action('onClick')}>
                Cancel
              </Button>
              <Button
                  bsStyle='primary'
                  onClick={action('onClick')}>
                Merge translations
              </Button>
            </Row>
          </span>
          </Modal.Footer>
        </Modal>
    ))
