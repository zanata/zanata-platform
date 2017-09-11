import React, {Component} from 'react'
import PropTypes from 'prop-types'
import {FromProjectVersionType} from '../../utils/prop-types-util'
import {Icon, LockIcon} from '../../components'
import {
  SortableContainer,
  SortableElement,
  SortableHandle
} from 'react-sortable-hoc'
import {
  Button,
  ListGroup,
  ListGroupItem,
  Tooltip,
  OverlayTrigger
} from 'react-bootstrap'

export const tooltipSort = (
  <Tooltip id='tooltipsort'>Best match will be chosen based on the priority of
    selected projects. Exact matches take precendence.
  </Tooltip>
)

export const DragHandle = SortableHandle(() =>
  <Icon name='menu' className='n1 drag-handle' title='click to drag' />)

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
      {version.id} <span className='u-textMuted'> {projectSlug}
      </span> <LockIcon status={version.status} />
      {" "}
      <Button bsSize='xsmall' className='close rm-version-btn'
        onClick={this.removeVersion}>
        <Icon name='cross' className='n2 iconCross' title='remove version' />
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
        <span className='versionMergeTitle-adjusted VersionMergeTitle'>
        Adjust priority of selected versions
        </span><br />
        <span className='u-textMuted versionMergeTitle-sub'>
        (best first)
        </span>
        <OverlayTrigger placement='top' overlay={tooltipSort}>
          <Icon name='info' className='s0 iconInfoVersionMerge' />
        </OverlayTrigger>
        {sortableItems}
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
      return (
        <span className="no-v text-muted">
          Please select versions to sort<br />
          <Icon name="version" className="s8" />
        </span>
      )
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
