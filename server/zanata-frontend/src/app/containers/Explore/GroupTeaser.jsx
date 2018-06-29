// @ts-nocheck
import React from 'react'
import * as PropTypes from 'prop-types'
import { Link, Icon } from '../../components'
import { getVersionGroupUrl } from '../../utils/UrlHelper'

/**
 * Entry of Version Group search results
 */
const GroupTeaser = ({
  details,
  name,
  ...props
}) => {
  const description = details.description
    ? (<div className='u-textMuted'>
      {details.description}
    </div>)
    : (<div className='u-textMuted'>
      <em>Group : {details.id}</em>
    </div>)
  const metaData = details.owner ? (
    <div className='metaInfo'>
      <Icon name='user' className='n1' parentClassName='iconUser-muted' />
      <Link to={details.owner}>{details.owner}</Link>
      <Icon name='users' className='n1' parentClassName='iconUsers-muted' />
    </div>
  ) : undefined
  const link = getVersionGroupUrl(details.id)

  return (
    <div className='teaserView' name={name}>
      {/* <View className='Mend(rh)'>
        TODO: Statistics Donut here
      </View> */}
      <div className='teaser-inner'>
        <div>
          <Link link={link} useHref className='btn-link b'>
            {details.title}
          </Link>
          {description}
        </div>
        {metaData}
      </div>
    </div>
  )
}

GroupTeaser.propTypes = {
  /**
   * Entry of the search results.
   */
  details: PropTypes.shape({
    id: PropTypes.string,
    description: PropTypes.string,
    title: PropTypes.string
  }),
  /**
   * Name for the component
   */
  name: PropTypes.string
}

export default GroupTeaser
