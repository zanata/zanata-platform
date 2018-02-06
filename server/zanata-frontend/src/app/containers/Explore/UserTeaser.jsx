import React from 'react'
import * as PropTypes from 'prop-types'
import { Link, Icon } from '../../components'
import { getProfileUrl } from '../../utils/UrlHelper'

/**
 * Entry of User search results
 */
const UserTeaser = ({
  name,
  details,
  ...props
}) => {
  const wordsTranslated = details.wordsTranslated &&
    (<div className='wordsTranslatedView'>
      <Icon parentClassName='iconTranslate-muted' />
        {details.wordsTranslated}
    </div>)
  const url = getProfileUrl(details.id)
  return (
    <div className='teaserView' name={name}>
      <div className='userTeaser-inner'>
        <img
          src={details.avatarUrl}
          alt={details.id}
          className='avatar-round' />
        <Link link={url} className='text-bold'>
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
