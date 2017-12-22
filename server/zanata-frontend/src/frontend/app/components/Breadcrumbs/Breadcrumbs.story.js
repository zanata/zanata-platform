import React from 'react'
import { storiesOf } from '@storybook/react'
import { Breadcrumb, Well } from 'react-bootstrap'

storiesOf('Breadcrumbs', module)
    .add('default', () => (
        <span>
          <h2>Breadcrumbs</h2>
          <Well>Breadcrumbs are used to indicate the current page's location. All pages 2 levels deep or more should use breadcrumbs in frontend. ie. <code>topdir/breadcrumbshere/breadcrumbshere/</code></Well>
          <p>Add <code>active</code> attribute to active Breadcrumb.Item.
            Do not set both active and href attributes. <code>active</code> overrides <code>href</code> and <code>span</code> element is rendered instead of a <code>#</code>.</p>
        <Breadcrumb>
          <Breadcrumb.Item href='#'>
            Home
          </Breadcrumb.Item>
          <Breadcrumb.Item href='s#'>
            Library
          </Breadcrumb.Item>
          <Breadcrumb.Item active>
            Data
          </Breadcrumb.Item>
        </Breadcrumb>
          <hr />
           <h3>Props</h3>
          <p>Breadcrumb component itself doesn't have any specific public properties</p>
        </span>
    ))

