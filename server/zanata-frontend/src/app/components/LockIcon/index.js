import * as React from 'react'
import {Tooltip, OverlayTrigger} from 'react-bootstrap'
import {Icon} from '../../components'
import {entityStatusPropType} from '../../utils/prop-types-util'
import {isEntityStatusReadOnly} from '../../utils/EnumValueUtils'

const DO_NOT_RENDER = null

/**
 * Version Lock Icon with tooltip
 *
 * @param status
 * @returns {XML}
 */
const LockIcon = ({status}) => {
  const tooltipReadOnly = <Tooltip id='tooltipreadonly'>Read only</Tooltip>
  return isEntityStatusReadOnly(status)
  ? (
    <OverlayTrigger placement='top' overlay={tooltipReadOnly}>
      <Icon name='locked' className='s0' parentClassName='icon-locked' />
    </OverlayTrigger>
  )
  : DO_NOT_RENDER
}
LockIcon.propTypes = {
  status: entityStatusPropType
}

export default LockIcon
