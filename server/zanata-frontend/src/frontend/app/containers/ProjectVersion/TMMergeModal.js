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

import {
  toggleTMMergeModal
} from '../../actions/version-actions'

/**
 * Root component for TM Merge Modal
 */
class TMMergeModal extends Component {
  static propTypes = {
    showTMMergeModal: PropTypes.bool,
    openTMMergeModal: PropTypes.func.isRequired,
    projectSlug: PropTypes.string.isRequired,
    versionSlug: PropTypes.string.isRequired
  }
  constructor (props) {
    super(props)
    this.state = {
      matchPercentage: 100,
      differentDocId: false,
      differentContext: false,
      fromImportedTM: false,
      language: '',
      fromProjectVersion: []
    }
  }
  onPercentSelection = (percent) => {
    this.setState({
      ...this.state,
      matchPercentage: percent
    })
  }
  render () {
    const action = (message) => {
      // TODO: Use Real Actions
      // console.info(message)
    }
    const tooltipReadOnly = (
      <Tooltip id='tooltipreadonly'>Read only
      </Tooltip>
    )
    const heading1 = <h3><Checkbox> Project A</Checkbox></h3>
    const heading2 = <h3><Checkbox> Project B
      <OverlayTrigger placement='top' overlay={tooltipReadOnly}>
        <Icon name='locked'
          className='s0 icon-locked' />
      </OverlayTrigger>
    </Checkbox></h3>

    const currentProject = this.props.projectSlug
    const currentVersion = this.props.versionSlug
    const short = this.props.showTMMergeModal
    const showHide = short ? {display: 'block'} : {display: 'none'}
    const percentageItems = [100, 90, 80].map(percentage => {
      return (
        <IndexedMenuItem onClick={this.onPercentSelection}
          percentage={percentage}
          matchPercentage={this.state.matchPercentage} />
      )
    })
    // Different DocID Checkbox handling
    const onDocIdCheckboxChange = () => {
      this.setState({
        ...this.state,
        differentDocId: !this.state.differentDocId
      })
    }
    const docIdLabel = this.state.differentDocId
        ? (<Label bsStyle='warning'>
        Copy as Fuzzy
        </Label>)
        : (<Label bsStyle='danger'>
        Don't Copy
        </Label>)
    // Different Context Checkbox handling
    const onContextCheckboxChange = () => {
      this.setState({
        ...this.state,
        differentContext: !this.state.differentContext
      })
    }
    const differentContextLabel = this.state.differentContext
        ? (<Label bsStyle='warning'>
          Copy as Fuzzy
        </Label>)
        : (<Label bsStyle='danger'>
          Don't Copy
        </Label>)
    // Match from Imported TM Checkbox handling
    const onImportedCheckboxChange = () => {
      this.setState({
        ...this.state,
        fromImportedTM: !this.state.fromImportedTM
      })
    }
    const matchImportedLabel = this.state.fromImportedTM
        ? (<Label bsStyle='warning'>
          Copy as Fuzzy
        </Label>)
        : (<Label bsStyle='danger'>
          Don't Copy
        </Label>)
    return (
      <Modal style={showHide}
        show
        onHide={this.props.openTMMergeModal}>
        <Modal.Header>
          <Modal.Title>Version TM Merge</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <div>
            <p className="intro">Copy existing translations from similar
              documents
              in other projects and versions into this project version.
            </p>
            <Col xs={12} className='vmerge-row'>
              <Col xs={4}>
                <span
                  className='vmerge-title text-info'>TM match threshold</span>
              </Col>
              <Col xs={5}>
                <DropdownButton bsStyle='default' bsSize='small'
                  title={this.state.matchPercentage + '%'}
                  id='dropdown-basic'
                  className='vmerge-ddown'>
                  {percentageItems}
                </DropdownButton>
              </Col>
            </Col>
            <Col xs={12}>
              <Panel className='tm-panel'>
                <ListGroup fill>
                  <ListGroupItem className=''>
                    <Checkbox onChange={onDocIdCheckboxChange}
                      checked={this.state.differentDocId}>
                    Different DocID
                      <small>{" "}Document name and path</small>
                      {docIdLabel}
                    </Checkbox>
                  </ListGroupItem>
                </ListGroup>
                <span className='and'>
              AND
                </span>
                <ListGroup fill>
                  <ListGroupItem className=''>
                    <Checkbox onChange={onContextCheckboxChange}
                      checked={this.state.differentContext}>
                      Different Context
                      <small>{" "} resId, msgctxt</small>
                      {differentContextLabel}
                    </Checkbox>
                  </ListGroupItem>
                </ListGroup>
              </Panel>
              <Panel className='tm-panel'>
                <span className='or'>OR</span>
                <ListGroup fill>
                  <ListGroupItem className=''>
                    <Checkbox onChange={onImportedCheckboxChange}>
                      Match from Imported TM
                      <small>{" "}</small>
                      {matchImportedLabel}
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
                        <ListGroupItem className='v'><Checkbox>2.0
                        </Checkbox>
                        </ListGroupItem>
                        <ListGroupItem className='v'><Checkbox>1.0
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
                onClick={this.props.openTMMergeModal}>
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

class IndexedMenuItem extends Component {
  static propTypes = {
    percentage: PropTypes.number.isRequired,
    onClick: PropTypes.func.isRequired,
    matchPercentage: PropTypes.number.isRequired
  }
  onClick = () => {
    this.props.onClick(this.props.percentage)
  }
  render () {
    const i = this.props.percentage
    return (
      <MenuItem onClick={this.onClick}
        eventKey={i} key={i} active={i === this.props.matchPercentage}>
        {i}%
      </MenuItem>
    )
  }
}

const mapStateToProps = (state) => {
  return {
    showTMMergeModal: state.projectVersion.TMMerge.show
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    openTMMergeModal: () => {
      dispatch(toggleTMMergeModal())
    }
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(TMMergeModal)

