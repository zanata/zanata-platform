import React from 'react'
import { storiesOf, action } from '@storybook/react'
import { Pagination, Well } from 'react-bootstrap'

storiesOf('Pagination', module)
    .add('default', () => (
      <span>
        <h2><img src="https://react-bootstrap.github.io/assets/logo.png" width="42px" />Pagination</h2>

        <Well>Multi-page pagination component. Set <code>items</code> to the number of pages. <code>activePage</code> prop dictates which page is active. <a href="https://react-bootstrap.github.io/components.html#pagination-props">Props for react-bootstrap pagination</a></Well>
      <Pagination
          bsSize='medium'
          items={10}
          activePage={1}
          onSelect={action('onSelect')} />
      <hr />
        <h3>More options</h3>
        <p>such as <code>first</code>, <code>last</code>, <code>previous</code>, <code>next</code>, <code>boundaryLinks</code> and <code>ellipsis</code>.</p>
      </span>
    ))
    .add('large', () => (
        <span>
                  <h2><img src="https://react-bootstrap.github.io/assets/logo.png" width="42px" />Pagination - large</h2>

        <Pagination
            bsSize='large'
            items={10}
            activePage={1}
            onSelect={action('onSelect')} /><hr />
        <p><code>bsSize='large'</code></p>
        </span>
    ))
    .add('small', () => (
      <span>
                <h2><img src="https://react-bootstrap.github.io/assets/logo.png" width="42px" />Pagination - small</h2>

        <Pagination
            bsSize='small'
            items={10}
            activePage={1}
            onSelect={action('onSelect')} /><hr />
        <p><code>bsSize='small'</code></p>
</span>
    ))

