import React from 'react'
import { storiesOf } from '@storybook/react'
import SearchReplace from './index'

// @ts-ignore
const data = []
for (let i = 0; i < 20; i++) {
  data.push({
    sources: ['source text'],
    translations: ['target text']
  })
}

storiesOf('SearchReplace', module)

  .add('default', () => (
    // @ts-ignore
    <SearchReplace phrases={data} />
  ))
