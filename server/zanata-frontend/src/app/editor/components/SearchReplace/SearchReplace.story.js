import React from 'react'
import { storiesOf } from '@storybook/react'
import SearchReplace from "./index"

storiesOf('SearchReplace', module)

  .add('default', () => (

    <div>
      <SearchReplace />
    </div>
  ))
