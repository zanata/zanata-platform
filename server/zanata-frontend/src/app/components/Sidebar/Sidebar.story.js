import React from 'react'
import { storiesOf } from '@storybook/react'
import Sidebar from '.'

storiesOf('Sidebar', module)
    .add('default', () => (
        <React.Fragment>
          <Sidebar />
        </React.Fragment>
    ))
