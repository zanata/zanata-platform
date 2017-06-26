import React, {Component} from 'react'
import PropTypes from 'prop-types'
import {Button, ListGroup, ListGroupItem} from 'react-bootstrap'
import Draggable from 'react-draggable'
import {FromProjectVersionType} from '../utils/prop-types-util.js'
/**
 * Root component for Version TM Merge draggable version panels
 */
class DraggableVersionPanels extends Component {
  static propTypes = {
    selectedVersions: PropTypes.arrayOf(FromProjectVersionType).isRequired
  }
  render () {
    if (this.props.selectedVersions.length === 0) {
      return <div></div>
    }
    const draggablePanels =
      this.props.selectedVersions.map((version, index) => {
        return (
          <Draggable bounds='parent' axis='y' grid={[57, 57]} key={index}>
            <ListGroupItem className='v' >
              <Button bsStyle='link' className='btn-link-sort'>
                <i className='fa fa-sort'></i>
              </Button>
              {version.version.id}
              <span className='text-muted'>
              {version.projectSlug}
              </span>
            </ListGroupItem>
          </Draggable>
        )
      }
    )
    return <ListGroup>
      <div>
        <span className='vmerge-adjtitle vmerge-title'>
          Adjust priority of selected versions
        </span><br />
        <span className='text-muted vmerge-adjsub'>
          (best first)
        </span>
      </div>
      {draggablePanels}
    </ListGroup>
  }
}

export default DraggableVersionPanels
