/* eslint-disable */
// @ts-nocheck
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
  Radio,
  Accordion,
  Tooltip,
  OverlayTrigger,
  Well
} from 'react-bootstrap'
import Toggle from 'react-toggle'

const tooltip1 = (<Tooltip id='from-project-source' title='From project source'>
  Exact text matches from projects are used before exact matches in imported TM.
  Fuzzy text matches from projects are used before fuzzy matches in imported TM.
</Tooltip>)
const tooltip2 = (<Tooltip id='adjust-priority' title='Adjust priority'>
  Best match will be chosen based on the priority of selected projects. Exact
  matches take precendence.
</Tooltip>)
const tooltip3 = (
    <Tooltip id='copy-as-fuzzy-project' title='Copy as fuzzy - Project'>
      Can only copy as translated if the context is the same. Otherwise it will
      always use fuzzy.
    </Tooltip>)
const tooltip4 = (
    <Tooltip id='copy-as-translated-TM' title='Copy as translated - TM'>
      Less than 100% match still copies as fuzzy.
    </Tooltip>)

storiesOf('VersionTMMergeModal', module)
    .addDecorator((story) => (
        <div className='static-modal'>
          {story()}
        </div>
    ))
    .add('version selected', () => (

        <Modal
            id="tmMergeModal"
            show={true}
            onHide={action('onHide')}>
          <Modal.Header>
            <Modal.Title><span className="u-textNewBlue">Version TM Merge</span></Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <p className="introPar">
              Copy existing <strong>translations</strong> from similar documents
              in other projects and versions into this project version.
            </p>
            <Accordion>
              <Panel header={
                <p>Matching phrases are found in the selected projects and
                  imported TM, filtered using the active
                  conditions, then the best matching translation is copied to
                  the target project-version. more..</p>
              } eventKey="1">
                <p><img src="http://i.imgur.com/ezA992G.png"
                        alt="Version TM Merge workflow"/></p>
              </Panel>
            </Accordion>
            <Col xs={12} className='versionMergeContainer'>
              <Panel>
                <div className='versionMergeTarget'>
                  <div className='VersionMergeTitle'>
                    <span>To</span>
                    <span className="panel-name">Target</span>
                  </div>
                  <ul>
                    <li className='list-group-item to' title='target project'>
                      <span className="item"><Icon name='project'
                                                   className='s1 iconTMX'/>
                        Project</span>
                      <span className="item"><Icon name='version'
                                                   className='s1 iconTMX'/>
                        Version</span>
                      <span className="item" id="versionMergeDropdown">
                      <Icon name="language" className="s1 iconTMX"/>
                         <DropdownButton bsStyle='default' title='Assamese'
                           id='languageDropdown'
                           className='versionMergeDropdown'>
                  <MenuItem onClick={action('onClick')}
                            eventKey='1'>Assamese</MenuItem>
                         </DropdownButton>
                      </span>
                    </li>
                  </ul>
                </div>
              </Panel>
            </Col>
            <Col xs={12} className='versionMergeRow'>
              <p className="lead">For every potential translation:</p>
              <span className="u-textNewBlue">If text is less than </span>
              <DropdownButton bsStyle='default' title='80%'
                id='percentDropdown'
                className='versionMergeDropdown'>
                <MenuItem onClick={action('onClick')}
                          eventKey='1'>100%</MenuItem>
                <MenuItem onClick={action('onClick')}
                          eventKey='1'>90%</MenuItem>
                <MenuItem onClick={action('onClick')}
                          eventKey='1'>80%</MenuItem>
              </DropdownButton><span className="u-textNewBlue"> &nbsp;similar, don't use it.
            </span>
            </Col>
            <Col xs={12} className='versionMergeContainer'>
              <Panel>
                <Col xs={12}>
                  <div className='VersionMergeTitle versionMergeTitle-flex'>
                    <span>
                      <Toggle icons={false} defaultChecked={true}/>
                    </span>
                    <span>From </span>
                    <span className="panel-name">Project Source</span>
                    <OverlayTrigger placement='right' overlay={tooltip1}>
                      <Button bsStyle="link" className="tooltip-btn">
                        <Icon name="info" className="s0 iconInfoVersionMerge"/>
                      </Button>
                    </OverlayTrigger>
                  </div>
                </Col>
                <Col xs={12} className="versionMergeSource">
                  <span>Select TM from</span>
                  <Radio inline>
                    this project
                  </Radio>
                  <Radio inline>
                    all projects
                  </Radio>
                  <Radio inline readOnly checked>
                    some projects
                  </Radio>
                </Col>
                <Col xs={12} className='versionMergeSearch'>
                  <InputGroup>
                    <InputGroup.Addon>
                      <Icon name='search' className='s0' title='search'/>
                    </InputGroup.Addon>
                    <FormControl type='text'
                                 className='versionMergeSearch-input'
                    />
                  </InputGroup>
                </Col>
                <Col xs={12} className="select-project">
                  Selected projects: <Label>Project A <Icon name="cross"
                                                            className="n1"/></Label>
                </Col>
                <Col xs={6}>
          <span className='versionMergeTitle-adjusted VersionMergeTitle'>
            Select source project versions to merge
          </span>
                  <div className="panel-group proj-select">
                    <Panel header={
                      <h3>
                        <ListGroup className="checkbox">
                          <ListGroupItem className='list-group-item'
                                         title='target project'>
                            <Checkbox readOnly checked inline><Icon name='project'
                                                           className='s0 iconTMX'/>
                              Project A</Checkbox>
                          </ListGroupItem>
                        </ListGroup>
                      </h3>}>
                      <ListGroup fill>
                        <ListGroupItem className='v' title='target version'>
                          <Checkbox checked readOnly inline><Icon name='version'
                                                         className='s0 iconTMX'/>
                            Version 1 <Icon name='locked'
                                            className='s0 icon-locked'/></Checkbox>

                        </ListGroupItem>
                        <ListGroupItem className='v' title='target version'>
                          <Checkbox checked readOnly inline><Icon name='version'
                                                         className='s0 iconTMX'/>
                            Version 2 <Icon name='locked'
                                            className='s0 icon-locked'/></Checkbox>

                        </ListGroupItem>
                        <ListGroupItem className='v' title='target version'>
                          <Checkbox inline><Icon name='version'
                                                 className='s0 iconTMX'/>
                            Version 3</Checkbox>

                        </ListGroupItem>
                      </ListGroup>
                    </Panel>
                  </div>
                </Col>
                <Col xs={6}>
                   <span className="versionMergeTitle-adjusted VersionMergeTitle">
          Adjust priority of selected versions
          </span><br/>
                  <span className="u-textMuted versionMergeTitle-sub">(best first)
                   <OverlayTrigger placement='right' overlay={tooltip2}>
                      <Button bsStyle="link" className="tooltip-btn">
                        <Icon name="info" className="s0 iconInfoVersionMerge"/>
                      </Button>
                    </OverlayTrigger>
                  </span>
                  <ListGroup fill>
                    <ListGroupItem className='v'>
                      <Icon name='menu' className='n1 drag-handle'/>
                      Version 1 <Icon name='locked' className='s0 icon-locked'/>
                      <br/><span className='u-textMuted'>  Project A
                       </span>
                      {" "}
                      <Button bsSize='xsmall' className='close rm-version-btn'>
                        <Icon name='cross' className='n2 iconCross'
                              title='remove version'/>
                      </Button>
                    </ListGroupItem>
                    <ListGroupItem className='v'>
                      <Icon name='menu' className='n1 drag-handle'/>
                      Version 2 <Icon name='locked' className='s0 icon-locked'/>
                      <br/><span className='u-textMuted'>  Project A
                       </span>
                      {" "}
                      <Button bsSize='xsmall' className='close rm-version-btn'>
                        <Icon name='cross' className='n2 iconCross'
                              title='remove version'/>
                      </Button>
                    </ListGroupItem>
                  </ListGroup>
                </Col>
                <Col xs={12} className="validations">
                  <Col xs={12} md={4}>
                    If the translation is from a different <span>project</span>
                    <Radio validationState='success' checked readOnly>
                      I don't mind at all <Label bsStyle="success">Copy as
                      translated</Label>
                    </Radio>
                    <Radio validationState='warning'>
                      I need to review it <Label bsStyle="warning">Copy as
                      fuzzy</Label>
                      <OverlayTrigger placement='right' overlay={tooltip3}>
                        <Button bsStyle="link" className="tooltip-btn">
                          <Icon name="info" className="s0 iconInfoVersionMerge"/>
                        </Button>
                      </OverlayTrigger>
                    </Radio>
                  </Col>
                  <Col xs={12} md={4}>
                    If the translation is from a different <span>document</span>
                    <Radio validationState='success' checked readOnly>
                      I don't mind at all <Label bsStyle="success">Copy as
                      translated</Label>
                    </Radio>
                    <Radio validationState='warning'>
                      I need to review it <Label bsStyle="warning">Copy as
                      fuzzy</Label>
                      <OverlayTrigger placement='right' overlay={tooltip3}>
                        <Button bsStyle="link" className="tooltip-btn">
                          <Icon name="info" className="s0 iconInfoVersionMerge"/>
                        </Button>
                      </OverlayTrigger>
                    </Radio>
                    <Radio validationState='error'>
                      I don't want it <Label bsStyle="danger">Discard</Label>
                    </Radio>
                  </Col>
                  <Col xs={12} md={4}>
                    If the translation is from a different <span>context</span>
                    <Radio validationState='success'>
                      I don't mind at all <Label bsStyle="success">Copy as
                      translated</Label>
                    </Radio>
                    <Radio validationState='warning'>
                      I need to review it <Label bsStyle="warning">Copy as
                      fuzzy</Label>
                      <OverlayTrigger placement='right' overlay={tooltip3}>
                        <Button bsStyle="link" className="tooltip-btn">
                          <Icon name="info" className="s0 iconInfoVersionMerge"/>
                        </Button>
                      </OverlayTrigger>
                    </Radio>
                    <Radio validationState='error' checked readOnly>
                      I don't want it <Label bsStyle="danger">Discard</Label>
                    </Radio>
                  </Col>
                </Col>
                <Col xs={12}>
                  <Well>
                    <p>Translations which satisfy all conditions will copy as
                      <span className="text-bold u-textSuccess"> translated</span>.
                    </p>
                  </Well>
                </Col>
              </Panel>
            </Col>
            <Col xs={12} className='versionMergeContainer'>
              <Panel>
                <Col xs={12}>
                  <div className='VersionMergeTitle versionMergeTitle-flex'>
                    <span>
                    <Toggle icons={false} defaultChecked={true}/>
                    </span>
                    <span>From </span>
                    <span className="panel-name">TM Source</span>
                  </div>
                </Col>
                <Col xs={12} md={8}>
                  No projects, documents or context for TMX
                  <Radio validationState='success'>
                    I don't mind at all <Label bsStyle="success">Copy as
                    translated</Label>
                    <OverlayTrigger placement='right' overlay={tooltip4}>
                      <Button bsStyle="link" className="tooltip-btn">
                        <Icon name="info" className="s0 iconInfoVersionMerge"/>
                      </Button>
                    </OverlayTrigger>
                  </Radio>
                  <Radio validationState='warning' checked readOnly>
                    I need to review it <Label bsStyle="warning">Copy as
                    fuzzy</Label>
                  </Radio>
                </Col>
              </Panel>
            </Col>

          </Modal.Body>
          <Modal.Footer>
          <span className='bootstrap u-pullRight'>
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

    .add('no version selected', () => (

        <Modal
            id="tmMergeModal"
            show={true}
            onHide={action('onHide')}>
          <Modal.Header>
            <Modal.Title><span className="u-textNewBlue">Version TM Merge</span></Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <p className="intro">
              Copy existing <strong>translations</strong> from similar documents
              in other projects and versions into this project version.
            </p>
            <Accordion>
              <Panel header={
                <p>Matching phrases are found in the selected projects and
                  imported TM, filtered using the active
                  conditions, then the best matching translation is copied to
                  the target project-version. more..</p>
              } eventKey="1">
                <p><img src="http://i.imgur.com/ezA992G.png"
                        alt="Version TM Merge workflow"/></p>
              </Panel>
            </Accordion>
            <Col xs={12} className='versionMergeContainer'>
              <Panel>
                <div className='versionMergeTarget'>
                  <div className='VersionMergeTitle'>
                    <span>To</span>
                    <span className="panel-name">Target</span>
                  </div>
                  <ul>
                    <li className='list-group-item to' title='target project'>
                      <span className="item"><Icon name='project'
                                                   className='s1 iconTMX'/>
                        Project</span>
                      <span className="item"><Icon name='version'
                                                   className='s1 iconTMX'/>
                        Version</span>
                      <span className="item" id="versionMergeDropdown">
                      <Icon name="language" className="s1 iconTMX"/>
                         <DropdownButton bsStyle='default' title='Assamese'
                                         id='language-dropdown-basic'
                                         className='versionMergeDropdown'>
                  <MenuItem onClick={action('onClick')}
                            eventKey='1'>Assamese</MenuItem>
                         </DropdownButton>
                      </span>
                    </li>
                  </ul>
                </div>
              </Panel>
            </Col>
            <Col xs={12} className='versionMergeRow'>
              <p className="lead">For every potential translation:</p>
              <span className="u-textNewBlue">If text is less than </span>
              <DropdownButton bsStyle='default' title='80%'
                              id='language-dropdown-basic'
                              className='versionMergeDropdown'>
                <MenuItem onClick={action('onClick')}
                          eventKey='1'>100%</MenuItem>
                <MenuItem onClick={action('onClick')}
                          eventKey='1'>90%</MenuItem>
                <MenuItem onClick={action('onClick')}
                          eventKey='1'>80%</MenuItem>
              </DropdownButton><span className="u-textNewBlue"> &nbsp;similar, don't use it.
            </span>
            </Col>
            <Col xs={12} className='versionMergeContainer'>
              <Panel>
                <Col xs={12}>
                  <div className='VersionMergeTitle versionMergeTitle-flex'>
                    <span>
                    <Toggle icons={false} defaultChecked={true}/>
                    </span>
                    <span>From </span>
                    <span className="panel-name">Project Source</span>
                    <OverlayTrigger placement='right' overlay={tooltip1}>
                      <Button bsStyle="link" className="tooltip-btn">
                        <Icon name="info" className="s0 iconInfoVersionMerge"/>
                      </Button>
                    </OverlayTrigger>
                  </div>
                </Col>
                <Col xs={12} className="versionMergeSource">
                  <span>Select TM from</span>
                  <Radio inline>
                    this project
                  </Radio>
                  <Radio inline>
                    all projects
                  </Radio>
                  <Radio inline checked readOnly>
                    some projects
                  </Radio>
                </Col>
                <Col xs={12} className='versionMergeSearch'>
                  <InputGroup>
                    <InputGroup.Addon>
                      <Icon name='search' className='s0' title='search'/>
                    </InputGroup.Addon>
                    <FormControl type='text'
                                 className='versionMergeSearch-input'
                    />
                  </InputGroup>
                </Col>
                <Col xs={12} className="select-project">
                  Selected projects: <Label>Project A <Icon name="cross"
                                                            className="n1"/></Label>
                </Col>
                <Col xs={6}>
          <span className='versionMergeTitle-adjusted VersionMergeTitle'>
            Select source project versions to merge
          </span>
                  <div className="panel-group proj-select">
                    <Panel header={
                      <h3>
                        <ListGroup className="checkbox">
                          <ListGroupItem className='list-group-item'
                                         title='target project'>
                            <Checkbox inline><Icon name='project'
                                                   className='s0 iconTMX'/>
                              Project A</Checkbox>
                          </ListGroupItem>
                        </ListGroup>
                      </h3>}>
                      <ListGroup fill>
                        <ListGroupItem className='v' title='target version'>
                          <Checkbox inline><Icon name='version'
                                                 className='s0 iconTMX'/>
                            Version 1 <Icon name='locked'
                                            className='s0 icon-locked'/></Checkbox>

                        </ListGroupItem>
                        <ListGroupItem className='v' title='target version'>
                          <Checkbox inline><Icon name='version'
                                                 className='s0 iconTMX'/>
                            Version 2 <Icon name='locked'
                                            className='s0 icon-locked'/></Checkbox>

                        </ListGroupItem>
                        <ListGroupItem className='v' title='target version'>
                          <Checkbox inline><Icon name='version'
                                                 className='s0 iconTMX'/>
                            Version 3</Checkbox>

                        </ListGroupItem>
                      </ListGroup>
                    </Panel>
                  </div>
                </Col>
                <Col xs={6}>
                  <span className="no-v u-textMuted">Please select versions to sort<br/>
                    <Icon name="version" className="s8"/></span>
                </Col>
                <Col xs={12} className="validations">
                  <Col xs={12} md={4}>
                    If the translation is from a different <span>project</span>
                    <Radio validationState='success' checked readOnly>
                      I don't mind at all <Label bsStyle="success">Copy as
                      translated</Label>
                    </Radio>
                    <Radio validationState='warning'>
                      I need to review it <Label bsStyle="warning">Copy as
                      fuzzy</Label>
                      <OverlayTrigger placement='right' overlay={tooltip3}>
                        <Button bsStyle="link" className="tooltip-btn">
                          <Icon name="info" className="s0 iconInfoVersionMerge"/>
                        </Button>
                      </OverlayTrigger>
                    </Radio>
                  </Col>
                  <Col xs={12} md={4}>
                    If the translation is from a different <span>document</span>
                    <Radio validationState='success' checked readOnly>
                      I don't mind at all <Label bsStyle="success">Copy as
                      translated</Label>
                    </Radio>
                    <Radio validationState='warning'>
                      I need to review it <Label bsStyle="warning">Copy as
                      fuzzy</Label>
                      <OverlayTrigger placement='right' overlay={tooltip3}>
                        <Button bsStyle="link" className="tooltip-btn">
                          <Icon name="info" className="s0 iconInfoVersionMerge"/>
                        </Button>
                      </OverlayTrigger>
                    </Radio>
                    <Radio validationState='error'>
                      I don't want it <Label bsStyle="danger">Discard</Label>
                    </Radio>
                  </Col>
                  <Col xs={12} md={4}>
                    If the translation is from a different <span>context</span>
                    <Radio validationState='success'>
                      I don't mind at all <Label bsStyle="success">Copy as
                      translated</Label>
                    </Radio>
                    <Radio validationState='warning'>
                      I need to review it <Label bsStyle="warning">Copy as
                      fuzzy</Label>
                      <OverlayTrigger placement='right' overlay={tooltip3}>
                        <Button bsStyle="link" className="tooltip-btn">
                          <Icon name="info" className="s0 iconInfoVersionMerge"/>
                        </Button>
                      </OverlayTrigger>
                    </Radio>
                    <Radio validationState='error' checked readOnly>
                      I don't want it <Label bsStyle="danger">Discard</Label>
                    </Radio>
                  </Col>
                </Col>
                <Col xs={12}>
                  <Well>
                    <p>Translations which satisfy all conditions will copy as
                      <span className="text-bold u-textSuccess"> translated</span>.
                    </p>
                  </Well>
                </Col>
              </Panel>
            </Col>
            <Col xs={12} className='versionMergeContainer'>
              <Panel>
                <Col xs={12}>
                  <div className='VersionMergeTitle versionMergeTitle-flex'>
                    <span>
                    <Toggle icons={false} defaultChecked={true}/>
                    </span>
                    <span>From </span>
                    <span className="panel-name">TM Source</span>
                  </div>
                </Col>
                <Col xs={12} md={8}>
                  No projects, documents or context for TMX
                  <Radio validationState='success'>
                    I don't mind at all <Label bsStyle="success">Copy as
                    translated</Label>
                    <OverlayTrigger placement='right' overlay={tooltip4}>
                      <Button bsStyle="link" className="tooltip-btn">
                        <Icon name="info" className="s0 iconInfoVersionMerge"/>
                      </Button>
                    </OverlayTrigger>
                  </Radio>
                  <Radio validationState='warning' checked readOnly>
                    I need to review it <Label bsStyle="warning">Copy as
                    fuzzy</Label>
                  </Radio>
                </Col>
              </Panel>
            </Col>

          </Modal.Body>
          <Modal.Footer>
          <span className='bootstrap u-pullRight'>
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

    .add('disabled TM', () => (

        <Modal
            id="tmMergeModal"
            show={true}
            onHide={action('onHide')}>
          <Modal.Header>
            <Modal.Title><span className="u-textNewBlue">Version TM Merge</span></Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <p className="intro">
              Copy existing <strong>translations</strong> from similar documents
              in other projects and versions into this project version.
            </p>
            <Accordion>
              <Panel header={
                <p>Matching phrases are found in the selected projects and
                  imported TM, filtered using the active
                  conditions, then the best matching translation is copied to
                  the target project-version. more..</p>
              } eventKey="1">
                <p><img src="http://i.imgur.com/ezA992G.png"
                        alt="Version TM Merge workflow"/></p>
              </Panel>
            </Accordion>
            <Col xs={12} className='versionMergeContainer'>
              <Panel>
                <div className='versionMergeTarget'>
                  <div className='VersionMergeTitle'>
                    <span>To</span>
                    <span className="panel-name">Target</span>
                  </div>
                  <ul>
                    <li className='list-group-item to' title='target project'>
                      <span className="item"><Icon name='project'
                                                   className='s1 iconTMX'/>
                        Project</span>
                      <span className="item"><Icon name='version'
                                                   className='s1 iconTMX'/>
                        Version</span>
                      <span className="item" id="versionMergeDropdown">
                      <Icon name="language" className="s1 iconTMX"/>
                         <DropdownButton bsStyle='default' title='Assamese'
                                         id='language-dropdown-basic'
                                         className='versionMergeDropdown'>
                  <MenuItem onClick={action('onClick')}
                            eventKey='1'>Assamese</MenuItem>
                         </DropdownButton>
                      </span>
                    </li>
                  </ul>
                </div>
              </Panel>
            </Col>
            <Col xs={12} className='versionMergeRow'>
              <p className="lead">For every potential translation:</p>
              <span className="u-textNewBlue">If text is less than </span>
              <DropdownButton bsStyle='default' title='80%'
                              id='language-dropdown-basic'
                              className='versionMergeDropdown'>
                <MenuItem onClick={action('onClick')}
                          eventKey='1'>100%</MenuItem>
                <MenuItem onClick={action('onClick')}
                          eventKey='1'>90%</MenuItem>
                <MenuItem onClick={action('onClick')}
                          eventKey='1'>80%</MenuItem>
              </DropdownButton><span className="u-textNewBlue"> &nbsp;similar, don't use it.
            </span>
            </Col>
            <Col xs={12} className='versionMergeContainer'>
              <Panel>
                <Col xs={12}>
                  <div className='VersionMergeTitle versionMergeTitle-flex'>
                    <span>
                    <Toggle icons={false} defaultChecked={true}/>
                    </span>
                    <span>From </span>
                    <span className="panel-name">Project Source</span>
                    <OverlayTrigger placement='right' overlay={tooltip1}>
                      <Button bsStyle="link" className="tooltip-btn">
                        <Icon name="info" className="s0 iconInfoVersionMerge"/>
                      </Button>
                    </OverlayTrigger>
                  </div>
                </Col>
                <Col xs={12} className="versionMergeSource">
                  <span>Select TM from</span>
                  <Radio inline>
                    this project
                  </Radio>
                  <Radio inline>
                    all projects
                  </Radio>
                  <Radio inline checked readOnly>
                    some projects
                  </Radio>
                </Col>
                <Col xs={12} className='versionMergeSearch'>
                  <InputGroup>
                    <InputGroup.Addon>
                      <Icon name='search' className='s0' title='search'/>
                    </InputGroup.Addon>
                    <FormControl type='text'
                                 className='versionMergeSearch-input'
                    />
                  </InputGroup>
                </Col>
                <Col xs={12} className="select-project">
                  Selected projects: <Label>Project A <Icon name="cross"
                                                            className="n1"/></Label>
                </Col>
                <Col xs={6}>
          <span className='versionMergeTitle-adjusted VersionMergeTitle'>
            Select source project versions to merge
          </span>
                  <div className="panel-group proj-select">
                    <Panel header={
                      <h3>
                        <ListGroup className="checkbox">
                          <ListGroupItem className='list-group-item'
                                         title='target project'>
                            <Checkbox inline><Icon name='project'
                                                   className='s0 iconTMX'/>
                              Project A</Checkbox>
                          </ListGroupItem>
                        </ListGroup>
                      </h3>}>
                      <ListGroup fill>
                        <ListGroupItem className='v' title='target version'>
                          <Checkbox inline><Icon name='version'
                                                 className='s0 iconTMX'/>
                            Version 1 <Icon name='locked'
                                            className='s0 icon-locked'/></Checkbox>

                        </ListGroupItem>
                        <ListGroupItem className='v' title='target version'>
                          <Checkbox inline><Icon name='version'
                                                 className='s0 iconTMX'/>
                            Version 2 <Icon name='locked'
                                            className='s0 icon-locked'/></Checkbox>

                        </ListGroupItem>
                        <ListGroupItem className='v' title='target version'>
                          <Checkbox inline><Icon name='version'
                                                 className='s0 iconTMX'/>
                            Version 3</Checkbox>

                        </ListGroupItem>
                      </ListGroup>
                    </Panel>
                  </div>
                </Col>
                <Col xs={6}>
                  <span className="no-v u-textMuted">Please select versions to sort<br/>
                    <Icon name="version" className="s8"/></span>
                </Col>
                <Col xs={12} className="validations">
                  <Col xs={12} md={4}>
                    If the translation is from a different <span>project</span>
                    <Radio validationState='success' checked readOnly>
                      I don't mind at all <Label bsStyle="success">Copy as
                      translated</Label>
                    </Radio>
                    <Radio validationState='warning'>
                      I need to review it <Label bsStyle="warning">Copy as
                      fuzzy</Label>
                      <OverlayTrigger placement='right' overlay={tooltip3}>
                        <Button bsStyle="link" className="tooltip-btn">
                          <Icon name="info" className="s0 iconInfoVersionMerge"/>
                        </Button>
                      </OverlayTrigger>
                    </Radio>
                  </Col>
                  <Col xs={12} md={4}>
                    If the translation is from a different <span>document</span>
                    <Radio validationState='success' checked readOnly>
                      I don't mind at all <Label bsStyle="success">Copy as
                      translated</Label>
                    </Radio>
                    <Radio validationState='warning'>
                      I need to review it <Label bsStyle="warning">Copy as
                      fuzzy</Label>
                      <OverlayTrigger placement='right' overlay={tooltip3}>
                        <Button bsStyle="link" className="tooltip-btn">
                          <Icon name="info" className="s0 iconInfoVersionMerge"/>
                        </Button>
                      </OverlayTrigger>
                    </Radio>
                    <Radio validationState='error'>
                      I don't want it <Label bsStyle="danger">Discard</Label>
                    </Radio>
                  </Col>
                  <Col xs={12} md={4}>
                    If the translation is from a different <span>context</span>
                    <Radio validationState='success'>
                      I don't mind at all <Label bsStyle="success">Copy as
                      translated</Label>
                    </Radio>
                    <Radio validationState='warning'>
                      I need to review it <Label bsStyle="warning">Copy as
                      fuzzy</Label>
                      <OverlayTrigger placement='right' overlay={tooltip3}>
                        <Button bsStyle="link" className="tooltip-btn">
                          <Icon name="info" className="s0 iconInfoVersionMerge"/>
                        </Button>
                      </OverlayTrigger>
                    </Radio>
                    <Radio validationState='error' checked readOnly>
                      I don't want it <Label bsStyle="danger">Discard</Label>
                    </Radio>
                  </Col>
                </Col>
                <Col xs={12}>
                  <Well>
                    <p>Translations which satisfy all conditions will copy as
                      <span className="text-bold u-textSuccess"> translated</span>.
                    </p>
                  </Well>
                </Col>
              </Panel>
            </Col>
            <Col xs={12} className='versionMergeContainer'>
              <Panel>
                <Col xs={12}>
                  <div className='VersionMergeTitle versionMergeTitle-flex'>
                    <span>
                    <Toggle icons={false} defaultChecked={false}/>
                    </span>
                    <span>From </span>
                    <span className="panel-name">TM Source</span>
                  </div>
                </Col>
                <Col xs={12} md={8}>
                  No projects, documents or context for TMX
                  <Radio disabled validationState='success'>
                    I don't mind at all <Label bsStyle="success">Copy as
                    translated</Label>
                    <OverlayTrigger placement='right' overlay={tooltip4}>
                      <Button bsStyle="link" className="tooltip-btn">
                        <Icon name="info" className="s0 iconInfoVersionMerge"/>
                      </Button>
                    </OverlayTrigger>
                  </Radio>
                  <Radio disabled validationState='warning' readOnly>
                    I need to review it <Label bsStyle="warning">Copy as
                    fuzzy</Label>
                  </Radio>
                </Col>
              </Panel>
            </Col>

          </Modal.Body>
          <Modal.Footer>
          <span className='bootstrap u-pullRight'>
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
