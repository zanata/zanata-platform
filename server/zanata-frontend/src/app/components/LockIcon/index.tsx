import React from 'react'
import {OverlayTrigger, Tooltip} from 'react-bootstrap'
import {Icon} from '../../components'
import {EntityStatus, isEntityStatusReadOnly} from '../../utils/EnumValueUtils'
import {entityStatusPropType} from '../../utils/prop-types-util'

const DO_NOT_RENDER: null = null

/**
 * Version Lock Icon with tooltip
 */
const LockIcon: React.SFC<LockIconProps> = ({status}) => {
  const tooltipReadOnly = <Tooltip id='tooltipreadonly'>Read only</Tooltip>
  return isEntityStatusReadOnly(status)
  ? (
    <OverlayTrigger placement='top' overlay={tooltipReadOnly}>
      <Icon name='locked' className='s0' parentClassName='icon-locked' />
    </OverlayTrigger>
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
