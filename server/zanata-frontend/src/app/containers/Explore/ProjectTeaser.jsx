// @ts-nocheck
import React from 'react'
import * as PropTypes from 'prop-types'
import { Link, Icon } from '../../components'
import { getProjectUrl } from '../../utils/UrlHelper'

const statusIcons = {
  ACTIVE: '',
  READONLY: 'locked'
}

/**
 * Entry of Project search results
 */
const ProjectTeaser = ({
  details,
  name,
  ...props
}) => {
  const status = statusIcons[details.status]
  const description = details.description
    ? (<div className='u-textMuted'>
        {details.description}
    </div>)
    : (<div className='u-textMuted'>
      <em>Project: {details.id}</em>
    </div>)
  const metaData = details.owner && (
    <div className='metaInfo'>
      <Icon name='user' className='n1' parentClassName='iconUser-muted' />
      <Link to={details.owner}>{details.owner}</Link>
      <Icon name='users' className='n1' parentClassName='iconUsers-muted' />
      <Link
        to={details.owner + '/' + details.id + '/people'}>
        {details.contributorCount}
      </Link>
    </div>
  )
  const link = getProjectUrl(details.id)
  const className = status !== statusIcons.ACTIVE
    ? 'btn-link b txt-muted'
    : 'btn-link b'
  const tooltip = status === statusIcons.ACTIVE
    ? ''
    : 'This project is currently read only'
  return (
    <div className='teaserView' name={name}>
      <div className='teaser-inner'>
        <div>
          <Link link={link} useHref className={className} title={tooltip}>
            {status !== statusIcons.ACTIVE &&
            (<Icon name={statusIcons[details.status]} className='s1'
              parentClassName='iconsStatus'
            />)}
            {details.title}
          </Link>
          {description}
        </div>
        {metaData}
      </div>
    </div>
  )
}

ProjectTeaser.propTypes = {
  /**
   * Entry of the search results.
   */
  details: PropTypes.shape({
    id: PropTypes.string,
    status: PropTypes.string,
    description: PropTypes.string,
    title: PropTypes.string,
    contributorCount: PropTypes.number
  }),
  /**
   * Name for the component
   */
  name: PropTypes.string
}

export default ProjectTeaser
