import * as PropTypes from "prop-types";
import React from "react";
import { Component } from "react";
import {LockIcon, Icon} from "../../components";
import { FromProjectVersion, FromProjectVersionType
} from "../../utils/prop-types-util";
import {
  SortableContainer,
  SortableElement,
  SortableHandle,
} from "react-sortable-hoc";
import Button from "antd/lib/button";
import "antd/lib/button/style/css";
import Tooltip from "antd/lib/tooltip";
import "antd/lib/tooltip/style/css";
import Layout from "antd/lib/layout";
import "antd/lib/layout/style/css";

export const tooltipSort = <span>Best match</span>;

export const DragHandle = SortableHandle(() =>
  <Icon name="menu" className="n1 drag" title="click to drag" />);

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
    return <li className="v list-group-item" >
      <DragHandle />
      <span className="ml2">
        {version.id}
      </span>
      <span className="fw5 ml4 ml1">
        {projectSlug}
      </span> <LockIcon status={version.status} />
      {" "}
      <Button className="close btn-xs" aria-label="button"
        onClick={this.removeVersion} icon="close" />
    </li>;
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
        <Layout className="d-inh">
        Adjust priority of selected versions
        <br />
        <span className="txt-muted">
        (best first)
        </span>
        <Tooltip placement="top" title={tooltipSort} trigger="hover">
          <a className="btn-xs btn-link">
            <Icon name="info" className="s0" />
          </a>
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
        <p className="no-v tc txt-muted">
          Please select versions to sort<br />
          <Icon name="version" className="s8" />
        </p>
      )
    }
    return (
      <span>
        <SortableList items={this.props.selectedVersions}
          onSortEnd={this.props.onDraggableMoveEnd} useDragHandle
          removeVersion={this.props.removeVersion}
          helperClass="sortable-helper" />
      </span>
    )
  }
}

export default DraggableVersionPanels
