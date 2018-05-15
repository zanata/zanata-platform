// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import Radio from 'antd/lib/radio'
import 'antd/lib/radio/style/css'
import Card from 'antd/lib/card'
import 'antd/lib/card/style/css'
import Row from 'antd/lib/row'
import 'antd/lib/row/style/css'
import Col from 'antd/lib/col'
import 'antd/lib/col/style/css'
import Tooltip from 'antd/lib/tooltip'
import 'antd/lib/tooltip/style/css'
import Input from 'antd/lib/input'
import 'antd/lib/input/style/css'
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
        <Col span={11}>
          <span className='mb1 b'>
            Select source project versions to merge
          </span>
          <div>
            <LoaderText loading={fetchingProject}
              loadingText={'Fetching Projects'} />
            <span className="u-textMuted">{noResults}</span>
          </div>
        </Col>
        <Row gutter={16}>
          <Col span={12}>
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
            <Toggle icons={false} defaultChecked
              onChange={this.toggleChange} />
            <span>From </span>
            <span className="panel-name">Project Source</span>
            <Tooltip placement='right'
              title={fromProjectSourceTooltip}>
              <Button className="btn-link tooltip-btn" aria-label="button">
                <Icon name="info" className="s0" />
              </Button>
            </Tooltip>
          </Col>
        </Row>
        <Row>
          <Col span={24}>
            <span className="mr2">Search TM from</span>
          </Col>
        </Row>
        <Row>
          <Col span={8}>
            <Radio name="fromProjectSelection" inline disabled={disabled}
              checked={this.state.fromProjectSelection === SAME}
              onChange={this.onFromProjectSelectionChange(SAME)}> this project
            </Radio>
          </Col>
          <Col span={8}>
            <Radio name="fromProjectSelection" inline disabled={disabled}
              checked={this.state.fromProjectSelection === ALL}
              onChange={this.onFromProjectSelectionChange(ALL)}> all projects
            </Radio>
          </Col>
          <Col span={8}>
            <Radio name="fromProjectSelection" inline disabled={disabled}
              checked={this.state.fromProjectSelection === OTHER}
              onChange={this.onFromProjectSelectionChange(OTHER)}>
              some projects
            </Radio>
          </Col>
          {fromVersionsPanel}
          <TMMergeProjectTMOptions {...this.props} disabled={disabled}
            disableDifferentProjectOption={disableDiffProjectOption} />
        </Row>
        <Row className='mt3 mb3'>
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
