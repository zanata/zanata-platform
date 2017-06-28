import React, {Component} from 'react'
import PropTypes from 'prop-types'
import {
  SortableContainer,
  SortableElement,
  SortableHandle} from 'react-sortable-hoc'
import {FromProjectVersionType} from '../utils/prop-types-util.js'
import {Button, ListGroup, ListGroupItem} from 'react-bootstrap'

const DragHandle = SortableHandle(() =>
  <Button bsStyle='link' className='btn-link-sort'>
    <i className='fa fa-sort'></i>
  </Button>)

const SortableItem = SortableElement(({value}) =>
  <ListGroupItem className='v' >
    <DragHandle />
    {value.version.id} <span className='text-muted'> {value.projectSlug}</span>
  </ListGroupItem>
)

const SortableList = SortableContainer(({items}) => {
  const sortableItems = items.map((value, index) => (
    <SortableItem key={`item-${index}`} index={index} value={value} />))
  return (
    <div>
      <span className='vmerge-adjtitle vmerge-title'>
      Adjust priority of selected versions
      </span><br />
      <span className='text-muted vmerge-adjsub'>
      (best first)
      </span>
      <div className="pre-scrollable">
      {sortableItems}
      </div>
    </div>
    )
})

/**
 * Root component for the Version TM Merge draggable version priority list
 */
class DraggableVersionPanels extends Component {
  static propTypes = {
    selectedVersions: PropTypes.arrayOf(FromProjectVersionType).isRequired,
    onDraggableMoveEnd: PropTypes.func.isRequired
  }
  render () {
    if (this.props.selectedVersions.length === 0) {
      return <div></div>
    }
    return (
      <ListGroup>
        <SortableList items={this.props.selectedVersions}
          onSortEnd={this.props.onDraggableMoveEnd} useDragHandle />
      </ListGroup>
    )
  }
}

export default DraggableVersionPanels
