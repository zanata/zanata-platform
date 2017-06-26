import React, {Component} from 'react'
import PropTypes from 'prop-types'
import {
  Panel, Tooltip, Checkbox, ListGroup, ListGroupItem, OverlayTrigger, PanelGroup
} from 'react-bootstrap'
import Icon from './Icon'
import {ProjectType, FromProjectVersionType} from '../utils/prop-types-util.js'
import {every} from 'lodash'

const tooltipReadOnly = <Tooltip id='tooltipreadonly'>Read only</Tooltip>

const LockIcon = (props) => {
  return props.status === 'READONLY'
    ? (
    <OverlayTrigger placement='top' overlay={tooltipReadOnly}>
      <Icon name='locked' className='s0 icon-locked' />
    </OverlayTrigger>
  )
    : <span />
}
LockIcon.propTypes = {
  status: PropTypes.string.isRequired
}

/**
 * Root component for the Version TM Merge project version panels
 */
class ProjectVersionPanels extends Component {
  static propTypes = {
    projectVersions: PropTypes.arrayOf(ProjectType).isRequired,
    selectedVersions: PropTypes.arrayOf(FromProjectVersionType).isRequired,
    /* params: version, projectSlug */
    onVersionCheckboxChange: PropTypes.func.isRequired,
    /* params: project object */
    onAllVersionCheckboxChange: PropTypes.func.isRequired
  }
  render () {
    if (this.props.projectVersions.length === 0) {
      return <div></div>
    }
    const panels = this.props.projectVersions.map((project, index) => {
      return (
        <Panel key={index} eventKey={index} header={
          <h3>
            <SelectAllVersionsCheckbox
              project={project}
              onAllVersionCheckboxChange={this.props.onAllVersionCheckboxChange}
              selectedVersions={this.props.selectedVersions} />
          </h3>}>
          <ListGroup fill>
            {project.versions.map((version, index) => {
              return (
                <ListGroupItem className='v' key={index}>
                  <VersionMenuCheckbox version={version}
                    onVersionCheckboxChange={this.props.onVersionCheckboxChange}
                    selectedVersions={this.props.selectedVersions}
                    projectSlug={project.id} />
                </ListGroupItem>
              )
            })}
          </ListGroup>
        </Panel>
      )
    })
    return <PanelGroup defaultActiveKey={0} accordion>{panels}</PanelGroup>
  }
}

/**
 * Sub Component of project version panels
 * Handles behavior of select all versions checkbox
 */
class SelectAllVersionsCheckbox extends Component {
  static propTypes = {
    project: ProjectType.isRequired,
    selectedVersions: PropTypes.arrayOf(FromProjectVersionType).isRequired,
    onAllVersionCheckboxChange: PropTypes.func.isRequired
  }
  onAllVersionCheckboxChange = () => {
    this.props.onAllVersionCheckboxChange(this.props.project)
  }
  render () {
    const {
      project,
      selectedVersions
    } = this.props
    const flattenedVersionArray = selectedVersions.map((project) => {
      return project.version
    })
    // Check if all project versions have been selected
    const allVersionsChecked = every(project.versions,
        version => flattenedVersionArray.includes(version))

    return (
      <Checkbox onChange={this.onAllVersionCheckboxChange}
        checked={allVersionsChecked}>
        {project.title} <LockIcon status={project.status} />
      </Checkbox>
    )
  }
}

/**
 * Sub Component of project version panels
 * Handles behavior of select version checkbox
 */
class VersionMenuCheckbox extends Component {
  static propTypes = {
    version: PropTypes.shape({
      id: PropTypes.string.isRequired,
      status: PropTypes.oneOf(['READONLY', 'ACTIVE'])
    }).isRequired,
    onVersionCheckboxChange: PropTypes.func.isRequired,
    selectedVersions: PropTypes.arrayOf(FromProjectVersionType).isRequired,
    projectSlug: PropTypes.string.isRequired
  }
  onVersionCheckboxChange = () => {
    this.props.onVersionCheckboxChange(
      this.props.version, this.props.projectSlug)
  }
  render () {
    const {
      version,
      selectedVersions
    } = this.props
    const versionChecked = selectedVersions.map((project) => {
      return project.version
    }).includes(version)
    return (
      <Checkbox onChange={this.onVersionCheckboxChange}
        checked={versionChecked}>
        {version.id} <LockIcon status={version.status} />
      </Checkbox>
    )
  }
}

export default ProjectVersionPanels
