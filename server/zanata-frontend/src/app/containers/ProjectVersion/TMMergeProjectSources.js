// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import {Row, Col, Radio, Card, Tooltip, Input, Button} from 'antd'
import {
  Icon, LoaderText, DraggableVersionPanels
} from '../../components'
import ProjectVersionPanels from './ProjectVersionPanels'
import TMMergeProjectTMOptions from './TMMergeProjectTMOptions'
import Toggle from 'react-toggle'

import {
  ProjectType, FromProjectVersionType
} from '../../utils/prop-types-util'

const DO_NOT_RENDER = undefined
const ALL = 'ALL'
const SAME = 'SAME'
const OTHER = 'OTHER'

const fromProjectSourceTooltip = (
  <p id='from-project-source'>
    Exact text matches from projects are used before exact matches in imported
    TM. Fuzzy text matches from projects are used before fuzzy matches in
    imported TM.
  </p>)

const Search = Input.Search

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
        <Search
          placeholder="input search text"
          onSearch={this.projectSearchTermChange}
          enterButton />
        <Row>
          <Col span={12}>
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
              onAllVersionCheckboxChange={onAllVersionCheckboxChange} />
          </Col>
          <Col span={12}>
            <DraggableVersionPanels
              selectedVersions={mergeOptions.selectedVersions}
              onDraggableMoveEnd={onDragMoveEnd}
              removeVersion={removeProjectVersion} />
          </Col>
        </Row>
      </span>
      )
    const disableDiffProjectOption = this.state.fromProjectSelection === SAME
    return (
      <span>
        <Row>
          <Col span={24}>
            <div className='VersionMergeTitle versionMergeTitle-flex'>
              <span>
                <Toggle icons={false} defaultChecked
                  onChange={this.toggleChange} />
              </span>
              <span>From </span>
              <span className="panel-name">Project Source</span>
              <Tooltip placement='right'
                title={fromProjectSourceTooltip}>
                <Button className="btn-link tooltip-btn" aria-label="button">
                  <Icon name="info" className="s0"
                    parentClassName="iconInfoVersionMerge" />
                </Button>
              </Tooltip>
            </div>
          </Col>
          <Col span={24} className='versionMergeSearch'>
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
          <Col span={24}>
            <Card>
              <p>Translations which satisfy all conditions will copy as
                <span className="u-textBold u-textSuccess"> translated</span>.
              </p>
            </Card>
          </Col>
        </Row>
      </span>
    )
  }
}

export default TMMergeProjectSources
