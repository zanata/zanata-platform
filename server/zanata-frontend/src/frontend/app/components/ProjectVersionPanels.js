import React, {PropTypes} from 'react'
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
    return (
      <Panel header={<h3><Checkbox>{project.title}</Checkbox></h3>}
        key={index} eventKey={index}>
        <ListGroup fill>
          {project.versions.map((version, index) => {
            const lockIcon = version.status === 'READONLY'
                ? <OverlayTrigger placement='top' overlay={tooltipReadOnly}>
                  <Icon name='locked' className='s0 icon-locked' />
                </OverlayTrigger>
                : ''
            return (
              <ListGroupItem className='v' key={index}>
                <Checkbox>{version.id}{" "}{lockIcon}</Checkbox>
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

ProjectVersionPanels.propTypes = {
  projectVersions: PropTypes.arrayOf(PropTypes.object)
}

export default ProjectVersionPanels
