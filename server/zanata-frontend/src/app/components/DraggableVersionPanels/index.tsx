import * as PropTypes from "prop-types";
import React from "react";
import { Component } from "react";
import {LockIcon} from "../../components";
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
} from "react-bootstrap";
import Button from "antd/lib/button";
import Icon from "antd/lib/icon";
import Tooltip from "antd/lib/tooltip";
import "antd/lib/tooltip/style/";
import Layout from "antd/lib/layout"

export const tooltipSort = <span>Best match</span>;

export const DragHandle = SortableHandle(() =>
  <Icon type="bars" className="n1 drag" title="click to drag" />);

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
      <Button className="close rm-version-btn btn-xs"
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
        <Layout>
        <span className="versionMergeTitle-adjusted VersionMergeTitle">
        Adjust priority of selected versions
        </span><br />
        <span className="u-textMuted versionMergeTitle-sub">
        (best first)
        </span>
        <Tooltip placement="top" title={tooltipSort} trigger="hover">
          <Button className="btn-xs btn-link iconInfoVersionMerge">
            <Icon type="info-circle-o" className="s0" />
          </Button>
        </Tooltip>
        {sortableItems}
        </Layout>
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
          <Icon type="api" className="s8" />
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
