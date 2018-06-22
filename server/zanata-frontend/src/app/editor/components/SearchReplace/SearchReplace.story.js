
import React from 'react'
import { storiesOf } from '@storybook/react'
import { action } from '@storybook/addon-actions'
import SearchReplace from "./index";

storiesOf('SearchReplace', module)
  .add('default', () => (
      <SearchReplace />
  ))
