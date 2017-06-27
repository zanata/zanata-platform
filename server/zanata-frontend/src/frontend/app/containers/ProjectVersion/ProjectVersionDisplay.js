import React from 'react'
import PropTypes from 'prop-types'
import {Icon} from '../../components'

export const ProjectVersionVertical = (props) => {
  const {projectSlug, versionSlug} = props
  return (
    <ul>
      <li>
        <Icon name='project' className='s0 tmx-icon' />
        {projectSlug}
      </li>
      <li>
        <Icon name='version' title='version'
          className='s0 tmx-icon' />
        {versionSlug}
      </li>
    </ul>
  )
}
ProjectVersionVertical.propTypes = {
  projectSlug: PropTypes.string.isRequired,
  versionSlug: PropTypes.string.isRequired
}
