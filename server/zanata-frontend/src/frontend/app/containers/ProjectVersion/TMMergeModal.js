import React, {PropTypes, Component} from 'react'
import {connect} from 'react-redux'
import Draggable from 'react-draggable'
// import {storiesOf} from '@kadira/storybook'
// import ReactDOM from 'react-dom'
import {
  Button, Panel, Row, Checkbox, InputGroup, Col,
  FormControl, DropdownButton, MenuItem, ListGroup, ListGroupItem, PanelGroup,
  OverlayTrigger, Tooltip, Label
}
  from 'react-bootstrap'
import {Icon, Modal} from '../../components'

/**
 * Root component for TM Merge Modal
 */
class TMMergeModal extends Component {
  static propTypes = {

    openTMMergeModal: PropTypes.func.required
  }

  render () {
    const action = (message) => {
      // TODO: Use Real Actions
      console.info(message)
    }
    const tooltipReadOnly = (
      <Tooltip id='tooltipreadonly'>Read only
      </Tooltip>
    )
    const heading1 = <h3><Checkbox checked> Project A</Checkbox></h3>
    const heading2 = <h3><Checkbox> Project B
      <OverlayTrigger placement='top' overlay={tooltipReadOnly}>
        <Icon name='locked'
          className='s0 icon-locked' />
      </OverlayTrigger>
    </Checkbox></h3>

    const currentProject = 'Current project'
    const currentVersion = 'Current version'
    return (
      <Modal
        show
        onHide={action('onHide')}>
        <Modal.Header>
          <Modal.Title>Version TM Merge</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <div>
            <p className="intro">Copy existing translations from similar
              documents
              in other projects and versions into this project version.
            </p>
            <span className='pull-right'>
              <Button
                bsStyle='primary'
                onClick={action('onClick')}>
                Merge translations
              </Button>
            </span>
            <Col xs={12} className='vmerge-row'>
              <Col xs={4}>
                <span
                  className='vmerge-title text-info'>TM match percentage</span>
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
                    Different DocID
                    <small>Document name and path</small>
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
                    Different Context
                    <small>resId, msgctxt</small>
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
                  <MenuItem divider />
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
                    <span className='text-info'>To </span>
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
                    <span className='text-info'>From</span>
                    <span className='text-muted'>Source</span>
                  </div>
                </Col>
                <Col xs={9} className='vmerge-searchbox'>
                  <InputGroup>
                    <InputGroup.Addon>
                      <Icon name='search'
                        className='s0'
                        title='search' />
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
                      <br /><span className='text-muted vmerge-adjsub'>
                        (best first)
                      </span>
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
    )
  }
}

const mapStateToProps = (state) => {
  return {}
}

const mapDispatchToProps = (dispatch) => {
  return {}
}

export default connect(mapStateToProps, mapDispatchToProps)(TMMergeModal)

