import React, {Component} from 'react'
import PropTypes from 'prop-types'
import {connect} from 'react-redux'
import { differenceWith, isEqual, throttle } from 'lodash'
import {arrayMove} from 'react-sortable-hoc'
import {Button, Panel, Row, InputGroup, Col, FormControl} from 'react-bootstrap'
import {
  Icon, Modal, LoaderText, SelectableDropdown, DraggableVersionPanels,
  TriCheckbox
} from '../../components'
import ProjectVersionPanels from './ProjectVersionPanels'
import {ProjectVersionVertical} from './project-version-displays'
import {ProjectVersionOptions} from './ProjectVersionOptions'
import CancellableProgressBar
  from '../../components/ProgressBar/CancellableProgressBar'
import {
  fetchVersionLocales,
  fetchProjectPage,
  toggleTMMergeModal,
  mergeVersionFromTM,
  queryTMMergeProgress,
  cancelTMMergeRequest,
  currentTMMergeProcessFinished
} from '../../actions/version-actions'
import {
  ProjectType, LocaleType, FromProjectVersionType, processStatusType
} from '../../utils/prop-types-util.js'
import {isProcessEnded} from '../../utils/EnumValueUtils'
import {getVersionLanguageSettingsUrl} from '../../utils/UrlHelper'
import {TMMergeOptionsCallbackPropType, TMMergeOptionsValuePropType}
  from './TMMergeOptionsCommon'

const percentValueToDisplay = v => `${v}%`
const localeToDisplay = l => l.displayName
const DO_NOT_RENDER = undefined

/*
 * Component to display TM merge options
 */
const MergeOptions = (
  {
    projectSlug,
    versionSlug,
    projectVersions,
    locales,
    fetchingProject,
    fetchingLocale,
    mergeOptions,
    onContextCheckboxChange,
    onDocIdCheckboxChange,
    onImportedCheckboxChange,
    onIgnoreDifferentDocIdChange,
    onIgnoreDifferentContextChange,
    onImportedTMCopyRuleChange,
    onPercentSelection,
    onLanguageSelection,
    onProjectSearchChange,
    flushProjectSearch,
    onVersionCheckboxChange,
    onAllVersionCheckboxChange,
    onFromAllProjectsChange,
    onDragMoveEnd,
    removeProjectVersion
  }) => {
  const noResults = (projectVersions.length === 0) ? 'No results' : ''
  const fromVersionsPanel = mergeOptions.fromAllProjects ? DO_NOT_RENDER : (
    <Col xs={12} className='vmerge-boxes'>
      <Panel>
        <Col xs={3}>
          <div className='vmerge-title'>
            <span className='text-info'>From</span>
            <span>Source</span>
          </div>
        </Col>
        <Col xs={9} className='vmerge-searchbox'>
          <InputGroup>
            <InputGroup.Addon>
              <Icon name='search' className='s0' title='search' />
            </InputGroup.Addon>
            <FormControl type='text'
              value={mergeOptions.projectSearchTerm}
              className='vmerge-searchinput'
              onChange={onProjectSearchChange}
              onKeyDown={flushProjectSearch}
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
            <span className="text-muted">{noResults}</span>
          </div>
          <ProjectVersionPanels projectVersions={projectVersions}
            selectedVersions={mergeOptions.selectedVersions}
            onVersionCheckboxChange={onVersionCheckboxChange}
            onAllVersionCheckboxChange={onAllVersionCheckboxChange}
          />
        </Col>
        <Col xs={6}>
          <DraggableVersionPanels
            selectedVersions={mergeOptions.selectedVersions}
            onDraggableMoveEnd={onDragMoveEnd}
            removeVersion={removeProjectVersion} />
        </Col>
      </Panel>
    </Col>
  )
  return (
    <div>
      <p className="intro">
        Copy existing translations from similar documents
        in other projects and versions into this project version.
      </p>
      <Col xs={12} className='vmerge-row'>
        <Col xs={4}>
          <span className='vmerge-title text-info'>TM match threshold</span>
        </Col>
        <Col xs={5}>
          <SelectableDropdown title={mergeOptions.matchPercentage + '%'}
            id='percent-dropdown-basic' className='vmerge-ddown'
            onSelectDropdownItem={onPercentSelection}
            selectedValue={mergeOptions.matchPercentage}
            valueToDisplay={percentValueToDisplay}
            values={[80, 90, 100]} />
        </Col>
      </Col>
      <ProjectVersionOptions
        differentDocId={mergeOptions.differentDocId}
        differentContext={mergeOptions.differentContext}
        fromImportedTM={mergeOptions.fromImportedTM}
        onDocIdCheckboxChange={onDocIdCheckboxChange}
        onContextCheckboxChange={onContextCheckboxChange}
        onImportedCheckboxChange={onImportedCheckboxChange}
        ignoreDifferentDocId={mergeOptions.ignoreDifferentDocId}
        ignoreDifferentContext={mergeOptions.ignoreDifferentContext}
        importedTMCopyAsTranslated={mergeOptions.importedTMCopyAsTranslated}
        onIgnoreDifferentDocIdChange={onIgnoreDifferentDocIdChange}
        onIgnoreDifferentContextChange={onIgnoreDifferentContextChange}
        onImportedTMCopyRuleChange={onImportedTMCopyRuleChange}
      />
      <Col xs={12} className='vmerge-row'>
        <Col xs={2}>
          <span className='vmerge-title text-info' id="languages-dd">
            <Icon name="language" className="s1" />
            Language
          </span>
        </Col>
        {fetchingLocale ? undefined : <Col xs={6}>
          <SelectableDropdown
            id='language-dropdown-basic' className='vmerge-ddown'
            onSelectDropdownItem={onLanguageSelection}
            selectedValue={mergeOptions.selectedLanguage}
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
              <span>Target</span>
            </div>
            <ProjectVersionVertical projectSlug={projectSlug}
              versionSlug={versionSlug} />
          </div>
        </Panel>
      </Col>
      <Col xs={12} className='vmerge-boxes'>
        <Panel>
          <div className='checkbox'>
            <label>
              <TriCheckbox onChange={onFromAllProjectsChange}
                checked={mergeOptions.fromAllProjects} />
                Search TM from all projects
            </label>
          </div>
        </Panel>
      </Col>
      {fromVersionsPanel}
    </div>
  )
}
MergeOptions.propTypes = {
  projectSlug: PropTypes.string.isRequired,
  versionSlug: PropTypes.string.isRequired,
  locales: PropTypes.arrayOf(LocaleType).isRequired,
  projectVersions: PropTypes.arrayOf(ProjectType).isRequired,
  fetchingProject: PropTypes.bool.isRequired,
  fetchingLocale: PropTypes.bool.isRequired,
  mergeOptions: PropTypes.shape({
    matchPercentage: PropTypes.number.isRequired,
    fromAllProjects: PropTypes.bool.isRequired,
    selectedLanguage: LocaleType,
    selectedVersions: PropTypes.arrayOf(FromProjectVersionType),
    projectSearchTerm: PropTypes.string,
    ...TMMergeOptionsValuePropType
  }).isRequired,
  onFromAllProjectsChange: PropTypes.func.isRequired,
  onPercentSelection: PropTypes.func.isRequired,
  onLanguageSelection: PropTypes.func.isRequired,
  onProjectSearchChange: PropTypes.func.isRequired,
  flushProjectSearch: PropTypes.func.isRequired,
  onVersionCheckboxChange: PropTypes.func.isRequired,
  onAllVersionCheckboxChange: PropTypes.func.isRequired,
  onDragMoveEnd: PropTypes.func.isRequired,
  removeProjectVersion: PropTypes.func.isRequired,
  ...TMMergeOptionsCallbackPropType
}

/**
 * Root component for TM Merge Modal
 */
class TMMergeModal extends Component {
  static propTypes = {
    /* params: projectSlug and versionSlug */
    fetchVersionLocales: PropTypes.func.isRequired,
    showTMMergeModal: PropTypes.bool.isRequired,
    toggleTMMergeModal: PropTypes.func.isRequired,
    /* params: project object */
    fetchProjectPage: PropTypes.func.isRequired,
    projectSlug: PropTypes.string.isRequired,
    versionSlug: PropTypes.string.isRequired,
    locales: PropTypes.arrayOf(LocaleType).isRequired,
    projectVersions: PropTypes.arrayOf(ProjectType).isRequired,
    startMergeProcess: PropTypes.func.isRequired,
    notification: PropTypes.object,
    triggered: PropTypes.bool.isRequired,
    fetchingProject: PropTypes.bool.isRequired,
    fetchingLocale: PropTypes.bool.isRequired,
    onCancelTMMerge: PropTypes.func.isRequired,
    // Not required - set to undefined when merge not in progress
    processStatus: processStatusType,
    queryStatus: PropTypes.string,
    queryTMMergeProgress: PropTypes.func.isRequired,
    mergeProcessFinished: PropTypes.func.isRequired
  }
  defaultState = {
    matchPercentage: 100,
    differentDocId: false,
    differentContext: false,
    fromImportedTM: false,
    ignoreDifferentDocId: false,
    ignoreDifferentContext: false,
    importedTMCopyAsTranslated: false,
    fromAllProjects: false,
    selectedLanguage: undefined,
    selectedVersions: [],
    projectSearchTerm: this.props.projectSlug,
    hasMerged: false
  }
  constructor (props) {
    super(props)
    this.state = this.defaultState
    /* Chose 1 second as an arbitrary period between searches.
     * leading and trailing options specify we want to search to after the user
     * stops typing. */
    this.throttleHandleSearch = throttle(props.fetchProjectPage, 1000,
      { 'leading': false })
  }
  componentDidMount () {
    this.props.fetchVersionLocales(
      this.props.projectSlug, this.props.versionSlug)
    this.props.fetchProjectPage(this.state.projectSearchTerm)
  }
  componentWillReceiveProps (nextProps) {
    const { locales, showTMMergeModal } = nextProps
    // Fetch locales again when modal is closed then re-opened
    // Reset the state when the modal is closed
    if (showTMMergeModal !== this.props.showTMMergeModal) {
      if (showTMMergeModal) {
        nextProps.fetchVersionLocales(
        nextProps.projectSlug, nextProps.versionSlug)
      } else {
        // If a merge has run, reload the page to display the merge results
        if (this.state.hasMerged) {
          window.location.reload()
        // Else reset the state to default on modal close
        } else {
          this.setState(this.defaultState)
        }
      }
    }
    if (!this.state.selectedLanguage) {
      this.setState((prevState, props) => ({
        selectedLanguage: locales.length === 0 ? undefined : locales[0]
      }))
    }
    const currentProcessStatus = this.props.processStatus
    if (!isProcessEnded(currentProcessStatus) &&
      isProcessEnded(nextProps.processStatus)) {
      this.setState({
        hasMerged: true
      })
      // process just finished, we want to re-display the merge option form.
      // but we want to delay it a bit so that the user can see the progress
      // bar animation finishes
      setTimeout(this.props.mergeProcessFinished, 1000)
    }
    // Filter out the source project and version if present in search results
    // TODO: perform this filtering on the server side when retrieving projects
    nextProps.projectVersions.map((project) => {
      project.versions = project.versions.filter((version) => {
        return project.id !== nextProps.projectSlug ||
          version.id !== nextProps.versionSlug
      })
    })
  }
  queryTMMergeProgress = () => {
    this.props.queryTMMergeProgress(this.props.processStatus.url)
  }
  cancelTMMerge = () => {
    this.props.onCancelTMMerge(this.props.processStatus.cancelUrl)
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
    this.throttleHandleSearch(textEntered)
    this.setState({
      projectSearchTerm: textEntered
    })
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
  onIgnoreDifferentDocIdChange = () => {
    this.setState(prevState => ({
      ignoreDifferentDocId: !prevState.ignoreDifferentDocId
    }))
  }
  onIgnoreDifferentContextChange = () => {
    this.setState(prevState => ({
      ignoreDifferentContext: !prevState.ignoreDifferentContext
    }))
  }
  onImportedTMCopyRuleChange = () => {
    this.setState(prevSTate => ({
      importedTMCopyAsTranslated: !prevSTate.importedTMCopyAsTranslated
    }))
  }
  // internal TM can come from all projects
  onFromAllProjectsChange = () => {
    this.setState(prevState => ({
      fromAllProjects: !prevState.fromAllProjects
    }))
  }
  submitForm = () => {
    this.props.startMergeProcess(this.props.projectSlug,
      this.props.versionSlug, this.state)
  }
  render () {
    const {
      showTMMergeModal,
      toggleTMMergeModal,
      projectSlug,
      versionSlug,
      projectVersions,
      locales,
      notification,
      triggered,
      fetchingProject,
      fetchingLocale,
      processStatus
    } = this.props
    const modalBody = processStatus
      ? (
      <CancellableProgressBar onCancelOperation={this.cancelTMMerge}
        processStatus={processStatus} buttonLabel="Cancel TM Merge"
        queryProgress={this.queryTMMergeProgress} />
      )
      : locales.length === 0
      ? <p>This version has no enabled languages. You must enable at least one
        language to use TM merge. <br />
        <a href={getVersionLanguageSettingsUrl(projectSlug, versionSlug)}>
        Language Settings</a></p>
      : (
        <MergeOptions {...{projectSlug, versionSlug, locales, projectVersions,
          fetchingProject, fetchingLocale}}
          mergeOptions={this.state}
          onPercentSelection={this.onPercentSelection}
          onDocIdCheckboxChange={this.onDocIdCheckboxChange}
          onContextCheckboxChange={this.onContextCheckboxChange}
          onImportedCheckboxChange={this.onImportedCheckboxChange}
          onIgnoreDifferentDocIdChange={this.onIgnoreDifferentDocIdChange}
          onIgnoreDifferentContextChange={this.onIgnoreDifferentContextChange}
          onImportedTMCopyRuleChange={this.onImportedTMCopyRuleChange}
          onFromAllProjectsChange={this.onFromAllProjectsChange}
          onAllVersionCheckboxChange={this.onAllVersionCheckboxChange}
          onVersionCheckboxChange={this.onVersionCheckboxChange}
          onLanguageSelection={this.onLanguageSelection}
          onProjectSearchChange={this.onProjectSearchChange}
          flushProjectSearch={this.flushProjectSearch}
          onDragMoveEnd={this.onDragMoveEnd}
          removeProjectVersion={this.removeProjectVersion}
        />
        )
    const hasTMSource = this.state.fromAllProjects ||
      this.state.fromImportedTM || this.state.selectedVersions.length > 0
    const modalFooter = processStatus
    ? undefined
    : (
      <span>
        <Row>
          <Button bsStyle='link' className='btn-left link-danger'
            onClick={toggleTMMergeModal}>
            Close
          </Button>
          <Button bsStyle='primary' onClick={this.submitForm}
            disabled={(triggered || !hasTMSource)}>
            Merge translations
          </Button>
        </Row>
      </span>
    )
    return (
      <Modal id="TM-merge-modal" show={showTMMergeModal}
        onHide={toggleTMMergeModal} keyboard backdrop>
        <Modal.Header>
          <Modal.Title>Version TM Merge</Modal.Title>
          <p className="text-danger modal-danger">
            {notification && notification.message}</p>
        </Modal.Header>
        <Modal.Body>{modalBody}</Modal.Body>
        <Modal.Footer>{modalFooter}</Modal.Footer>
      </Modal>
    )
  }
}

const mapStateToProps = (state) => {
  const {
    projectVersion: {
      locales,
      notification,
      fetchingProject,
      fetchingLocale,
      TMMerge: {
        show,
        triggered,
        projectVersions,
        processStatus,
        queryStatus
      }
    }
  } = state
  return {
    showTMMergeModal: show,
    triggered,
    locales,
    projectVersions,
    notification,
    fetchingProject,
    fetchingLocale,
    processStatus,
    queryStatus
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    fetchVersionLocales: (project, version) => {
      dispatch(fetchVersionLocales(project, version))
    },
    fetchProjectPage: (project) => {
      dispatch(fetchProjectPage(project))
    },
    toggleTMMergeModal: () => {
      dispatch(toggleTMMergeModal())
    },
    startMergeProcess: (projectSlug, versionSlug, mergeOptions) => {
      dispatch(mergeVersionFromTM(projectSlug, versionSlug, mergeOptions))
    },
    queryTMMergeProgress: (url) => {
      dispatch(queryTMMergeProgress(url))
    },
    onCancelTMMerge: (url) => {
      dispatch(cancelTMMergeRequest(url))
    },
    mergeProcessFinished: () => {
      dispatch(currentTMMergeProcessFinished())
    }
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(TMMergeModal)
