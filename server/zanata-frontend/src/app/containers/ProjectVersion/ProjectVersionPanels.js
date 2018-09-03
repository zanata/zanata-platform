// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import Collapse from 'antd/lib/collapse'
import 'antd/lib/collapse/style/css'
import { sumBy, find } from 'lodash'

import {LockIcon, Icon, TriCheckbox} from '../../components'
import {ProjectType, FromProjectVersionType,
  versionDtoPropType} from '../../utils/prop-types-util'

const Panel = Collapse.Panel

/**
 * Panels for selecting and prioritising of project-versions
 */
class ProjectVersionPanels extends Component {
  static propTypes = {
    visibleProjectsWithVersions: PropTypes.arrayOf(ProjectType).isRequired,
    selectedVersions: PropTypes.arrayOf(FromProjectVersionType).isRequired,
    /* params: version, projectSlug */
    onVersionCheckboxChange: PropTypes.func.isRequired,
    /* params: project object */
    onAllVersionCheckboxChange: PropTypes.func.isRequired,
    onAllProjectsCheckboxChange: PropTypes.func.isRequired
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
    if (this.props.visibleProjectsWithVersions.length === 0) {
      return <span><Panel /></span>
    }
    const panels =
      this.props.visibleProjectsWithVersions.map((project, index) => {
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

    // loop this.props.visibleProjectsWithVersions and see if all of them are
    // in the this.props.selectedVersions
    const selectedProjectsWithVersions =
      this.props.visibleProjectsWithVersions.filter((visibleProject) => {
        return find(this.props.selectedVersions, (v) => {
          return v.projectSlug === visibleProject.id &&
            find(visibleProject.versions, it => it.id === v.version.id)
        })
      })

    const totalVersions = sumBy(this.props.visibleProjectsWithVersions,
      (proj) => {
        return proj.versions.length
      })
    const totalSelectedVersions = sumBy(selectedProjectsWithVersions,
      (proj) => {
        return proj.versions.length
      })

    const allVersionsChecked = totalVersions === totalSelectedVersions
    const someVersionsChecked = !allVersionsChecked &&
      totalSelectedVersions !== 0

    return (
      <span>
        <div className='checkbox'>
          <span>
            <TriCheckbox
              onChange={this.props.onAllProjectsCheckboxChange}
              checked={allVersionsChecked}
              indeterminate={someVersionsChecked} /> Select all
          </span>
        </div>
        {panels}
      </span>
    )
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
    <Collapse>
      <Panel showArrow={false} header={
        <span className='list-group-item'>
          <SelectAllVersionsCheckbox
            project={project}
            onAllVersionCheckboxChange={onAllVersionCheckboxChange}
            selectedVersionsInProject={selectedVersionsInProject} />
        </span>}>
        <ul>
          {project.versions.map((version, index) => {
            const checked = isVersionInList(selectedVersionsInProject, version)
            return (
              <li className='v list-group-item' key={index}>
                <VersionMenuCheckbox version={version}
                  onVersionCheckboxChange={onVersionCheckboxChange}
                  checked={checked}
                  projectSlug={project.id} />
              </li>
            )
          })}
        </ul>
      </Panel>
    </Collapse>
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
        <span>
          <TriCheckbox
            onChange={this.onAllVersionCheckboxChange}
            checked={allVersionsChecked}
            indeterminate={someVersionsChecked} /> <Icon name='project'
              title='source project' className='s0' /> {project.title} <LockIcon
                status={project.status} />
        </span>
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
        <span>
          <TriCheckbox onChange={this.onVersionCheckboxChange}
            checked={checked} /> <Icon name='version' title='source version'
              className='s0' /> {version.id} <LockIcon
                status={version.status} />
        </span>
      </div>
    )
  }
}

export default ProjectVersionPanels
