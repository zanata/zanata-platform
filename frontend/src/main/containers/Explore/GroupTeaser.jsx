import React, { PropTypes } from 'react'
import {
  Link,
  View,
  Icon
} from 'zanata-ui'

const viewTheme = {
  base: {
    ai: 'Ai(c)',
    fld: '',
    m: 'Mb(rh)'
  }
}

const statusIcons = {
  ACTIVE: '',
  READONLY: 'locked',
  OBSOLETE: 'trash'
}
/**
 * Entry of Version Group search results
 */
const GroupTeaser = ({
  details,
  name,
  ...props
}) => {
  const status = statusIcons[details.status]
  const description = details.description
    ? (<div className={'C(muted)'}>
      {details.description}
    </div>)
    : (<div className={'C(muted) Fs(i)'}>
        No description available
    </div>)
  const metaData = details.owner ? (
    <View
      theme={{
        base: {
          ai: 'Ai(c)',
          fld: '',
          fz: 'Fz(msn1)',
          m: 'Mstart(a)--md'
        }
      }}>
      <Icon
        name='user'
        size='n1'
        theme={{ base: { c: 'C(muted)', m: 'Mend(rq)' } }} />
      <Link to={details.owner}>{details.owner}</Link>
      <Icon
        name='users'
        size='n1'
        theme={{
          base: {
            c: 'C(muted)',
            m: 'Mend(rq) Mstart(rh)'
          }
        }}
      />
    </View>
  ) : undefined
  const link = window.config.baseUrl + '/version-group/view/' + details.id
  const theme = status !== statusIcons.ACTIVE
                  ? { base: { fw: 'Fw(600)', c: 'C(muted)' } }
                  : { base: { fw: 'Fw(600)' } }
  return (
    <View theme={viewTheme} name={name}>
      {/* <View className='Mend(rh)'>
        TODO: Statistics Donut here
      </View> */}
      <View theme={{ base: { fld: 'Fld(c) Fld(r)--md', flx: 'Flx(flx1)' } }}>
        <View>
          <Link link={link} useHref theme={theme}>
            {status !== statusIcons.ACTIVE &&
            (<Icon name={statusIcons[details.status]} size='1'
              theme={{ base: { m: 'Mend(rq)' } }} />)}
            {details.title}
          </Link>
          {description}
        </View>
        {metaData}
      </View>
    </View>
  )
}

GroupTeaser.propTypes = {
  /**
   * Entry of the search results.
   */
  details: PropTypes.shape({
    id: PropTypes.string,
    status: PropTypes.string,
    description: PropTypes.string,
    title: PropTypes.string
  }),
  /**
   * Name for the component
   */
  name: PropTypes.string
}

export default GroupTeaser
