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

const ProjectVersionPanels = (props) => {
  if (!props.projectVersions[0]) {
    return <div></div>
  }
  let panels = props.projectVersions.map((project, index) => {
    const projectLockIcon = project.status === 'READONLY'
        ? <OverlayTrigger placement='top' overlay={tooltipReadOnly}>
          <Icon name='locked' className='s0 icon-locked' />
        </OverlayTrigger>
        : ''
    return (
      <Panel header={<h3><Checkbox>{project.title}{" "}{projectLockIcon}
      </Checkbox></h3>} key={index} eventKey={index}>
        <ListGroup fill>
          {project.versions.map((version, index) => {
            return (
              <ListGroupItem className='v' key={index}>
                <VersionMenuCheckbox version={version}
                  onClick={props.onVersionCheckboxChange}
                  fromProjectVersion={props.fromProjectVersion} />
              </ListGroupItem>
                )
          })}
        </ListGroup>
      </Panel>
      )
  }
  )
  return <PanelGroup defaultActiveKey='1' accordion>{panels}</PanelGroup>
}

class VersionMenuCheckbox extends Component {
  static propTypes = {
    version: PropTypes.object,
    onClick: PropTypes.func.isRequired,
    fromProjectVersion: PropTypes.arrayOf(PropTypes.object)
  }
  onClick = () => {
    this.props.onClick(this.props.version)
  }
  render () {
    const {
      version,
      fromProjectVersion
    } = this.props
    const versionChecked = fromProjectVersion.includes(version)
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

ProjectVersionPanels.propTypes = {
  projectVersions: PropTypes.arrayOf(PropTypes.object),
  fromProjectVersion: PropTypes.arrayOf(PropTypes.object),
  onVersionCheckboxChange: PropTypes.func.isRequired
}

export default ProjectVersionPanels
