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
 * Entry of User search results
 */
const UserTeaser = ({
  name,
  details,
  ...props
}) => {
  const wordsTranslated = details.wordsTranslated &&
    (<View theme={{ base: { ai: 'Ai(c)', fld: '', m: 'Mstart(a)' } }}>
      <Icon name='translate'
        theme={{
          base: {
            c: 'C(muted)',
            m: 'Mend(rq) Mstart(rh)'
          }
        }} />
        {details.wordsTranslated}
    </View>)
  return (
    <View theme={viewTheme} name={name}>
      <View theme={{ base: {ai: 'Ai(c)', fld: 'Fld(r)'} }}>
        <img
          src={details.avatarUrl}
          alt={details.id}
          className='Bdrs(rnd) Mend(rq) W(r1h) H(r1h)' />
        <Link link={'/profile/' + details.id}
          theme={{ base: { fw: 'Fw(600)' } }}>
          {details.description}
        </Link>
      </View>
      {wordsTranslated}
    </View>
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
