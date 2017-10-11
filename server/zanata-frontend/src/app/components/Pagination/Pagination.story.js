import React from 'react'
import { storiesOf, action } from '@storybook/react'
import { Pagination } from 'react-bootstrap'

storiesOf('Pagination', module)
    .add('default', () => (
      <Pagination
          bsSize='medium'
          items={10}
          activePage={1}
          onSelect={action('onSelect')} />
    ))
    .add('large', () => (
        <Pagination
            bsSize='large'
            items={10}
            activePage={1}
            onSelect={action('onSelect')} />
    ))
    .add('small', () => (
        <Pagination
            bsSize='small'
            items={10}
            activePage={1}
            onSelect={action('onSelect')} />
    ))

