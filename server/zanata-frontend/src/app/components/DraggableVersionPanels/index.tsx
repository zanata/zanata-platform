import * as PropTypes from "prop-types";
import React from "react";
import { Component } from "react";
import {Icon, LockIcon} from "../../components";
import { FromProjectVersion, FromProjectVersionType
} from "../../utils/prop-types-util";
import {
  SortableContainer,
  SortableElement,
  SortableHandle,
} from "react-sortable-hoc";
import {
  ListGroup,
  ListGroupItem,
  Tooltip,
  OverlayTrigger
} from "react-bootstrap";
import { Button } from "antd";

export const tooltipSort = (
  <Tooltip id="tooltipsort">Best match will be chosen based on the priority of
    selected projects. Exact matches take precendence.
  </Tooltip>
)

export const DragHandle = SortableHandle(() =>
  <Icon name="menu" className="n1" parentClassName="drag-handle"
    title="click to drag" />);

interface ItemProps {
  dispatch: (action: any) => void
  removeVersion: (...args: any[]) => any,
  value: any,
}

export class Item extends Component<ItemProps, {}> {
  // @ts-ignore: unused
  private static propTypes = {
    value: FromProjectVersionType.isRequired,
    removeVersion: PropTypes.func.isRequired
  }
  // styling for panel appears in TMMergeModal (ProjectVersion/index.less) css
  public render () {
    const { value: { version, projectSlug } } = this.props
    return <ListGroupItem className="v" >
      <DragHandle />
      {version.id} <span className="u-textMuted"> {projectSlug}
      </span> <LockIcon status={version.status} />
      {" "}
      <Button className="close rm-version-btn btn-xs" aria-label="button"
        onClick={this.removeVersion} icon="close" />
    </ListGroupItem>
  }

  private removeVersion = () => {
    const { value: { version, projectSlug } } = this.props
    this.props.removeVersion(projectSlug, version)
  }
}

const SortableItem = SortableElement(Item as any) as any

interface ItemsProps {
  items: FromProjectVersion[]
  removeVersion: (...args: any[]) => any
}

class Items extends Component<ItemsProps, {}> {
  // @ts-ignore: unused
  private static propTypes = {
    items: PropTypes.arrayOf(FromProjectVersionType).isRequired,
    removeVersion: PropTypes.func.isRequired
  }
  public render () {
    const { items, removeVersion } = this.props
    const sortableItems = items.map((value, index) => (
      <SortableItem
        key={value.projectSlug + ":" + value.version.id} index={index}
        value={value} removeVersion={removeVersion} />))
    return (
      <div>
        <span className="versionMergeTitle-adjusted VersionMergeTitle">
        Adjust priority of selected versions
        </span><br />
        <span className="u-textMuted versionMergeTitle-sub">
        (best first)
        </span>
        <OverlayTrigger placement="top" overlay={tooltipSort}>
          <Icon name="info" className="s0"
            parentClassName="iconInfoVersionMerge" />
        </OverlayTrigger>
        {sortableItems}
      </div>
    )
  }
}

const SortableList = SortableContainer(Items as any) as any

/**
 * Draggable version priority list
 */
class DraggableVersionPanels extends Component<{
  selectedVersions: FromProjectVersion[];
  onDraggableMoveEnd: (...args: any[]) => any;
  removeVersion: (...args: any[]) => any;
}, {}> {
  // @ts-ignore: unused
  private static propTypes = {
    selectedVersions: PropTypes.arrayOf(FromProjectVersionType).isRequired,
    onDraggableMoveEnd: PropTypes.func.isRequired,
    removeVersion: PropTypes.func.isRequired
  }
  public render () {
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
          helperClass="sortable-helper" />
      </ListGroup>
    )
  }
}

export default DraggableVersionPanels
