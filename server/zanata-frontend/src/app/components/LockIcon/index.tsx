import React from "react";
import Tooltip from "antd/lib/tooltip";
import "antd/lib/tooltip/style/css";
import {Icon} from "../../components";
import {EntityStatus, isEntityStatusReadOnly} from "../../utils/EnumValueUtils";
import {entityStatusPropType} from "../../utils/prop-types-util";

const DO_NOT_RENDER: null = null

/**
 * Version Lock Icon with tooltip
 */
const LockIcon: React.SFC<LockIconProps> = ({status}) => {
  const tooltipReadOnly = <span id="tooltipreadonly">Read only</span>;
  return isEntityStatusReadOnly(status)
  ? (
    <Tooltip placement="top" title={tooltipReadOnly}>
      <Icon name="locked" className="s0 txt-warn" />
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
