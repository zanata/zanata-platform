import React, {PropTypes, Component} from 'react'
import {
  Panel, Tooltip, Checkbox, ListGroup, ListGroupItem, OverlayTrigger, PanelGroup
}
  from 'react-bootstrap'
import Icon from './Icon'

const tooltipReadOnly = (
  <Tooltip id='tooltipreadonly'>Read only
  </Tooltip>
)

/**
 * Root component for project version panels
 */
class ProjectVersionPanels extends Component {
  static propTypes = {
    projectVersions: PropTypes.arrayOf(PropTypes.object),
    fromProjectVersion: PropTypes.arrayOf(PropTypes.object),
    onVersionCheckboxChange: PropTypes.func.isRequired,
    onAllVersionCheckboxChange: PropTypes.func.isRequired
  }
  render () {
    if (this.props.projectVersions.length <= 0) {
      return <div></div>
    }
    let panels = this.props.projectVersions.map((project, index) => {
      return (
        <Panel header={<h3><SelectAllVersionsCheckbox
          project={project} onClick={this.props.onAllVersionCheckboxChange}
          fromProjectVersion={this.props.fromProjectVersion} />
        </h3>} key={index} eventKey={index}>
          <ListGroup fill>
            {project.versions.map((version, index) => {
              return (
                <ListGroupItem className='v' key={index}>
                  <VersionMenuCheckbox version={version}
                    onClick={this.props.onVersionCheckboxChange}
                    fromProjectVersion={this.props.fromProjectVersion}
                    projectSlug={project.id} />
                </ListGroupItem>
              )
            })}
          </ListGroup>
        </Panel>
      )
    })
    return <PanelGroup defaultActiveKey='1' accordion>{panels}</PanelGroup>
  }
}

/**
 * Sub Component of project version panels
 * Handles behavior of select all versions checkbox
 */
class SelectAllVersionsCheckbox extends Component {
  static propTypes = {
    project: PropTypes.object,
    onClick: PropTypes.func.isRequired,
    fromProjectVersion: PropTypes.arrayOf(PropTypes.object)
  }
  onClick = () => {
    this.props.onClick(this.props.project)
  }
  render () {
    const {
      project,
      fromProjectVersion
    } = this.props
    const flattenedVersionArray = fromProjectVersion.map((project) => {
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
    version: PropTypes.object.isRequired,
    onClick: PropTypes.func.isRequired,
    fromProjectVersion: PropTypes.arrayOf(PropTypes.object),
    projectSlug: PropTypes.string.isRequired
  }
  onClick = () => {
    this.props.onClick(this.props.version, this.props.projectSlug)
  }
  render () {
    const {
      version,
      fromProjectVersion
    } = this.props
    const flattenedVersionArray = fromProjectVersion.map((project) => {
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
