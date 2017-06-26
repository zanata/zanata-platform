import React, {Component} from 'react'
import PropTypes from 'prop-types'
import {connect} from 'react-redux'
import { differenceWith, isEqual, throttle } from 'lodash'
import {arrayMove} from 'react-sortable-hoc'
import {
  Button, Panel, Row, Checkbox, InputGroup, Col, Label, FormControl, ListGroup,
  ListGroupItem
} from 'react-bootstrap'
import {Icon, Modal} from '../../components'
import ProjectVersionPanels from '../../components/ProjectVersionPanels'
import DraggableVersionPanels from '../../components/DraggableVersionPanels'
import SelectableDropdown from '../../components/SelectableDropdown'
import {
  fetchVersionLocales,
  fetchProjectPage,
  toggleTMMergeModal
} from '../../actions/version-actions'
import {ProjectType, LocaleType} from '../../utils/prop-types-util.js'

const CopyLabel = (props) => {
  return props.copy
    ? (<Label bsStyle='warning'>
      Copy as Fuzzy
    </Label>)
    : (<Label bsStyle='danger'>
      Don't Copy
    </Label>)
}
CopyLabel.propTypes = {
  copy: PropTypes.bool.isRequired
}

/**
 * Root component for TM Merge Modal
 */
class TMMergeModal extends Component {
  static propTypes = {
    /* params: projectSlug and versionSlug */
    fetchVersionLocales: PropTypes.func.isRequired,
    showTMMergeModal: PropTypes.bool.isRequired,
    openTMMergeModal: PropTypes.func.isRequired,
    /* params: project object */
    openProjectPage: PropTypes.func.isRequired,
    projectSlug: PropTypes.string.isRequired,
    versionSlug: PropTypes.string.isRequired,
    locales: PropTypes.arrayOf(LocaleType).isRequired,
    projectVersions: PropTypes.arrayOf(ProjectType).isRequired,
    notification: PropTypes.object
  }
  constructor (props) {
    super(props)
    this.state = {
      matchPercentage: 100,
      differentDocId: false,
      differentContext: false,
      fromImportedTM: false,
      selectedLanguage: undefined,
      selectedVersions: [],
      projectSearchTerm: this.props.projectSlug
    }
    /* Chose 1 second as an arbitrary period between searches.
     * leading and trailing options specify we want to search to after the user
     * stops typing. */
    this.throttleHandleSearch = throttle(props.openProjectPage, 1000,
      { 'leading': false }, { 'trailing': true })
  }
  componentDidMount () {
    this.props.fetchVersionLocales(
      this.props.projectSlug, this.props.versionSlug)
    this.props.openProjectPage(this.state.projectSearchTerm)
  }
  componentWillReceiveProps (nextProps) {
    const locales = nextProps.locales
    if (!this.state.selectedLanguage) {
      this.setState((prevState, props) => ({
        // FIXME change to locale object for submission, use display name
        selectedLanguage: locales.length === 0 ? '' : locales[0].displayName
      }))
    }
  }
  onPercentSelection = (percent) => {
    this.setState((prevState, props) => ({
      matchPercentage: percent
    }))
  }
  onLanguageSelection = (language) => {
    this.setState((prevState, props) => ({
      selectedLanguage: language
    }))
  }
  onProjectSearchChange = (event) => {
    const textEntered = event.target.value
    this.setState((prevState, props) => ({
      projectSearchTerm: textEntered
    }), this.throttleHandleSearch(this.state.projectSearchTerm))
  }
  flushProjectSearch = (event) => {
    if (event.key === 'Enter') {
      this.throttleHandleSearch.flush()
    }
  }
  // Sorts the selectedVersion list after a reorder of the Draggable List
  onDragMoveEnd = ({oldIndex, newIndex}) => {
    this.setState((prevState, props) => ({
      selectedVersions:
        arrayMove(prevState.selectedVersions, oldIndex, newIndex)
    }))
  }
  // Remove a version from fromProjectVersion array
  removeProjectVersion = (project, version) => {
    this.setState((prevState, props) => ({
      selectedVersions: prevState.selectedVersions.filter(({ projectSlug,
       version: { id } }) => projectSlug !== project || id !== version.id)}))
  }
  // Remove all versions of a Project from fromProjectVersion array
  removeAllProjectVersions = (projectSlug) => {
    this.setState((prevState) => {
      return {
        selectedVersions: prevState.selectedVersions
          .filter(p => projectSlug !== p.projectSlug)
      }
    })
  }
  // Add a version to fromProjectVersion array
  pushProjectVersion = (projectVersion) => {
    this.setState((prevState, props) => ({
      selectedVersions: [...prevState.selectedVersions, projectVersion]
    }))
  }
  // Add all versions of a Project to fromProjectVersion array
  pushAllProjectVersions = (projectVersions) => {
    this.setState(prevState => {
      return {
        selectedVersions: prevState.selectedVersions.concat(projectVersions)
      }
    })
  }
  inSelection = (projectSlug, version) => {
    return this.state.selectedVersions
      .find(p => p.projectSlug === projectSlug && p.version.id === version.id)
  }
  // Remove/Add version from fromProjectVersion array based on selection
  onVersionCheckboxChange = (version, projectSlug) => {
    const versionChecked = this.inSelection(projectSlug, version)
    versionChecked ? this.removeProjectVersion(projectSlug, version)
      : this.pushProjectVersion({version, projectSlug: projectSlug})
  }
  // Remove/Add all project versions to version list
  onAllVersionCheckboxChange = (project) => {
    const projectSlug = project.id
    const versionsInProject = project.versions.map((version) => {
      return {version, projectSlug}
    })
    const diff = differenceWith(versionsInProject,
      this.state.selectedVersions, isEqual)
    if (diff.length === 0) {
      // we already have all versions in this project selected,
      // the operation is to remove them all
      this.removeAllProjectVersions(projectSlug)
    } else {
      // we want to add all versions to the selection
      this.pushAllProjectVersions(diff)
    }
  }
  // Different DocID Checkbox handling
  onDocIdCheckboxChange = () => {
    this.setState((prevState, props) => ({
      differentDocId: !prevState.differentDocId
    }))
  }
  // Different Context Checkbox handling
  onContextCheckboxChange = () => {
    this.setState((prevState, props) => ({
      differentContext: !prevState.differentContext
    }))
  }
  // Match from Imported TM Checkbox handling
  onImportedCheckboxChange = () => {
    this.setState((prevState, props) => ({
      fromImportedTM: !prevState.fromImportedTM
    }))
  }
  render () {
    const {
      showTMMergeModal,
      openTMMergeModal,
      projectSlug,
      versionSlug,
      projectVersions,
      locales,
      notification
    } = this.props
    const action = (message) => {
      // TODO: Use Real Actions
      // console.info('clicked')
    }
    const languages = locales.map(l => l.displayName)
    const percentValueToDisplay = p => `${p}%`
    const showHide = showTMMergeModal ? {display: 'block'} : {display: 'none'}

    return (
      <Modal style={showHide}
        show
        onHide={openTMMergeModal}>
        <Modal.Header>
          <Modal.Title>Version TM Merge</Modal.Title>
          <p className="text-danger">
            {notification && notification.message}</p>
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
                <SelectableDropdown title={this.state.matchPercentage + '%'}
                  id='dropdown-basic' className='vmerge-ddown'
                  onSelectDropdownItem={this.onPercentSelection}
                  selectedValue={this.state.matchPercentage}
                  valueToDisplay={percentValueToDisplay}
                  values={[80, 90, 100]} />
              </Col>
            </Col>
            <Col xs={12}>
              <Panel className='tm-panel'>
                <ListGroup fill>
                  <ListGroupItem className=''>
                    <Checkbox onChange={this.onDocIdCheckboxChange}
                      checked={this.state.differentDocId}>
                    Different DocID
                      <small>{" "}Document name and path</small>
                      <CopyLabel copy={this.state.differentDocId} />
                    </Checkbox>
                  </ListGroupItem>
                </ListGroup>
                <span className='and'>
              AND
                </span>
                <ListGroup fill>
                  <ListGroupItem className=''>
                    <Checkbox onChange={this.onContextCheckboxChange}
                      checked={this.state.differentContext}>
                      Different Context
                      <small>{" "} resId, msgctxt</small>
                      <CopyLabel copy={this.state.differentContext} />
                    </Checkbox>
                  </ListGroupItem>
                </ListGroup>
              </Panel>
              <Panel className='tm-panel'>
                <span className='or'>OR</span>
                <ListGroup fill>
                  <ListGroupItem className=''>
                    <Checkbox onChange={this.onImportedCheckboxChange}>
                      Match from Imported TM
                      <small>{" "}</small>
                      <CopyLabel copy={this.state.fromImportedTM} />
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
                <SelectableDropdown
                  id='dropdown-basic' className='vmerge-ddown'
                  onSelectDropdownItem={this.onLanguageSelection}
                  selectedValue={this.state.selectedLanguage}
                  values={languages} />
              </Col>
            </Col>
            <Col xs={12} className='vmerge-boxes'>
              <Panel>
                <div className='vmerge-target'>
                  <div className='vmerge-title'>
                    <span className='text-info'>To</span>
                    <span className='text-muted'>Target</span>
                  </div>
                  <ul>
                    <li>
                      <Icon name='project' className='s0 tmx-icon' />
                      {projectSlug}
                    </li>
                    <li>
                      <Icon name='version' title='version'
                        className='s0 tmx-icon' />
                      {versionSlug}
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
                        title='search'
                      />
                    </InputGroup.Addon>
                    <FormControl type='text'
                      value={this.state.projectSearchTerm}
                      className='vmerge-searchinput'
                      onChange={this.onProjectSearchChange}
                      onKeyDown={this.flushProjectSearch}
                    />
                  </InputGroup>
                </Col>
                <Col xs={6}>
                  <span className='vmerge-adjtitle
                  vmerge-title'>Select source project versions to merge
                  </span>
                  <ProjectVersionPanels projectVersions={projectVersions}
                    selectedVersions={this.state.selectedVersions}
                    onVersionCheckboxChange={this.onVersionCheckboxChange}
                    onAllVersionCheckboxChange={this.onAllVersionCheckboxChange}
                    projectList={this.props.projectVersions}
                  />
                </Col>
                <Col xs={6}>
                  <DraggableVersionPanels
                    selectedVersions={this.state.selectedVersions}
                    onDraggableMoveEnd={this.onDragMoveEnd} />
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
                onClick={openTMMergeModal}>
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
  return {
    showTMMergeModal: state.projectVersion.TMMerge.show,
    locales: state.projectVersion.locales,
    projectVersions: state.projectVersion.TMMerge.projectVersions,
    notification: state.projectVersion.notification
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    fetchVersionLocales: (project, version) => {
      dispatch(fetchVersionLocales(project, version))
    },
    openProjectPage: (project) => {
      dispatch(fetchProjectPage(project))
    },
    openTMMergeModal: () => {
      dispatch(toggleTMMergeModal())
    }
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(TMMergeModal)

