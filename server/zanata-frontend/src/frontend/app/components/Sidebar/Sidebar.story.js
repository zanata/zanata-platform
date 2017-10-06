import React from 'react'
import { storiesOf } from '@storybook/react'
import { Sidebar } from '../'

storiesOf('Sidebar', module)
    .add('default', () => (
        <div>
          <Sidebar />
          <div className='flexTab'>
            <p>This sidebar example has the active tag applied to both the People
              and Languages pages to provide examples of how this design handles
              sidebar links.</p>
            <p>The sidebar nav has been implemented using &nbsp;
              <a href='https://react-bootstrap.github.io/components.html#navs'>
                react bootstrap components</a>.</p>
          </div>
      </div>
    ))
