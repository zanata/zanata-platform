import React, {Component} from 'react'
import PropTypes from 'prop-types'
import {
  SortableContainer,
  SortableElement,
  SortableHandle} from 'react-sortable-hoc'
import {FromProjectVersionType} from '../utils/prop-types-util.js'
import {Button, ListGroup, ListGroupItem} from 'react-bootstrap'
import {Icon} from '../components'

const DragHandle = SortableHandle(() =>
  <Button bsStyle='link' className='btn-link-sort'>
    <i className='fa fa-sort'></i>
  </Button>)

class Item extends Component {
  static propTypes = {
    value: PropTypes.object,
    removeVersion: PropTypes.func
  }
  removeVersion = () => {
    const { value: { version, projectSlug } } = this.props
    this.props.removeVersion(projectSlug, version)
  }
  render () {
    const { value: { version, projectSlug } } = this.props
    return <ListGroupItem className='v' >
      <DragHandle />
      {version.id} <span className='text-muted'> {projectSlug}
      </span>
       {/** TODO: Float the button to the right */}
      {" "}
      <Button bsSize='xsmall' className='cross u'
        onClick={this.removeVersion}>
        <Icon name='cross' className='n2 crossicon' title='remove version' />
      </Button>
    </ListGroupItem>
  }
}
const SortableItem = SortableElement(Item)

class Items extends Component {
  static propTypes = {
    items: PropTypes.arrayOf(PropTypes.object),
    removeVersion: PropTypes.func
  }
  render () {
    const { items, removeVersion } = this.props
    const sortableItems = items.map((value, index) => (
      <SortableItem key={`item-${index}`} index={index}
        value={value} removeVersion={removeVersion} />))
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
  }
}

const SortableList = SortableContainer(Items)

/**
 * Root component for the Version TM Merge draggable version priority list
 */
class DraggableVersionPanels extends Component {
  static propTypes = {
    selectedVersions: PropTypes.arrayOf(FromProjectVersionType).isRequired,
    onDraggableMoveEnd: PropTypes.func.isRequired,
    removeVersion: PropTypes.func.isRequired
  }
  render () {
    if (this.props.selectedVersions.length === 0) {
      return <div></div>
    }
    return (
      <ListGroup>
        <SortableList items={this.props.selectedVersions}
          onSortEnd={this.props.onDraggableMoveEnd} useDragHandle
          removeVersion={this.props.removeVersion} />
      </ListGroup>
    )
  }
}

export default DraggableVersionPanels
