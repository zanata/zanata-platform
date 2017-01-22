import React, { PropTypes } from 'react'
import { Link, Icon } from '../../components'

/**
 * Entry of Language team search results
 */
const LanguageTeamTeaser = ({
  name,
  details,
  ...props
}) => {
  const link = window.config.baseUrl + '/language/view/' + details.id
  return (
    <div className='team-teaser-view' name={name}>
      <div className='flex-row'>
        <Link link={link} className='text-bold'>
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
    id: React.PropTypes.string,
    localeDetails: React.PropTypes.object,
    memberCount: React.PropTypes.number
  }),
  /**
   * Name for the component
   */
  name: PropTypes.string
}

export default LanguageTeamTeaser
