// @ts-nocheck
import React from 'react'
import * as PropTypes from 'prop-types'
import { Link, Icon } from '../../components'
import { getLanguageUrl } from '../../utils/UrlHelper'

/**
 * Entry of Language team search results
 */
const LanguageTeamTeaser = ({
  name,
  details,
  ...props
}) => {
  const link = getLanguageUrl(details.id)
  return (
    <div className='teamTeaser' name={name}>
      <div className='u-flexRow'>
        <Link link={link} useHref className='btn-link b'>
          {details.localeDetails.displayName}
        </Link>
        <span className='txt-muted ml2'>
          {details.id}
        </span>
        <div className='languageTeamTeaser-members'>
          <Icon name='users' className='s1 txt-muted v-mid mr2' />
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
