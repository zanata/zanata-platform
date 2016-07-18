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
    <View theme={viewTheme} name={name}>
      <View theme={{ base: {fld: 'Fld(r)'} }}>
        <Link link={link}
          theme={{ base: { fw: 'Fw(600)' } }}>
          {details.locale}
        </Link>
        <span className='C(muted) Mstart(rq)'>
          {details.id}
        </span>
      </View>
      <View theme={{ base: { ai: 'Ai(c)', fld: '', m: 'Mstart(a)' } }} >
        <Icon name='users'
          theme={{
            base: {
              c: 'C(muted)',
              m: 'Mend(rq) Mstart(rh)'
            }
          }}
        />
      {details.memberCount}
      </View>
    </View>
  )
}

LanguageTeamTeaser.propTypes = {
  /**
   * Entry of the search results.
   */
  details: PropTypes.shape({
    id: React.PropTypes.string,
    locale: React.PropTypes.string,
    memberCount: React.PropTypes.number
  }),
  /**
   * Name for the component
   */
  name: PropTypes.string
}

export default LanguageTeamTeaser
