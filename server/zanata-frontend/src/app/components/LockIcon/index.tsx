import React from "react";
import Icon from "antd/lib/icon";
import Tooltip from "antd/lib/tooltip";
import {EntityStatus, isEntityStatusReadOnly} from "../../utils/EnumValueUtils";
import {entityStatusPropType} from "../../utils/prop-types-util";

const DO_NOT_RENDER: null = null

/**
 * Version Lock Icon with tooltip
 */
const LockIcon: React.SFC<LockIconProps> = ({status}) => {
  const tooltipReadOnly = <span>Read only</span>;
  return isEntityStatusReadOnly(status)
  ? (
    <Tooltip placement="top" title={tooltipReadOnly}>
      <Icon type="lock" className="s0 icon-locked" />
    </Tooltip>
  )
  : DO_NOT_RENDER
}

interface LockIconProps {
  status: EntityStatus
}

LockIcon.propTypes = {
  status: entityStatusPropType
}

export default LockIcon
