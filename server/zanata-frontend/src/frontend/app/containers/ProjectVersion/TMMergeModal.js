import React, {Component} from 'react'
import PropTypes from 'prop-types'
import {connect} from 'react-redux'
import { differenceWith, isEqual, throttle } from 'lodash'
import {arrayMove} from 'react-sortable-hoc'
import {Button, Panel, Row, InputGroup, Col, FormControl} from 'react-bootstrap'
import {Icon, Modal, LoaderText} from '../../components'
import ProjectVersionPanels from './ProjectVersionPanels'
import DraggableVersionPanels from '../../components/DraggableVersionPanels'
import SelectableDropdown from '../../components/SelectableDropdown'
import {ProjectVersionVertical} from './ProjectVersionDisplay'
import {ProjectVersionOptions} from './ProjectVersionOptions'
import {
  fetchVersionLocales,
  fetchProjectPage,
  toggleTMMergeModal,
  mergeVersionFromTM
} from '../../actions/version-actions'
import {ProjectType, LocaleType} from '../../utils/prop-types-util.js'

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
    startMergeProcess: PropTypes.func.isRequired,
    notification: PropTypes.object,
    triggered: PropTypes.bool.isRequired,
    fetchingProject: PropTypes.bool.isRequired,
    fetchingLocale: PropTypes.bool.isRequired
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
      { 'leading': false })
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
        selectedLanguage: locales.length === 0 ? undefined : locales[0]
      }))
    }
  }
  onPercentSelection = (percent) => {
    this.setState({
      matchPercentage: percent
    })
  }
  onLanguageSelection = (language) => {
    this.setState({
      selectedLanguage: language
    })
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
  isProjectVersionSelected = (projectSlug, version) => {
    return this.state.selectedVersions
      .find(p => p.projectSlug === projectSlug && p.version.id === version.id)
  }
  // Remove/Add version from fromProjectVersion array based on selection
  onVersionCheckboxChange = (version, projectSlug) => {
    const versionChecked = this.isProjectVersionSelected(projectSlug, version)
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
  summitForm = () => {
    this.props.startMergeProcess(this.props.projectSlug,
      this.props.versionSlug, this.state)
  }
  render () {
    const {
      showTMMergeModal,
      openTMMergeModal,
      projectSlug,
      versionSlug,
      projectVersions,
      locales,
      notification,
      triggered,
      fetchingProject,
      fetchingLocale
    } = this.props
    const localeToDisplay = l => l.displayName
    const percentValueToDisplay = p => `${p}%`
    const showHide = showTMMergeModal ? {display: 'block'} : {display: 'none'}

    return (
      <Modal style={showHide}
        show
        onHide={openTMMergeModal}>
        <Modal.Header>
          <Modal.Title>Version TM Merge</Modal.Title>
          <p className="text-danger modal-danger">
            {notification && notification.message}</p>
        </Modal.Header>
        <Modal.Body>
          <div>
            <p className="intro">
              Copy existing translations from similar documents
              in other projects and versions into this project version.
            </p>
            <Col xs={12} className='vmerge-row'>
              <Col xs={4}>
                <span className='vmerge-title text-info'>
                  TM match threshold
                </span>
              </Col>
              <Col xs={5}>
                <SelectableDropdown title={this.state.matchPercentage + '%'}
                  id='percent-dropdown-basic' className='vmerge-ddown'
                  onSelectDropdownItem={this.onPercentSelection}
                  selectedValue={this.state.matchPercentage}
                  valueToDisplay={percentValueToDisplay}
                  values={[80, 90, 100]} />
              </Col>
            </Col>
            <ProjectVersionOptions
              differentDocId={this.state.differentDocId}
              differentContext={this.state.differentContext}
              fromImportedTM={this.state.fromImportedTM}
              onDocIdCheckboxChange={this.onDocIdCheckboxChange}
              onContextCheckboxChange={this.onContextCheckboxChange}
              onImportedCheckboxChange={this.onImportedCheckboxChange} />
            <Col xs={12} className='vmerge-row'>
              <Col xs={2}>
                <span className='vmerge-title text-info'>Language</span>
              </Col>
              {fetchingLocale ? undefined : <Col xs={6}>
                <SelectableDropdown
                  id='language-dropdown-basic' className='vmerge-ddown'
                  onSelectDropdownItem={this.onLanguageSelection}
                  selectedValue={this.state.selectedLanguage}
                  valueToDisplay={localeToDisplay}
                  values={locales} />
              </Col>}
              <Col xs={6}>
                <LoaderText loading={fetchingLocale}
                  loadingText={'Fetching Locales'} />
              </Col>
            </Col>
            <Col xs={12} className='vmerge-boxes'>
              <Panel>
                <div className='vmerge-target'>
                  <div className='vmerge-title'>
                    <span className='text-info'>To</span>
                    <span className='text-muted'>Target</span>
                  </div>
                  <ProjectVersionVertical projectSlug={projectSlug}
                    versionSlug={versionSlug} />
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
                  <span className='vmerge-adjtitle vmerge-title'>
                    Select source project versions to merge
                  </span>
                  <div>
                    <LoaderText loading={fetchingProject}
                      loadingText={'Fetching Projects'} />
                  </div>
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
              <Button bsStyle='link' className='btn-left link-danger'
                onClick={openTMMergeModal}>
                Cancel
              </Button>
              <Button bsStyle='primary' onClick={this.summitForm}
                disabled={triggered}>
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
    triggered: state.projectVersion.TMMerge.triggered,
    locales: state.projectVersion.locales,
    projectVersions: state.projectVersion.TMMerge.projectVersions,
    notification: state.projectVersion.notification,
    fetchingProject: state.projectVersion.fetchingProject,
    fetchingLocale: state.projectVersion.fetchingLocale
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
    },
    startMergeProcess: (projectSlug, versionSlug, mergeOptions) => {
      dispatch(mergeVersionFromTM(projectSlug, versionSlug, mergeOptions))
    }
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(TMMergeModal)
