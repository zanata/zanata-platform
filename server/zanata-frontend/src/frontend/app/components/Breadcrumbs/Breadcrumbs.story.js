import React from 'react'
import { storiesOf } from '@storybook/react'
import { Breadcrumb } from 'react-bootstrap'

storiesOf('Breadcrumbs', module)
    .add('default', () => (
        <span>
          <h2>Breadcrumbs</h2>
          <p>Breadcrumbs are used to indicate the current page's location. Add active attribute to active Breadcrumb.Item.
Do not set both active and href attributes. active overrides href and span element is rendered instead of a.
            #</p>
          <p><strong>Guideline:</strong> All pages 2 levels deep or more should use breadcrumbs in frontend. ie. topdir/breadcrumbshere/breadcrumbshere/</p>
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
           <h3>Props</h3>
          <p>Breadcrumb component itself doesn't have any specific public properties</p>
        </span>
    ))

