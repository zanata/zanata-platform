import React, {Component} from 'react'
import PropTypes from 'prop-types'
import {InputGroup, Col, FormControl, OverlayTrigger, Radio,
  Tooltip, Button} from 'react-bootstrap'
import {
  Icon, LoaderText, DraggableVersionPanels
} from '../../components'
import ProjectVersionPanels from './ProjectVersionPanels'
import Toggle from 'react-toggle'

import {
  ProjectType, FromProjectVersionType
} from '../../utils/prop-types-util.js'

const DO_NOT_RENDER = undefined
const ALL = 'ALL'
const SAME = 'SAME'
const OTHER = 'OTHER'

const tooltip1 = (
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
    removeProjectVersion: PropTypes.func.isRequired
  }
  defaultState = {
    fromProjectSelection: SAME
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
      <span>
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
      </span>
      )
    return (
      <span>
        <Col xs={12}>
          <div className='vmerge-title vmerge-title-flex'>
            <span>
              <Toggle icons={false} defaultChecked />
            </span>
            <span>From </span>
            <span className="panel-name">Project Source</span>
            <OverlayTrigger placement='right' overlay={tooltip1}>
              <Button bsStyle="link" className="tooltip-btn">
                <Icon name="info" className="s0 info-icon" />
              </Button>
            </OverlayTrigger>
          </div>
        </Col>
        <Col xs={12} className='vmerge-searchbox'>
          Search TM from
          <Radio name="fromProjectSelection" inline
            checked={this.state.fromProjectSelection === SAME}
            onChange={this.onFromProjectSelectionChange(SAME)}> this project
          </Radio>
          <Radio name="fromProjectSelection" inline
            checked={this.state.fromProjectSelection === ALL}
            onChange={this.onFromProjectSelectionChange(ALL)}> all projects
          </Radio>
          <Radio name="fromProjectSelection" inline
            checked={this.state.fromProjectSelection === OTHER}
            onChange={this.onFromProjectSelectionChange(OTHER)}> some
            projects
          </Radio>
        </Col>
        {fromVersionsPanel}
      </span>
    )
  }
}

export default TMMergeProjectSources
