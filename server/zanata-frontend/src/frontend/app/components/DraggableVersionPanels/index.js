import React, {Component} from 'react'
import PropTypes from 'prop-types'
import {
  SortableContainer,
  SortableElement,
  SortableHandle} from 'react-sortable-hoc'
import {FromProjectVersionType} from '../../utils/prop-types-util'
import {Button, ListGroup, ListGroupItem} from 'react-bootstrap'
import {Icon, LockIcon} from '../../components'

export const DragHandle = SortableHandle(() =>
  // TODO: Make an Icon component for the sortable handle
  // <i className='fa fa-sort'></i>
  <Icon name='chevron-up' className='s1' />)

export class Item extends Component {
  static propTypes = {
    value: FromProjectVersionType.isRequired,
    removeVersion: PropTypes.func.isRequired
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
      </span> <LockIcon status={version.status} />
      {" "}
      <Button bsSize='xsmall' className='close rm-version-btn'
        onClick={this.removeVersion}>
        <Icon name='cross' className='n2 crossicon' title='remove version' />
      </Button>
    </ListGroupItem>
  }
}
const SortableItem = SortableElement(Item)

class Items extends Component {
  static propTypes = {
    items: PropTypes.arrayOf(FromProjectVersionType).isRequired,
    removeVersion: PropTypes.func.isRequired
  }
  render () {
    const { items, removeVersion } = this.props
    const sortableItems = items.map((value, index) => (
      <SortableItem
        key={value.projectSlug + ':' + value.version.id} index={index}
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
 * Draggable version priority list
 */
class DraggableVersionPanels extends Component {
  static propTypes = {
    selectedVersions: PropTypes.arrayOf(FromProjectVersionType).isRequired,
    onDraggableMoveEnd: PropTypes.func.isRequired,
    removeVersion: PropTypes.func.isRequired
  }
  render () {
    if (this.props.selectedVersions.length === 0) {
      return <span></span>
    }
    return (
      <ListGroup>
        <SortableList items={this.props.selectedVersions}
          onSortEnd={this.props.onDraggableMoveEnd} useDragHandle
          removeVersion={this.props.removeVersion}
          helperClass='sortable-helper' />
      </ListGroup>
    )
  }
}

export default DraggableVersionPanels
