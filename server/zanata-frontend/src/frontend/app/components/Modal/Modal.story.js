import React from 'react'
import ReactDOM from 'react-dom'
import Draggable from 'react-draggable'
import {storiesOf} from '@kadira/storybook'
import {action, decorateAction} from '@kadira/storybook-addon-actions'
import {
  Button, Panel, Row, Table, Well, Checkbox, InputGroup, Col,
  FormControl, DropdownButton, MenuItem, ListGroup, ListGroupItem, PanelGroup,
  OverlayTrigger, Tooltip, Badge, Label
}
  from 'react-bootstrap'
import {Icon, Modal} from '../../components'
import Lorem from 'react-lorem-component'

const tooltipSort = (
    <Tooltip id='tooltipsort'>Best match will be chosen based on the priority of
      selected projects. Exact matches take precendence.
    </Tooltip>
)

const tooltipReadOnly = (
    <Tooltip id='tooltipreadonly'>Read only
    </Tooltip>
)

const tooltipAssam = (
    <Tooltip id='tooltipassam'>
      Assamese
      <br />
      অসমীয়া
    </Tooltip>
)

const tooltipGerman = (
    <Tooltip id='tooltipassam'>
      German
      <br />
      Deutsch
    </Tooltip>
)

const tooltipJapan = (
    <Tooltip id='tooltipassam'>
      Japanese
      <br />
      日本語
    </Tooltip>
)

const tooltipTMX = (
    <Tooltip id='tooltipTMXnote'>
      Some systems can't import TMX with srclang=*all*
    </Tooltip>
)

const docCountAss = (
    <Tooltip id='docall'>
      12 documents
    </Tooltip>
)

const docCountGerman = (
    <Tooltip id='docall'>
      12 documents
    </Tooltip>
)

const docCountJapan = (
    <Tooltip id='docall'>
      12 documents
    </Tooltip>
)

const docCountAll = (
    <Tooltip id='docall'>
      36 documents
    </Tooltip>
)



const heading1 = <h3><Checkbox checked> Project A</Checkbox></h3>
const heading2 = <h3><Checkbox> Project B
  <OverlayTrigger placement='top' overlay={tooltipReadOnly}>
    <Icon name='locked'
          className='s0 icon-locked'/>
  </OverlayTrigger>
</Checkbox></h3>

const currentProject = 'Current project'
const currentVersion = 'Current version'

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
    .add('TMX export - one language', () => (
        <Modal
            show={true}
            onHide={action('onHide')}>
          <Modal.Header>
            <Modal.Title>Export Project to TMX</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <span className='tmx-export'>
              <p>Are you sure you want to export this project to TMX?<br />
              <strong>All documents in this project have the source language
                &nbsp;<a href="">en-US</a>.</strong></p>
              <br />
               <p>
                 <Button
                     bsStyle='primary'
                     onClick={action('onClick')}>
                    Download
                 </Button>
              </p>
            </span>
          </Modal.Body>
        </Modal>
    ))
    .add('TMX export - multiple languages', () => (
        <Modal
            show={true}
            onHide={action('onHide')}>
          <Modal.Header>
            <Modal.Title>Export Project to TMX</Modal.Title>
          </Modal.Header>
          <Modal.Body>
             <span className='tmx-export'>
              <p>Are you sure you want to export this project to TMX?</p>
               <p className='lead'>Source languages</p>
                 <Table className='tmx-table'>
                  <tbody>
                    <tr>
                      <td>
                        <OverlayTrigger placement='left' overlay={tooltipAssam}>
                          <Button bsStyle='link'>as</Button>
                        </OverlayTrigger>
                      </td>
                      <td>
                        <OverlayTrigger placement='top' overlay={docCountAss}>
                          <Badge>
                            12 <Icon name='document' className='n1'/>
                          </Badge>
                        </OverlayTrigger>
                      </td>
                      <td>
                        <span className='tmx-dl'>
                          <Button
                          bsStyle='primary'
                          bsSize='small'
                          onClick={action('onClick')}>
                          Download
                          </Button>
                          <span className='asterix'>*</span>
                        </span>
                      </td>
                    </tr>
                    <tr>
                      <td>
                         <OverlayTrigger placement='left'
                          overlay={tooltipGerman}>
                          <Button bsStyle='link'>de</Button>
                        </OverlayTrigger>
                      </td>
                      <td>
                        <OverlayTrigger placement='top' overlay={docCountGerman}>
                          <Badge>
                            12 <Icon name='document' className='n1'/>
                          </Badge>
                        </OverlayTrigger>
                      </td>
                      <td>
                        <span className='tmx-dl'>
                          <Button
                              bsStyle='primary'
                              bsSize='small'
                              onClick={action('onClick')}>
                          Download
                          </Button>
                          <span className='asterix'>*</span>
                        </span>
                      </td>
                    </tr>
                    <tr>
                      <td>
                         <OverlayTrigger placement='left'
                         overlay={tooltipJapan}>
                          <Button bsStyle='link'>ja</Button>
                        </OverlayTrigger>
                      </td>
                      <td>
                        <OverlayTrigger placement='top' overlay={docCountJapan}>
                          <Badge>
                            12 <Icon name='document' className='n1'/>
                          </Badge>
                        </OverlayTrigger>
                      </td>
                      <td>
                        <span className='tmx-dl'>
                          <Button
                              bsStyle='primary'
                              bsSize='small'
                              onClick={action('onClick')}>
                          Download
                          </Button>
                          <span className='asterix'>*</span>
                        </span>
                      </td>
                    </tr>
                    <tr>
                      <td><strong>ALL</strong></td>
                      <td>
                        <OverlayTrigger placement='top' overlay={docCountAll}>
                          <Badge>
                            36 <Icon name='document' className='n1'/>
                          </Badge>
                        </OverlayTrigger>
                      </td>
                      <td>
                        <Button
                          bsStyle='primary'
                          bsSize='small'
                          onClick={action('onClick')}>
                          Download
                        </Button>
                      </td>
                    </tr>
                  </tbody>
                </Table>
                <p className='all-warning'>
                  <OverlayTrigger placement='top' overlay={tooltipTMX}>
                    <Button bsStyle='link'>
                      <Icon name='warning' className='n1'/>
                      &nbsp;Produces a TMX file which<br />some systems can't import.
                    </Button>
                  </OverlayTrigger>
                </p>
                <p className='text-warning'>* All translations of documents for
                  the selected source language will be included.</p>
             </span>
          </Modal.Body>
        </Modal>
    ))
    .add('TMX export - reindexing', () => (
        <Modal
            show={true}
            onHide={action('onHide')}>
          <Modal.Header>
            <Modal.Title>Export Project to TMX</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <span className='tmx-export'>
              <p><strong>Reindexing</strong></p>
              <p>
                1 of 445
              </p>
            </span>
          </Modal.Body>
        </Modal>
    ))
    .add('TMX export - reindexing finished', () => (
        <Modal
            show={true}
            onHide={action('onHide')}>
          <Modal.Header>
            <Modal.Title>Export Project to TMX</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <span className='tmx-export'>
              <p><strong>Reindexing complete</strong></p>
               <p>
                 <Button
                     bsStyle='primary'
                     onClick={action('onClick')}>
                    Download
                 </Button>
              </p>
            </span>
          </Modal.Body>
        </Modal>
    ))
    .add('version TM - diff doc id/context', () => (
        <Modal
            show={true}
            onHide={action('onHide')}>
          <Modal.Header>
            <Modal.Title>Version TM Merge</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <div>
              <p className="intro">Copy existing translations from similar documents
                in other projects and versions into this project version.</p>
              <Col xs={12} className='vmerge-row'>
                <Col xs={4}>
                  <span className='vmerge-title text-info'>TM match percentage</span>
                </Col>
                <Col xs={5}>
                  <DropdownButton bsStyle='default' bsSize='small'
                                  title='90% +'
                                  id='dropdown-basic'
                                  className='vmerge-ddown'>
                    <MenuItem onClick={action('onClick')} eventKey='1'>
                      100%</MenuItem>
                    <MenuItem onClick={action('onClick')} eventKey='2'>
                      80% +</MenuItem>
                    <MenuItem onClick={action('onClick')} eventKey='3' active>
                      70% +</MenuItem>
                    <MenuItem onClick={action('onClick')} eventKey='4'>
                      60% +</MenuItem>
                  </DropdownButton>
                </Col>
              </Col>
              <Col xs={12}>
                <Panel className='tm-panel'>
                  <ListGroup fill>
                    <ListGroupItem className=''><Checkbox checked>
                      Different DocID  <small>Document name and path</small>
                      <Label bsStyle='warning'>
                        Copy as Fuzzy
                      </Label>
                    </Checkbox>
                    </ListGroupItem>
                  </ListGroup>
                  <span className='and'>
                AND
                </span>
                  <ListGroup fill>
                    <ListGroupItem className=''><Checkbox checked>
                      Different Context  <small>resId, msgctxt</small>
                      <Label bsStyle='warning'>
                        Copy as Fuzzy
                      </Label>
                    </Checkbox>
                    </ListGroupItem>
                  </ListGroup>
                </Panel>
                <Panel className='tm-panel'>
                  <span className='or'>OR</span>
                  <ListGroup fill>
                    <ListGroupItem className=''><Checkbox>
                      Match from Imported TM
                    </Checkbox>
                    </ListGroupItem>
                  </ListGroup>
                </Panel>
              </Col>
              <Col xs={12} className='vmerge-row'>
                <Col xs={2}>
                  <span className='vmerge-title text-info'>Language</span>
                </Col>
                <Col xs={6}>
                  <DropdownButton bsStyle='default' bsSize='small'
                                  title='Japanese'
                                  id='dropdown-basic'
                                  className='vmerge-ddown'>
                    <MenuItem onClick={action('onClick')} eventKey='1'>
                      All</MenuItem>
                    <MenuItem divider/>
                    <MenuItem onClick={action('onClick')} eventKey='2'>
                      Korean</MenuItem>
                    <MenuItem onClick={action('onClick')} eventKey='3' active>
                      Japanese</MenuItem>
                    <MenuItem onClick={action('onClick')} eventKey='4'>
                      Russian</MenuItem>
                  </DropdownButton>
                </Col>
              </Col>
              <Col xs={12} className='vmerge-boxes'>
                <Panel>
                  <div className='vmerge-target'>
                    <div className='vmerge-title'>
                      <span className='text-info'>To  </span>
                      <span className='text-muted'>  Target</span>
                    </div>
                    <ul>
                      <li>
                        {currentProject}
                      </li>
                      <li>
                        {currentVersion}
                      </li>
                    </ul>
                  </div>
                </Panel>
              </Col>
              <Col xs={12} className='vmerge-boxes'>
                <Panel>
                  <Col xs={3}>
                    <div className='vmerge-title'>
                      <span className='text-info'>From  </span>
                      <span className='text-muted'>  Source</span>
                    </div>
                  </Col>
                  <Col xs={9} className='vmerge-searchbox'>
                    <InputGroup>
                      <InputGroup.Addon>
                        <Icon name='search'
                              className='s0'
                              title='search'/>
                      </InputGroup.Addon>
                      <FormControl type='text'
                                   value='Project'
                                   className='vmerge-searchinput'
                      />
                    </InputGroup>
                  </Col>
                  <Col xs={6}>
                 <span className='vmerge-adjtitle
                    vmerge-title'>Select source project versions to merge
              </span>
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
                      <div><span className='vmerge-adjtitle
                    vmerge-title'>
                      Adjust priority of selected versions</span>
                        <br /><span className='text-muted vmerge-adjsub'>(best first)
                      </span>
                        <OverlayTrigger placement='top' overlay={tooltipSort}>
                          <Icon name='info' className='s0 info-icon'/>
                        </OverlayTrigger>
                      </div>
                      <Draggable bounds='parent' axis='y' grid={[57, 57]}>
                        <ListGroupItem className='v'>
                          <Button bsStyle='link' className='btn-link-sort'>
                            <i className='fa fa-sort'></i>
                          </Button>
                          2.0
                          <span className='text-muted'>
                            Project A
                          </span>
                        </ListGroupItem>
                      </Draggable>
                      <Draggable bounds='parent' axis='y' grid={[57, 57]}>
                        <ListGroupItem className='v'>
                          <Button bsStyle='link' className='btn-link-sort'>
                            <i className='fa fa-sort'></i>
                          </Button>
                          1.0
                          <span className='text-muted'>
                            Project A
                          </span>
                        </ListGroupItem>
                      </Draggable>
                    </ListGroup>
                  </Col>
                </Panel>
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

    .add('version TM - same doc id/context', () => (
        <Modal
            show={true}
            onHide={action('onHide')}>
          <Modal.Header>
            <Modal.Title>Version TM Merge</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <div>
              <p className="intro">Copy existing translations from similar documents
                in other projects and versions into this project version.</p>
              <Col xs={12} className='vmerge-row'>
                <Col xs={4}>
                  <span className='vmerge-title text-info'>TM match percentage</span>
                </Col>
                <Col xs={5}>
                  <DropdownButton bsStyle='default' bsSize='small'
                                  title='100% '
                                  id='dropdown-basic'
                                  className='vmerge-ddown'>
                    <MenuItem onClick={action('onClick')} eventKey='1'>
                      90% +</MenuItem>
                    <MenuItem onClick={action('onClick')} eventKey='2'>
                      80% +</MenuItem>
                    <MenuItem onClick={action('onClick')} eventKey='3' active>
                      70% +</MenuItem>
                    <MenuItem onClick={action('onClick')} eventKey='4'>
                      60% +</MenuItem>
                  </DropdownButton>
                </Col>
              </Col>
              <Col xs={12}>
                <Panel className='tm-panel'>
                  <ListGroup fill>
                    <ListGroupItem className=''><Checkbox checked>
                      Different DocID  <small>Document name and path</small>
                      <Label bsStyle='info'>
                        Approved
                      </Label>
                    </Checkbox>
                    </ListGroupItem>
                  </ListGroup>
                  <span className='and'>
                AND
                </span>
                  <ListGroup fill>
                    <ListGroupItem className=''><Checkbox checked>
                      Different Context  <small>resId, msgctxt</small>
                      <Label bsStyle='success'>
                        Translated
                      </Label>
                    </Checkbox>
                    </ListGroupItem>
                  </ListGroup>
                </Panel>
                <Panel className='tm-panel'>
                  <span className='or'>OR</span>
                  <ListGroup fill>
                    <ListGroupItem className=''><Checkbox>
                      Match from Imported TM
                    </Checkbox>
                    </ListGroupItem>
                  </ListGroup>
                </Panel>
              </Col>
              <Col xs={12} className='vmerge-row'>
                <Col xs={2}>
                  <span className='vmerge-title text-info'>Language</span>
                </Col>
                <Col xs={6}>
                  <DropdownButton bsStyle='default' bsSize='small'
                                  title='Japanese'
                                  id='dropdown-basic'
                                  className='vmerge-ddown'>
                    <MenuItem onClick={action('onClick')} eventKey='1'>
                      All</MenuItem>
                    <MenuItem divider/>
                    <MenuItem onClick={action('onClick')} eventKey='2'>
                      Korean</MenuItem>
                    <MenuItem onClick={action('onClick')} eventKey='3' active>
                      Japanese</MenuItem>
                    <MenuItem onClick={action('onClick')} eventKey='4'>
                      Russian</MenuItem>
                  </DropdownButton>
                </Col>
              </Col>
              <Col xs={12} className='vmerge-boxes'>
                <Panel>
                  <div className='vmerge-target'>
                    <div className='vmerge-title'>
                      <span className='text-info'>To  </span>
                      <span className='text-muted'>  Target</span>
                    </div>
                    <ul>
                      <li>
                        {currentProject}
                      </li>
                      <li>
                        {currentVersion}
                      </li>
                    </ul>
                  </div>
                </Panel>
              </Col>
              <Col xs={12} className='vmerge-boxes'>
                <Panel>
                  <Col xs={3}>
                    <div className='vmerge-title'>
                      <span className='text-info'>From  </span>
                      <span className='text-muted'>  Source</span>
                    </div>
                  </Col>
                  <Col xs={9} className='vmerge-searchbox'>
                    <InputGroup>
                      <InputGroup.Addon>
                        <Icon name='search'
                              className='s0'
                              title='search'/>
                      </InputGroup.Addon>
                      <FormControl type='text'
                                   value='Project'
                                   className='vmerge-searchinput'
                      />
                    </InputGroup>
                  </Col>
                  <Col xs={6}>
                 <span className='vmerge-adjtitle
                    vmerge-title'>Select source project versions to merge
              </span>
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
                      <div><span className='vmerge-adjtitle
                    vmerge-title'>
                      Adjust priority of selected versions</span>
                        <br /><span className='text-muted vmerge-adjsub'>(best first)
                      </span>
                        <OverlayTrigger placement='top' overlay={tooltipSort}>
                          <Icon name='info' className='s0 info-icon'/>
                        </OverlayTrigger>
                      </div>
                      <Draggable bounds='parent' axis='y' grid={[57, 57]}>
                        <ListGroupItem className='v'>
                          <Button bsStyle='link' className='btn-link-sort'>
                            <i className='fa fa-sort'></i>
                          </Button>
                          2.0
                          <span className='text-muted'>
                            Project A
                          </span>
                        </ListGroupItem>
                      </Draggable>
                      <Draggable bounds='parent' axis='y' grid={[57, 57]}>
                        <ListGroupItem className='v'>
                          <Button bsStyle='link' className='btn-link-sort'>
                            <i className='fa fa-sort'></i>
                          </Button>
                          1.0
                          <span className='text-muted'>
                            Project A
                          </span>
                        </ListGroupItem>
                      </Draggable>
                    </ListGroup>
                  </Col>
                </Panel>
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

    .add('version TM - imported match', () => (
        <Modal
            show={true}
            onHide={action('onHide')}>
          <Modal.Header>
            <Modal.Title>Version TM Merge</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <div>
              <p className="intro">Copy existing translations from similar documents
                in other projects and versions into this project version.</p>
              <Col xs={12} className='vmerge-row'>
                <Col xs={4}>
                  <span className='vmerge-title text-info'>TM match percentage</span>
                </Col>
                <Col xs={5}>
                  <DropdownButton bsStyle='default' bsSize='small'
                                  title='90% +'
                                  id='dropdown-basic'
                                  className='vmerge-ddown'>
                    <MenuItem onClick={action('onClick')} eventKey='1'>
                      100% </MenuItem>
                    <MenuItem onClick={action('onClick')} eventKey='2'>
                      80% +</MenuItem>
                    <MenuItem onClick={action('onClick')} eventKey='3' active>
                      70% +</MenuItem>
                    <MenuItem onClick={action('onClick')} eventKey='4'>
                      60% +</MenuItem>
                  </DropdownButton>
                </Col>
              </Col>
              <Col xs={12}>
                <Panel className='tm-panel'>
                  <ListGroup fill>
                    <ListGroupItem className=''><Checkbox>
                      Different DocID  <small>Document name and path</small>
                    </Checkbox>
                    </ListGroupItem>
                  </ListGroup>
                  <span className='and'>
                AND
                </span>
                  <ListGroup fill>
                    <ListGroupItem className=''><Checkbox>
                      Different Context  <small>resId, msgctxt</small>
                    </Checkbox>
                    </ListGroupItem>
                  </ListGroup>
                </Panel>
                <Panel className='tm-panel'>
                  <span className='or'>OR</span>
                  <ListGroup fill>
                    <ListGroupItem className=''><Checkbox checked>
                      Match from Imported TM
                      <Label bsStyle='warning'>
                        Copy as Fuzzy
                      </Label>
                    </Checkbox>
                    </ListGroupItem>
                  </ListGroup>
                </Panel>
              </Col>
              <Col xs={12} className='vmerge-row'>
                <Col xs={2}>
                  <span className='vmerge-title text-info'>Language</span>
                </Col>
                <Col xs={6}>
                  <DropdownButton bsStyle='default' bsSize='small'
                                  title='Japanese'
                                  id='dropdown-basic'
                                  className='vmerge-ddown'>
                    <MenuItem onClick={action('onClick')} eventKey='1'>
                      All</MenuItem>
                    <MenuItem divider/>
                    <MenuItem onClick={action('onClick')} eventKey='2'>
                      Korean</MenuItem>
                    <MenuItem onClick={action('onClick')} eventKey='3' active>
                      Japanese</MenuItem>
                    <MenuItem onClick={action('onClick')} eventKey='4'>
                      Russian</MenuItem>
                  </DropdownButton>
                </Col>
              </Col>
              <Col xs={12} className='vmerge-boxes'>
                <Panel>
                  <div className='vmerge-target'>
                    <div className='vmerge-title'>
                      <span className='text-info'>To  </span>
                      <span className='text-muted'>  Target</span>
                    </div>
                    <ul>
                      <li>
                        {currentProject}
                      </li>
                      <li>
                        {currentVersion}
                      </li>
                    </ul>
                  </div>
                </Panel>
              </Col>
              <Col xs={12} className='vmerge-boxes'>
                <Panel>
                  <Col xs={3}>
                    <div className='vmerge-title'>
                      <span className='text-info'>From  </span>
                      <span className='text-muted'>  Source</span>
                    </div>
                  </Col>
                  <Col xs={9} className='vmerge-searchbox'>
                    <InputGroup>
                      <InputGroup.Addon>
                        <Icon name='search'
                              className='s0'
                              title='search'/>
                      </InputGroup.Addon>
                      <FormControl type='text'
                                   value='Project'
                                   className='vmerge-searchinput'
                      />
                    </InputGroup>
                  </Col>
                  <Col xs={6}>
                 <span className='vmerge-adjtitle
                    vmerge-title'>Select source project versions to merge
              </span>
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
                      <div><span className='vmerge-adjtitle
                    vmerge-title'>
                      Adjust priority of selected versions</span>
                        <br /><span className='text-muted vmerge-adjsub'>(best first)
                      </span>
                        <OverlayTrigger placement='top' overlay={tooltipSort}>
                          <Icon name='info' className='s0 info-icon'/>
                        </OverlayTrigger>
                      </div>
                      <Draggable bounds='parent' axis='y' grid={[57, 57]}>
                        <ListGroupItem className='v'>
                          <Button bsStyle='link' className='btn-link-sort'>
                            <i className='fa fa-sort'></i>
                          </Button>
                          2.0
                          <span className='text-muted'>
                            Project A
                          </span>
                        </ListGroupItem>
                      </Draggable>
                      <Draggable bounds='parent' axis='y' grid={[57, 57]}>
                        <ListGroupItem className='v'>
                          <Button bsStyle='link' className='btn-link-sort'>
                            <i className='fa fa-sort'></i>
                          </Button>
                          1.0
                          <span className='text-muted'>
                            Project A
                          </span>
                        </ListGroupItem>
                      </Draggable>
                    </ListGroup>
                  </Col>
                </Panel>
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
