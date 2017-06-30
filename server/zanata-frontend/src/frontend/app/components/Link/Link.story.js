import React from 'react'
import { storiesOf } from '@kadira/storybook'
import { Link } from '../'

storiesOf('Link', module)
    .add('link within frontend app', () => (
      <Link link='/languages'>Languages</Link>
    ))
    .add('link page not in frontend app', () => (
      <Link link='http://zanata.org/language/view/ja' useHref>
        Japanese
      </Link>
    ))
