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
    <div className='langTeamTeaserView' name={name}>
      <div className='flex-row'>
        <Link link={link} className='bold-font'>
          {details.localeDetails.displayName}
        </Link>
        <span className='text-muted langTeamTeaserViewId '>
          {details.id}
        </span>
      </div>
      <div className='langTeamTeaserViewMembers'>
        <Icon name='users' classname='usersicon-muted' />
      {details.memberCount}
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
