import React, { PropTypes } from 'react'
import { Link, Icon } from '../../components'

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
    ? (<div className='text-muted'>
        {details.description}
    </div>)
    : (<div className='text-muted'>
      <em>No description available</em>
    </div>)
  const metaData = details.owner && (
    <div className='meta-info'>
      <Icon name='user' className='n1 usericon-muted' />
      <Link to={details.owner}>{details.owner}</Link>
      <Icon name='users' className='usersicon-muted n1' />
      <Link
        to={details.owner + '/' + details.id + '/people'}>
        {details.contributorCount}
      </Link>
    </div>
  )
  const link = window.config.baseUrl + '/project/view/' + details.id
  const className = status !== statusIcons.ACTIVE
    ? 'Fw(600) C(muted)'
    : 'Fw(600)'
  const tooltip = status === statusIcons.ACTIVE
    ? ''
    : 'This project is currently read only'
  return (
    <div className='teaser-view-theme' name={name}>
      <div className='teaser-inner'>
        <div>
          <Link link={link} useHref className={className} title={tooltip}>
            {status !== statusIcons.ACTIVE &&
            (<Icon name={statusIcons[details.status]} className='s1 statusicons'
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
