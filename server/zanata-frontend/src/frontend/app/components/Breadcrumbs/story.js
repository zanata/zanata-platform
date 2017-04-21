import React from 'react'
import { storiesOf, action } from '@kadira/storybook'
import { Breadcrumb } from 'react-bootstrap'

storiesOf('Breadcrumbs', module)
    .add('default', () => (
        <Breadcrumb>
          <Breadcrumb.Item href='#'>
            Home
          </Breadcrumb.Item>
          <Breadcrumb.Item href='#'>
            Library
          </Breadcrumb.Item>
          <Breadcrumb.Item active>
            Data
          </Breadcrumb.Item>
        </Breadcrumb>
    ))

