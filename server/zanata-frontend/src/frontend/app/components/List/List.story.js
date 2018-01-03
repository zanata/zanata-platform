import React from 'react'
import {storiesOf} from '@storybook/react'
import {ListGroup, ListGroupItem, Well, Table} from 'react-bootstrap'

storiesOf('List group', module)
    .add('default', () => (
        <span>
          <h2><img src="https://react-bootstrap.github.io/assets/logo.png"
                   width="42px"/>List group</h2>
          <Well>List groups are a flexible and powerful component for displaying not only simple lists of elements, but complex ones with custom content.</Well>
       <ListGroup>
          <ListGroupItem>Item 1</ListGroupItem>
          <ListGroupItem>Item 2</ListGroupItem>
          <ListGroupItem>...</ListGroupItem>
         </ListGroup>
          <hr/>
          <h3>Props</h3>
          <h4>ListGroup</h4>
          <Table striped bordered condensed hover>
          <thead>
          <tr>
            <th>Name</th>
            <th>Type</th>
            <th>Default</th>
            <th>Description</th>
          </tr>
          </thead>
          <tbody>
           <tr>
            <td>bsClass</td>
            <td>string</td>
            <td>'list-group'</td>
            <td>Base CSS class and prefix for the component. Generally one should only change bsClass to provide new, non-Bootstrap, CSS styles for a component.</td>
          </tr>
           <tr>
            <td>componentClass</td>
             <td>elementType</td>
            <td></td>
            <td>You can use a custom element type for this component.

              If not specified, it will be treated as <code>'li'</code> if every child is a non-actionable <code>ListGroupItem></code>, and <code>'div'</code> otherwise.</td>
                    </tr>
          </tbody>
                    </Table>
                   <h4>ListGroupItem</h4>
          <Table striped bordered condensed hover>
          <thead>
          <tr>
            <th>Name</th>
            <th>Type</th>
            <th>Default</th>
            <th>Description</th>
          </tr>
          </thead>
          <tbody>
                  <tr>
            <td>active</td>
            <td>any</td>
            <td></td>
                    <td></td>
                  </tr>
           <tr>
            <td>bsClass</td>
            <td>string</td>
            <td>'list-group-item'</td>
            <td>Base CSS class and prefix for the component. Generally one should only change bsClass to provide new, non-Bootstrap, CSS styles for a component.</td>
          </tr>
           <tr>
              <td>bsStyle</td>
             <td>one of: <code>"success"</code>, <code>"warning"</code>, <code>"danger"</code>, <code>"info"</code>, <code>"default"</code>, <code>"primary"</code>, <code>"link"</code>
</td>
            <td>'default'</td>
            <td>Component visual or contextual style variants.</td>
                    </tr>
                            <tr>
            <td>disabled</td>
            <td>any</td>
            <td></td>
                    <td></td>
                  </tr>
                            <tr>
            <td>header</td>
            <td>node</td>
            <td></td>
                    <td></td>
                  </tr>
                            <tr>
            <td>href</td>
            <td>string</td>
            <td></td>
                    <td></td>
                  </tr>
                            <tr>
            <td>listItem</td>
            <td>boolean</td>
            <td>false</td>
                    <td></td>
                  </tr>
                            <tr>
            <td>onClick</td>
            <td>function</td>
            <td></td>
                    <td></td>
                  </tr>
                            <tr>
            <td>type</td>
            <td>string</td>
            <td></td>
                    <td></td>
                  </tr>
          </tbody>
                    </Table>
        </span>
    ))
    .add('with links', () => (
        <span>
          <h2><img src="https://react-bootstrap.github.io/assets/logo.png"
                   width="42px"/>With links</h2>
          <Well>Set the <code>href</code> or <code>onClick</code> prop on <code>ListGroupItem</code>, to create a linked or clickable element.
</Well>
        <ListGroup>
            <ListGroupItem href='#' active>Active</ListGroupItem>
            <ListGroupItem href='#'>Link</ListGroupItem>
            <ListGroupItem href='#' disabled>Disabled</ListGroupItem>
          </ListGroup>
        </span>
    ))
    .add('with headings', () => (
        <span>
          <h2><img src="https://react-bootstrap.github.io/assets/logo.png"
                   width="42px"/>With headings</h2>

        <Well>Set the header prop to create a structured item, with a heading and a body area.
        </Well>
          <ListGroup>
            <ListGroupItem header='Heading 1'>
              Some body text</ListGroupItem>
            <ListGroupItem header='Heading 2' href='#'>
              Linked item</ListGroupItem>
          </ListGroup>
        </span>
    ))
