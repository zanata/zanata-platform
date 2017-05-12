import React from 'react'
import { storiesOf } from '@kadira/storybook'
import { action, decorateAction } from '@kadira/storybook-addon-actions'
import { Button, Panel, Row, Table, Well, Checkbox, InputGroup, Col,
  FormControl, DropdownButton, MenuItem, ListGroup, ListGroupItem, PanelGroup }
  from 'react-bootstrap'
import { Icon, Modal } from '../../components'
import Lorem from 'react-lorem-component'


const heading1 =  <h3><Checkbox checked> Project A</Checkbox></h3>
const heading2 =  <h3><Checkbox> Project B  <Icon name='locked'
   className='s0' />
</Checkbox></h3>

storiesOf('Modal', module)
    .addDecorator((story) => (
          <div className='static-modal'>
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
                <span className='vmerge-title text-info'>Language</span>
              </Col>
              <Col xs={6}>
                <DropdownButton bsStyle='default' bsSize='small'
                   title='Dropdown button'
                   id='dropdown-basic'
                   className='vmerge-ddown' >
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
                <div  className='vmerge-title'>
                <span className='text-info'>To </span>
                <span className='text-muted'> Target</span>
                </div>
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
              <Col xs={3}>
                <div  className='vmerge-title'>
                  <span className='text-info'>From </span>
                  <span className='text-muted'> Source</span>
                </div>
              </Col>
              <Col xs={9}>
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
            <Col xs={12} className='vmerge-boxes'>
              <Col xs={6}>
                <PanelGroup defaultActiveKey='1' accordion>
                  <Panel header={heading1} eventKey='1'>
                    <ListGroup fill>
                      <ListGroupItem className='v'><Checkbox checked>2.0
                      </Checkbox>
                      </ListGroupItem>
                      <ListGroupItem className='v'><Checkbox checked>1.0
                      </Checkbox>
                      </ListGroupItem>
                    </ListGroup>
                  </Panel>
                  <Panel header={heading2} eventKey='2'>
                    <ListGroup fill>
                    <ListGroupItem className='v'><Checkbox>1.0</Checkbox>
                    </ListGroupItem>
                    </ListGroup>
                  </Panel>
                </PanelGroup>
              </Col>
                <Col xs={6}>
                  <ListGroup>
                    <ListGroupItem><span className='vmerge-adjtitle
                    vmerge-title'>
                      Adjust priority </span>
                      <span className='text-muted vmerge-adjsub'>(best first)
                      </span>
                      <Icon name='info' className='s0 info-icon' />
                    </ListGroupItem>
                    <ListGroupItem className='v'>2.0</ListGroupItem>
                    <ListGroupItem className='v'>1.0</ListGroupItem>
                  </ListGroup>
                </Col>
            </Col>
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
