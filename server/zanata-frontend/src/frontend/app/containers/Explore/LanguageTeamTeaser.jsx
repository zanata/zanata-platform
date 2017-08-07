import React from 'react'
import PropTypes from 'prop-types'
import { Link, Icon } from '../../components'
import { serverUrl } from '../../config'

/**
 * Entry of Language team search results
 */
const LanguageTeamTeaser = ({
  name,
  details,
  ...props
}) => {
  const link = serverUrl + '/language/view/' + details.id
  return (
    <div className='team-teaser-view' name={name}>
      <div className='flex-row'>
        <Link link={link} useHref className='text-bold'>
          {details.localeDetails.displayName}
        </Link>
        <span className='text-muted langteam-teaser-view-id '>
          {details.id}
        </span>
        <div className='langteam-teaser-view-members'>
          <Icon name='users' className='s1 usersicon-muted' />
          {details.memberCount}
        </div>
      </div>
    </div>
  )
}

LanguageTeamTeaser.propTypes = {
  /**
   * Entry of the search results.
   */
  details: PropTypes.shape({
    id: PropTypes.string,
    localeDetails: PropTypes.object,
    memberCount: PropTypes.number
  }),
  /**
   * Name for the component
   */
  name: PropTypes.string
}

export default LanguageTeamTeaser
