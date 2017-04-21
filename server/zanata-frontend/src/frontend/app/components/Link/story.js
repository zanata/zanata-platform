import React from 'react'
import { storiesOf, action } from '@kadira/storybook'
import { Link } from '../'

storiesOf('Link', module)
    .add('default', () => (
      <Link link='www.zanata.org'>link to zanata.org</Link>
    ))
