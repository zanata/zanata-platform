import React from 'react'
import { storiesOf } from '@storybook/react'
import { ListGroup, ListGroupItem } from 'react-bootstrap'

storiesOf('List', module)
    .add('default', () => (
       <ListGroup>
          <ListGroupItem>Item 1</ListGroupItem>
          <ListGroupItem>Item 2</ListGroupItem>
          <ListGroupItem>...</ListGroupItem>
         </ListGroup>
    ))
    .add('with links', () => (
          <ListGroup>
            <ListGroupItem href='#' active>Active</ListGroupItem>
            <ListGroupItem href='#'>Link</ListGroupItem>
            <ListGroupItem href='#' disabled>Disabled</ListGroupItem>
          </ListGroup>
    ))
    .add('with headings', () => (
          <ListGroup>
            <ListGroupItem header='Heading 1'>
              Some body text</ListGroupItem>
            <ListGroupItem header='Heading 2' href='#'>
              Linked item</ListGroupItem>
          </ListGroup>
    ))
