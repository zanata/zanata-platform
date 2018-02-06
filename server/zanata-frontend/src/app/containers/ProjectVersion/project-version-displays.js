import React from 'react'
import * as PropTypes from 'prop-types'
import {Icon} from '../../components'

export const ProjectVersionVertical = ({projectSlug, versionSlug}) => {
  return (
    <ul>
      <li className='list-group-item' title='target project' >
        <Icon name='project' className='s0' parentClassName='iconTMX' />
        {projectSlug}
      </li>
      <li className='list-group-item' title='target version'>
        <Icon name='version' className='s0' parentClassName='iconTMX' />
        {versionSlug}
      </li>
    </ul>
  )
}
const projectVersionDisplayPropTypes = {
  projectSlug: PropTypes.string.isRequired,
  versionSlug: PropTypes.string.isRequired
}
ProjectVersionVertical.propTypes = projectVersionDisplayPropTypes

export const ProjectVersionHorizontal = ({projectSlug, versionSlug}) => {
  return (
    <span>
      <span className="item">
        <Icon name='project' className='s1'
          parentClassName='iconTMX' />{projectSlug}
      </span>
      <span className="item">
        <Icon name='version' className='s1'
          parentClassName='iconTMX' />{versionSlug}
      </span>
    </span>
  )
}
ProjectVersionHorizontal.propTypes = projectVersionDisplayPropTypes
