import React, {Component} from 'react'
import PropTypes from 'prop-types'
import {
  Panel, ListGroup, ListGroupItem, PanelGroup
} from 'react-bootstrap'
import {LockIcon, Icon, TriCheckbox} from '../../components'
import {ProjectType, FromProjectVersionType,
  versionDtoPropType} from '../../utils/prop-types-util'

/**
 * Panels for selecting and prioritising of project-versions
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
  /*
    selectedVersions is an array of shape:
    {
      projectSlug,
      version: {
        id,
        status
      }
    }
   */
  selectedVersionsOfProject = (selectedVersions, project) => {
    return selectedVersions
      .filter(p => p.projectSlug === project.id)
      .map(p => p.version)
  }
  render () {
    if (this.props.projectVersions.length === 0) {
      return <PanelGroup />
    }
    const panels = this.props.projectVersions.map((project, index) => {
      const selectedVersionsInProject =
        this.selectedVersionsOfProject(this.props.selectedVersions, project)
      return (
        <SelectableProjectPanel key={index} eventKey={index}
          selectedVersionsInProject={selectedVersionsInProject}
          project={project}
          onAllVersionCheckboxChange={this.props.onAllVersionCheckboxChange}
          onVersionCheckboxChange={this.props.onVersionCheckboxChange}
        />
      )
    })
    return <PanelGroup defaultActiveKey={0} accordion>{panels}</PanelGroup>
  }
}

// util function to check if a version is in a list of versions by id comparison
const isVersionInList =
  (versions, version) => !!versions.find(v => v.id === version.id)
/**
 * Sub Component of a single project with versions.
 * Handles behavior of display or selecting versions of this project.
 */
const SelectableProjectPanel = ({
  project,
  selectedVersionsInProject,
  onAllVersionCheckboxChange,
  onVersionCheckboxChange }) => {
  return (
    <Panel header={
      <h3>
        <SelectAllVersionsCheckbox
          project={project}
          onAllVersionCheckboxChange={onAllVersionCheckboxChange}
          selectedVersionsInProject={selectedVersionsInProject} />
      </h3>}>
      <ListGroup fill>
        {project.versions.map((version, index) => {
          const checked = isVersionInList(selectedVersionsInProject, version)
          return (
            <ListGroupItem className='v' key={index}>
              <VersionMenuCheckbox version={version}
                onVersionCheckboxChange={onVersionCheckboxChange}
                checked={checked}
                projectSlug={project.id} />
            </ListGroupItem>
          )
        })}
      </ListGroup>
    </Panel>
  )
}
SelectableProjectPanel.propTypes = {
  project: ProjectType.isRequired,
  /* params: version, projectSlug */
  onVersionCheckboxChange: PropTypes.func.isRequired,
  /* params: project object */
  onAllVersionCheckboxChange: PropTypes.func.isRequired,
  selectedVersionsInProject: PropTypes.arrayOf(versionDtoPropType).isRequired
}

/**
 * Sub Component of project version panels
 * Handles behavior of select all versions checkbox
 */
class SelectAllVersionsCheckbox extends Component {
  static propTypes = {
    project: ProjectType.isRequired,
    selectedVersionsInProject: PropTypes.arrayOf(versionDtoPropType).isRequired,
    onAllVersionCheckboxChange: PropTypes.func.isRequired
  }
  onAllVersionCheckboxChange = () => {
    this.props.onAllVersionCheckboxChange(this.props.project)
  }
  render () {
    const {project, selectedVersionsInProject} = this.props
    // Check if all project versions have been selected
    // since we are just comparing versions in one project, we can just check
    // the size
    const allVersionsChecked =
      project.versions.length === selectedVersionsInProject.length
    const someVersionsChecked =
      selectedVersionsInProject.length < project.versions.length &&
      selectedVersionsInProject.length > 0
    return (
      <div className='checkbox'>
        <label>
          <TriCheckbox
            onChange={this.onAllVersionCheckboxChange}
            checked={allVersionsChecked}
            indeterminate={someVersionsChecked} /> <Icon name='project'
              title='source project'
              className='s0 iconTMX' /> {project.title} <LockIcon
                status={project.status} />
        </label>
      </div>
    )
  }
}

/**
 * Sub Component of project version panels
 * Handles behavior of select version checkbox
 */
class VersionMenuCheckbox extends Component {
  static propTypes = {
    version: versionDtoPropType.isRequired,
    onVersionCheckboxChange: PropTypes.func.isRequired,
    projectSlug: PropTypes.string.isRequired,
    checked: PropTypes.bool.isRequired
  }
  onVersionCheckboxChange = () => {
    this.props.onVersionCheckboxChange(
      this.props.version, this.props.projectSlug)
  }
  render () {
    const {
      version,
      checked
    } = this.props
    return (
      <div className='checkbox'>
        <label>
          <TriCheckbox onChange={this.onVersionCheckboxChange}
            checked={checked} /> <Icon name='version' title='source version'
              className='s0 iconTMX' /> {version.id} <LockIcon
                status={version.status} />
        </label>
      </div>
    )
  }
}

export default ProjectVersionPanels
