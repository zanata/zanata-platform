import React from 'react'
import PropTypes from 'prop-types'
import {Icon} from '../../components'

export const ProjectVersionVertical = ({projectSlug, versionSlug}) => {
  return (
    <ul>
      <li className='list-group-item' title='target project' >
        <Icon name='project' className='s0 tmx-icon' />
        {projectSlug}
      </li>
      <li className='list-group-item' title='target version'>
        <Icon name='version' className='s0 tmx-icon' />
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
      <Icon name='project' className='s1 tmx-icon' />{projectSlug}
      <Icon name='version' className='s1 tmx-icon' />{versionSlug}
    </span>
  )
}
ProjectVersionHorizontal.propTypes = projectVersionDisplayPropTypes
