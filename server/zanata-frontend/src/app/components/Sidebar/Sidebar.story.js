import React from 'react'
import { storiesOf } from '@storybook/react'
import Sidebar from '.'

storiesOf('Sidebar', module)
    .add('default', () => (
      <Sidebar content='Test this' />
    ))
