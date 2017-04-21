import React from 'react'
import { storiesOf, action } from '@kadira/storybook'
import { Pagination } from 'react-bootstrap'

storiesOf('Pagination', module)
    .add('default', () => (
        <span>
      <Pagination
          bsSize='large'
          items={10}
          activePage={1}
          onSelect={action('onSelect')} />
    <br />
    <Pagination
        bsSize='medium'
        items={10}
        activePage={1}
        onSelect={action('onSelect')} />
<br />
<Pagination
    bsSize='small'
    items={10}
    activePage={1}
    onSelect={action('onSelect')} />
        </span>
    ))
