import React, {Component} from 'react'
import PropTypes from 'prop-types'
import {
  Panel, Tooltip, Checkbox, ListGroup, ListGroupItem, OverlayTrigger, PanelGroup
} from 'react-bootstrap'
import Icon from './Icon'
import {ProjectType, FromProjectVersionType} from '../utils/prop-types-util.js'

const tooltipReadOnly = (<Tooltip id='tooltipreadonly'>Read only</Tooltip>)

/**
 * Root component for project version panels
 */
class ProjectVersionPanels extends Component {
  static propTypes = {
    projectVersions: PropTypes.arrayOf(ProjectType).isRequired,
    fromProjectVersions: PropTypes.arrayOf(FromProjectVersionType).isRequired,
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
              onClick={this.props.onAllVersionCheckboxChange}
              fromProjectVersions={this.props.fromProjectVersions} />
          </h3>}>
          <ListGroup fill>
            {project.versions.map((version, index) => {
              return (
                <ListGroupItem className='v' key={index}>
                  <VersionMenuCheckbox version={version}
                    onClick={this.props.onVersionCheckboxChange}
                    fromProjectVersions={this.props.fromProjectVersions}
                    projectSlug={project.id} />
                </ListGroupItem>
              )
            })}
          </ListGroup>
        </Panel>
      )
    })
    return <PanelGroup defaultActiveKey='0' accordion>{panels}</PanelGroup>
  }
}

/**
 * Sub Component of project version panels
 * Handles behavior of select all versions checkbox
 */
class SelectAllVersionsCheckbox extends Component {
  static propTypes = {
    project: ProjectType.isRequired,
    fromProjectVersions: PropTypes.arrayOf(FromProjectVersionType).isRequired,
    onClick: PropTypes.func.isRequired
  }
  onClick = () => {
    this.props.onClick(this.props.project)
  }
  render () {
    const {
      project,
      fromProjectVersions
    } = this.props
    const flattenedVersionArray = fromProjectVersions.map((project) => {
      return project.version
    })
    // Check if all project versions have been selected
    const containsVersionArray = (projectVersionList, selectedVersionList) => {
      let allChecked = true
      projectVersionList.map((version) => {
        if (!selectedVersionList.includes(version)) {
          allChecked = false
        }
      })
      return allChecked
    }
    const allVersionsChecked =
        containsVersionArray(project.versions, flattenedVersionArray)
    const projectLockIcon = project.status === 'READONLY'
        ? <OverlayTrigger placement='top' overlay={tooltipReadOnly}>
          <Icon name='locked' className='s0 icon-locked' />
        </OverlayTrigger>
        : ''
    return (
      <Checkbox onChange={this.onClick}
        checked={allVersionsChecked}>
        {project.title}{" "}{projectLockIcon}
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
      status: PropTypes.string.isRequired
    }).isRequired,
    onClick: PropTypes.func.isRequired,
    fromProjectVersions: PropTypes.arrayOf(FromProjectVersionType).isRequired,
    projectSlug: PropTypes.string.isRequired
  }
  onClick = () => {
    this.props.onClick(this.props.version, this.props.projectSlug)
  }
  render () {
    const {
      version,
      fromProjectVersions
    } = this.props
    const flattenedVersionArray = fromProjectVersions.map((project) => {
      return project.version
    })
    const versionChecked = flattenedVersionArray.includes(version)
    const versionLockIcon = version.status === 'READONLY'
        ? <OverlayTrigger placement='top' overlay={tooltipReadOnly}>
          <Icon name='locked' className='s0 icon-locked' />
        </OverlayTrigger>
        : ''
    return (
      <Checkbox onChange={this.onClick} checked={versionChecked}>
        {version.id}{" "}{versionLockIcon}
      </Checkbox>
    )
  }
}

export default ProjectVersionPanels
