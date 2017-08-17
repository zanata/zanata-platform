import React, {Component} from 'react'
import PropTypes from 'prop-types'
import {Panel, InputGroup, Col, FormControl,
  ToggleButtonGroup, ToggleButton} from 'react-bootstrap'
import {
  Icon, LoaderText, DraggableVersionPanels
} from '../../components'
import ProjectVersionPanels from './ProjectVersionPanels'

import {
  ProjectType, FromProjectVersionType
} from '../../utils/prop-types-util.js'

const DO_NOT_RENDER = undefined
const ALL = 'ALL'
const SAME = 'SAME'
const OTHER = 'OTHER'

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
    removeProjectVersion: PropTypes.func.isRequired
  }
  defaultState = {
    fromProjectSelection: SAME
  }
  constructor (props) {
    super(props)
    this.state = this.defaultState
  }
  onFromProjectSelectionChange = (value) => {
    this.setState((prevState, props) => ({
      fromProjectSelection: value
    }))
    if (value === ALL) {
      this.props.onFromAllProjectsChange()
    }
  }
  render () {
    const {
      projectVersions,
      fetchingProject,
      mergeOptions,
      onProjectSearchChange,
      flushProjectSearch,
      onVersionCheckboxChange,
      onAllVersionCheckboxChange,
      onDragMoveEnd,
      removeProjectVersion
    } = this.props
    const noResults = (projectVersions.length === 0) ? 'No results' : ''
    const fromVersionsPanel = this.state.fromProjectSelection !== OTHER
      ? DO_NOT_RENDER
      : (
      <div>
        <Col xs={12}>
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
      </div>
      )
    return (
      <div>
        <Col xs={12} className='vmerge-boxes'>
          <Panel>
            <Col xs={12}>
              <div className='vmerge-title'>
                <span>From</span>
                <span className="text-info"> Project Source</span>
              </div>
            </Col>
            <Col xs={12} className='vmerge-searchbox'>
              Search TM from
              <ToggleButtonGroup
                type="radio" name="button"
                value={this.state.fromProjectSelection}
                onChange={this.onFromProjectSelectionChange}>
                <ToggleButton value={SAME}> this project
                </ToggleButton>
                <ToggleButton value={ALL}> all projects
                </ToggleButton>
                <ToggleButton value={OTHER}> other projects that I choose
                </ToggleButton>
              </ToggleButtonGroup>
            </Col>
            {fromVersionsPanel}
          </Panel>
        </Col>
      </div>
    )
  }
}

export default TMMergeProjectSources
