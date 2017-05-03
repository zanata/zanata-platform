import React from 'react'
import { storiesOf, action } from '@kadira/storybook'
import { ListGroup, ListGroupItem } from 'react-bootstrap'

storiesOf('Lists', module)
    .add('in-headings', () => (
    <span>
       <ListGroup>
            <ListGroupItem>Item 1</ListGroupItem>
            <ListGroupItem>Item 2</ListGroupItem>
            <ListGroupItem>...</ListGroupItem>
          </ListGroup>
          <ListGroup>
            <ListGroupItem href='#' active>Active</ListGroupItem>
            <ListGroupItem href='#'>Link</ListGroupItem>
            <ListGroupItem href='#' disabled>Disabled</ListGroupItem>
          </ListGroup>
          <ListGroup>
            <ListGroupItem header='Heading 1'>
            Some body text</ListGroupItem>
            <ListGroupItem header='Heading 2' href='#'>
            Linked item</ListGroupItem>
          </ListGroup>
      </span>
    ))
