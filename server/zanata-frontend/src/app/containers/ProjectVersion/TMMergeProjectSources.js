// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
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
import Switch from 'antd/lib/switch'
import 'antd/lib/switch/style/css'
import Input from 'antd/lib/input'
import 'antd/lib/input/style/css'
import {
  Icon, LoaderText, DraggableVersionPanels
} from '../../components'
import ProjectVersionPanels from './ProjectVersionPanels'
import TMMergeProjectTMOptions from './TMMergeProjectTMOptions'

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
    visibleProjectsWithVersions: PropTypes.arrayOf(ProjectType).isRequired,
    fetchingProject: PropTypes.bool.isRequired,
    mergeOptions: PropTypes.shape({
      selectedVersions: PropTypes.arrayOf(FromProjectVersionType),
      projectSearchTerm: PropTypes.string
    }).isRequired,
    onFromAllProjectsChange: PropTypes.func.isRequired,
    onProjectSearchChange: PropTypes.func.isRequired,
    onVersionCheckboxChange: PropTypes.func.isRequired,
    onAllVersionCheckboxChange: PropTypes.func.isRequired,
    onAllProjectsCheckboxChange: PropTypes.func.isRequired,
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
  projectSearchTermChanged = value => {
    this.props.onProjectSearchChange(value)
  }
  toggleChange = (e) => {
    const checked = e.target.checked
    this.setState((prevState) => ({
      enabled: checked
    }))
  }
  render () {
    const {
      visibleProjectsWithVersions,
      fetchingProject,
      mergeOptions,
      onVersionCheckboxChange,
      onAllVersionCheckboxChange,
      onAllProjectsCheckboxChange,
      onDragMoveEnd,
      removeProjectVersion
    } = this.props
    const disabled = !this.state.enabled
    const noResults = (visibleProjectsWithVersions.length === 0)
      ? 'No results' : ''
    const fromVersionsPanel = this.state.fromProjectSelection === ALL ||
    disabled
      ? DO_NOT_RENDER
      : (
      <span>
        <Search
          placeholder='input search text'
          onSearch={this.projectSearchTermChanged}
          enterButton />
        <Row>
          <Col span={11} className='mr2'>
            <span>
              Select source project versions to merge
            </span>
            <LoaderText loading={fetchingProject}
              loadingText={'Fetching Projects'} />
            <span className='txt-muted ml1'>{noResults}</span>
            {visibleProjectsWithVersions.length > 0 &&
              <ProjectVersionPanels
                visibleProjectsWithVersions={visibleProjectsWithVersions}
                selectedVersions={mergeOptions.selectedVersions}
                onVersionCheckboxChange={onVersionCheckboxChange}
                onAllVersionCheckboxChange={onAllVersionCheckboxChange}
                onAllProjectsCheckboxChange={onAllProjectsCheckboxChange} />
            }
          </Col>
          <Col span={11}>
            {visibleProjectsWithVersions.length > 0 &&
              <DraggableVersionPanels
                selectedVersions={mergeOptions.selectedVersions}
                onDraggableMoveEnd={onDragMoveEnd}
                removeVersion={removeProjectVersion} />
            }
          </Col>
        </Row>
      </span>
      )
    const disableDiffProjectOption = this.state.fromProjectSelection === SAME
    return (
      <span>
        <Row>
          <Col span={24} className='v-mid'>
            <Switch defaultChecked
              onChange={this.toggleChange} />
            <span className='f4 ml2 v-mid'>From </span>
            <span className='f4 b mr2 v-mid'>Project Source</span>
            <Tooltip placement='right'
              title={fromProjectSourceTooltip}>
              <a className='btn-link pa0' aria-label='button'>
                <Icon name='info' className='s0 v-mid' />
              </a>
            </Tooltip>
          </Col>
        </Row>
        <Row className='mt2 mb2'>
          <Col span={6}>
            <span className='mr2'>Search TM from</span>
          </Col>
          <Col span={6}>
            <Radio name='fromProjectSelection' inline disabled={disabled}
              checked={this.state.fromProjectSelection === SAME}
              onChange={this.onFromProjectSelectionChange(SAME)}> this project
            </Radio>
          </Col>
          <Col span={6}>
            <Radio name='fromProjectSelection' inline disabled={disabled}
              checked={this.state.fromProjectSelection === ALL}
              onChange={this.onFromProjectSelectionChange(ALL)}> all projects
            </Radio>
          </Col>
          <Col span={6}>
            <Radio name='fromProjectSelection' inline disabled={disabled}
              checked={this.state.fromProjectSelection === OTHER}
              onChange={this.onFromProjectSelectionChange(OTHER)}>
              some projects
            </Radio>
          </Col>
          {fromVersionsPanel}
          <TMMergeProjectTMOptions {...this.props} disabled={disabled}
            disableDifferentProjectOption={disableDiffProjectOption} />
        </Row>
        <Row className='mt4 mb4'>
          <Col span={24}>
            <Card>
              <p>Translations which satisfy all conditions will copy as
                <span className='b txt-success'> translated</span>.
              </p>
            </Card>
          </Col>
        </Row>
      </span>
    )
  }
}

export default TMMergeProjectSources
