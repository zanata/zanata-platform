import React, {PropTypes} from 'react'
import {
  Button, ListGroup, ListGroupItem
}
  from 'react-bootstrap'
import Draggable from 'react-draggable'

const DraggableVersionPanels = (props) => {
  if (!props.fromProjectVersion[0]) {
    return <div></div>
  }
  const draggablePanels = props.fromProjectVersion.map((version, index) => {
    // TODO: Display the corresponding Project Title here
    return (
      <Draggable bounds='parent' axis='y' grid={[57, 57]} key={index}>
        <ListGroupItem className='v' >
          <Button bsStyle='link' className='btn-link-sort'>
            <i className='fa fa-sort'></i>
          </Button>
          {version.id}
          <span className='text-muted'>
            Project ?
          </span>
        </ListGroupItem>
      </Draggable>
    )
  }
  )
  return <ListGroup><div><span className='vmerge-adjtitle vmerge-title'>
  Adjust priority of selected versions</span><br />
    <span className='text-muted vmerge-adjsub'>(best first)</span>
  </div>{draggablePanels}</ListGroup>
}

DraggableVersionPanels.propTypes = {
  fromProjectVersion: PropTypes.arrayOf(PropTypes.object)
}

export default DraggableVersionPanels
