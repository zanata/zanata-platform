import React from 'react'
import {storiesOf} from '@storybook/react'
import {action, decorateAction} from '@storybook/addon-actions'
import {Modal, Icon} from '../../components'
import {
  Row,
  Button,
  Col,
  Panel,
  DropdownButton,
  MenuItem,
  InputGroup,
  FormControl,
  ListGroup,
  ListGroupItem,
  Label,
  Checkbox,
  Radio
} from 'react-bootstrap'

storiesOf('VersionTMMergeModal', module)
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
            <Modal.Title>Version TM Merge</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <p className="intro">
              Copy existing <strong>translations</strong> from similar documents
              in other projects and versions into this project version.
            </p>
            <Col xs={12} className='vmerge-boxes'>
              <Panel>
                <div className='vmerge-target'>
                  <div className='vmerge-title'>
                    <span className='text-info'>To</span>
                    <span>Target</span>
                  </div>
                  <ul>
                    <li className='list-group-item' title='target project'>
                      <span><Icon name='project' className='s0 tmx-icon'/>
                        Project</span>
                      <span><Icon name='version' className='s0 tmx-icon'/>
                        Version</span>
                      <span className='vmerge-title text-info'
                            id="languages-dd">
                      <Icon name="language" className="s1"/>
                      Language
                         <DropdownButton bsStyle='default' title='Assamese'
                                         id='language-dropdown-basic'
                                         className='vmerge-ddown'>
                  <MenuItem onClick={action('onClick')}
                            eventKey='1'>Assamese</MenuItem>
                         </DropdownButton>
                      </span>
                    </li>
                  </ul>
                </div>
              </Panel>
            </Col>
            <Col xs={12} className='vmerge-row'>
              <h3>For every potential translation:</h3>
              <span
                  className='vmerge-title text-info'>If text is less than </span>
              <DropdownButton bsStyle='default' title='80%'
                              id='language-dropdown-basic'
                              className='vmerge-ddown'>
                <MenuItem onClick={action('onClick')}
                          eventKey='1'>100%</MenuItem>
                <MenuItem onClick={action('onClick')}
                          eventKey='1'>90%</MenuItem>
                <MenuItem onClick={action('onClick')}
                          eventKey='1'>80%</MenuItem>
              </DropdownButton>&nbsp;similar, don't use it.
            </Col>
            <Col xs={12} className='vmerge-boxes'>
              <Panel>
                <Col xs={12}>
                  <div className='vmerge-title'>
                    <span className='text-info'>From </span>
                    <span>Project Source</span>
                  </div>
                </Col>
                <Col xs={12}>
                  Select TM from
                  <Radio inline>
                    this project
                  </Radio>
                  <Radio inline>
                    all projects
                  </Radio>
                  <Radio inline checked>
                    some projects
                  </Radio>
                </Col>
                <Col xs={12} className='vmerge-searchbox'>
                  <InputGroup>
                    <InputGroup.Addon>
                      <Icon name='search' className='s0' title='search'/>
                    </InputGroup.Addon>
                    <FormControl type='text'
                                 className='vmerge-searchinput'
                    />
                  </InputGroup>
                </Col>
                <Col xs={12}>
                  Selected projects: <Label>Project A <Icon name="cross"
                                                            className="n1"/></Label>
                </Col>
                <Col xs={6}>
          <span className='vmerge-adjtitle vmerge-title'>
            Select source project versions to merge
          </span>
                  <Panel header={
                    <h3>
                      <ListGroup>
                        <ListGroupItem className='list-group-item'
                                       title='target project'>
                          <Checkbox checked inline><Icon name='project'
                                                         className='s0 tmx-icon'/>
                            Projectname</Checkbox>
                        </ListGroupItem>
                      </ListGroup>
                    </h3>}>
                    <ListGroup fill>
                      <ListGroupItem className='v' title='target version'>
                        <Checkbox checked inline><Icon name='version'
                                                       className='s0 tmx-icon'/>
                          Versionname</Checkbox>

                      </ListGroupItem>
                    </ListGroup>
                  </Panel>
                </Col>
                <Col xs={6}>
                   <span className="vmerge-adjtitle vmerge-title">
          Adjust priority of selected versions
          </span><br/>
                  <span className="text-muted vmerge-adjsub">(best first)</span>
                  <ListGroup fill>
                    <ListGroupItem className='v'>
                      <Icon name='menu' className='n1 drag-handle'/>
                      <span className='text-muted'> Version1
                       </span> <Icon name='locked' className='s0 icon-locked'/>
                      {" "}
                      <Button bsSize='xsmall' className='close rm-version-btn'>
                        <Icon name='cross' className='n2 crossicon'
                              title='remove version'/>
                      </Button>
                    </ListGroupItem>
                  </ListGroup>
                </Col>
                <Col xs={12}>
                  <Col xs={12} md={4}>
                    If the translation is from a different project
                    <Radio validationState='success'>
                      I don't mind at all <Label bsStyle="success">Copy as translated</Label>
                    </Radio>
                    <Radio validationState='warning'>
                      I will need to review it <Label bsStyle="warning">Copy as translated</Label>
                    </Radio>
                  </Col>
                  <Col xs={12} md={4}>
                    If the translation is from a different document
                    <Radio validationState='success'>
                      I don't mind at all <Label bsStyle="success">Copy as translated</Label>
                    </Radio>
                    <Radio validationState='warning'>
                      I will need to review it <Label bsStyle="warning">Copy as translated</Label>
                    </Radio>
                    <Radio validationState='error'>
                      I don't want it <Label bsStyle="danger">Discard</Label>
                    </Radio>
                  </Col>
                  <Col xs={12} md={4}>
                    If the translation is from a different context
                    <Radio validationState='success'>
                      I don't mind at all <Label bsStyle="success">Copy as translated</Label>
                    </Radio>
                    <Radio validationState='warning'>
                      I will need to review it <Label bsStyle="warning">Copy as translated</Label>
                    </Radio>
                    <Radio validationState='error'>
                      I don't want it <Label bsStyle="danger">Discard</Label>
                    </Radio>
                  </Col>
                </Col>
              </Panel>
            </Col>
            <Col xs={12} className='vmerge-boxes'>
              <Panel>
                <Col xs={12}>
                  <div className='vmerge-title'>
                    <span className='text-info'>From </span>
                    <span>TM Source</span>
                  </div>
                </Col>
                <Col xs={12} md={8}>
                  No projects, documents or context for TMX
                  <Radio validationState='success'>
                    I don't mind at all <Label bsStyle="success">Copy as translated</Label>
                  </Radio>
                  <Radio validationState='warning'>
                    I will need to review it <Label bsStyle="warning">Copy as translated</Label>
                  </Radio>
                </Col>
              </Panel>
            </Col>

          </Modal.Body>
          <Modal.Footer>
          <span className='bootstrap pull-right'>
            <Row>
              <Button bsStyle='link'
                      className='btn-left'
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
