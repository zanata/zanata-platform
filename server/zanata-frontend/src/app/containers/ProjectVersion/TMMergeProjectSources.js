import React, {Component} from 'react'
import PropTypes from 'prop-types'
import {InputGroup, Col, FormControl, OverlayTrigger, Radio, Well,
  Tooltip, Panel} from 'react-bootstrap'
import {
  Icon, LoaderText, DraggableVersionPanels
} from '../../components'
import ProjectVersionPanels from './ProjectVersionPanels'
import TMMergeProjectTMOptions from './TMMergeProjectTMOptions'
import Toggle from 'react-toggle'

import {
  ProjectType, FromProjectVersionType
} from '../../utils/prop-types-util.js'

const DO_NOT_RENDER = undefined
const ALL = 'ALL'
const SAME = 'SAME'
const OTHER = 'OTHER'

const fromProjectSourceTooltip = (
  <Tooltip id='from-project-source' title='From project source'>
    Exact text matches from projects are used before exact matches in imported
    TM. Fuzzy text matches from projects are used before fuzzy matches in
    imported TM.
  </Tooltip>)

/*
 * Component to display TM merge from project sources
 */
class TMMergeProjectSources extends Component {
  static propTypes = {
    projectVersions: PropTypes.arrayOf(ProjectType).isRequired,
    fetchingProject: PropTypes.bool.isRequired,
    mergeOptions: PropTypes.shape({
      selectedVersions: PropTypes.arrayOf(FromProjectVersionType),
      projectSearchTerm: PropTypes.string
    }).isRequired,
    onFromAllProjectsChange: PropTypes.func.isRequired,
    onProjectSearchChange: PropTypes.func.isRequired,
    flushProjectSearch: PropTypes.func.isRequired,
    onVersionCheckboxChange: PropTypes.func.isRequired,
    onAllVersionCheckboxChange: PropTypes.func.isRequired,
    onDragMoveEnd: PropTypes.func.isRequired,
    removeProjectVersion: PropTypes.func.isRequired,
    thisProjectSlug: PropTypes.string.isRequired
  }
  defaultState = {
    fromProjectSelection: SAME,
    enabled: true
  }
  constructor (props) {
    super(props)
    this.state = this.defaultState
  }
  onFromProjectSelectionChange = (value) => () => {
    this.setState((prevState, props) => ({
      fromProjectSelection: value
    }))
    if (value === ALL) {
      this.props.onFromAllProjectsChange()
    }
    if (value === SAME) {
      // if user select this project, we should search for current project
      this.props.onProjectSearchChange(this.props.thisProjectSlug)
    }
  }
  projectSearchTermChanged = e => {
    const text = e.target.value
    this.props.onProjectSearchChange(text)
  }
  toggleChange = (e) => {
    const checked = e.target.checked
    this.setState((prevState) => ({
      enabled: checked
    }))
  }
  render () {
    const {
      projectVersions,
      fetchingProject,
      mergeOptions,
      flushProjectSearch,
      onVersionCheckboxChange,
      onAllVersionCheckboxChange,
      onDragMoveEnd,
      removeProjectVersion
    } = this.props
    const disabled = !this.state.enabled
    const noResults = (projectVersions.length === 0) ? 'No results' : ''
    const fromVersionsPanel = this.state.fromProjectSelection === ALL ||
    disabled
      ? DO_NOT_RENDER
      : (
      <span className="search-input">
        <Col xs={12}>
          <InputGroup>
            <InputGroup.Addon>
              <Icon name='search' className='s0' title='search' />
            </InputGroup.Addon>
            <FormControl type='text'
              value={mergeOptions.projectSearchTerm}
              className='versionMergeSearch-input'
              onChange={this.projectSearchTermChanged}
              onKeyDown={flushProjectSearch}
            />
          </InputGroup>
        </Col>
        <Col xs={6}>
          <span className='versionMergeTitle-adjusted VersionMergeTitle'>
          Select source project versions to merge
          </span>
          <div>
            <LoaderText loading={fetchingProject}
              loadingText={'Fetching Projects'} />
            <span className="u-textMuted">{noResults}</span>
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
      </span>
      )
    const disableDiffProjectOption = this.state.fromProjectSelection === SAME
    return (
      <Panel>
        <Col xs={12}>
          <div className='VersionMergeTitle versionMergeTitle-flex'>
            <span>
              <Toggle icons={false} defaultChecked
                onChange={this.toggleChange} />
            </span>
            <span>From </span>
            <span className="panel-name">Project Source</span>
            <OverlayTrigger placement='right'
              overlay={fromProjectSourceTooltip}>
              <a className="btn-link tooltip-btn" role="button">
                <Icon name="info" className="s0"
                  parentClassName="iconInfoVersionMerge" />
              </a>
            </OverlayTrigger>
          </div>
        </Col>
        <Col xs={12} className='versionMergeSearch'>
          <span>Search TM from</span>
          <Radio name="fromProjectSelection" inline disabled={disabled}
            checked={this.state.fromProjectSelection === SAME}
            onChange={this.onFromProjectSelectionChange(SAME)}> this project
          </Radio>
          <Radio name="fromProjectSelection" inline disabled={disabled}
            checked={this.state.fromProjectSelection === ALL}
            onChange={this.onFromProjectSelectionChange(ALL)}> all projects
          </Radio>
          <Radio name="fromProjectSelection" inline disabled={disabled}
            checked={this.state.fromProjectSelection === OTHER}
            onChange={this.onFromProjectSelectionChange(OTHER)}> some projects
          </Radio>
        </Col>
        {fromVersionsPanel}
        <TMMergeProjectTMOptions {...this.props} disabled={disabled}
          disableDifferentProjectOption={disableDiffProjectOption}
        />
        <Col xs={12}>
          <Well>
            <p>Translations which satisfy all conditions will copy as
              <span className="u-textBold u-textSuccess"> translated</span>.
            </p>
          </Well>
        </Col>
      </Panel>
    )
  }
}

export default TMMergeProjectSources
