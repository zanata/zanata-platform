import React, { PropTypes } from 'react'
import { Link, Icon } from '../../components'

/**
 * Entry of User search results
 */
const UserTeaser = ({
  name,
  details,
  ...props
}) => {
  const wordsTranslated = details.wordsTranslated &&
    (<div className='words-translated-view'>
      <Icon className='translateicon-muted' />
        {details.wordsTranslated}
    </div>)
  return (
    <div className='teaser-view-theme' name={name}>
      <div className='user-teaser-inner'>
        <img
          src={details.avatarUrl}
          alt={details.id}
          className='avatar-round' />
        <Link link={'/profile/view/' + details.id}
          className='text-bold'>
          {details.description}
        </Link>
      </div>
      {wordsTranslated}
    </div>
  )
}

UserTeaser.propTypes = {
  /**
   * Entry of the search results.
   */
  details: PropTypes.shape({
    id: PropTypes.string,
    avatarUrl: PropTypes.string,
    description: PropTypes.string,
    wordsTranslated: PropTypes.number
  }),
  /**
   * Name for the component
   */
  name: PropTypes.string
}

export default UserTeaser
