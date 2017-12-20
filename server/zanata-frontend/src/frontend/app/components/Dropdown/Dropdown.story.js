import React from 'react'
import { storiesOf, action } from '@storybook/react'
import { DropdownButton, MenuItem, Table } from 'react-bootstrap'

storiesOf('Dropdown', module)
    .add('default', () => (
      <span>
        <h2>Dropdown</h2>
        <p>Dropdown button to be used when there are multiple actions (a maximum of 7 to 15) available. Use dropdowns where radio buttons would not make sense due to too many options.</p>
      <DropdownButton bsStyle='default' title='Dropdown button'
        id='dropdown-basic'>
        <MenuItem onClick={action('onClick')} eventKey='1'>
          Action</MenuItem>
        <MenuItem onClick={action('onClick')} eventKey='2'>
          Another action</MenuItem>
        <MenuItem  onClick={action('onClick')}eventKey='3' active>
          Active Item</MenuItem>
      </DropdownButton>
        <h3>Props</h3>
        <p>The Dropdown expects at least one component with bsRole="toggle" and exactly one with bsRole="menu"</p>

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
            <td>boolean</td>
            <td>'false'</td>
            <td></td>
          </tr>
          <tr>
            <td>block</td>
            <td>boolean</td>
            <td>'false'</td>
            <td></td>
          </tr>
           <tr>
            <td>bsClass</td>
            <td>string</td>
            <td>'btn'</td>
            <td>Base CSS class and prefix for the component. Generally one should only change bsClass to provide new, non-Bootstrap, CSS styles for a component.</td>
          </tr>
           <tr>
            <td>bsSize</td>
            <td>one of: "lg", "large", "sm", "small", "xs", "xsmall"</td>
            <td></td>
            <td>Component size variations</td>
          </tr>
           <tr>
            <td>bsStyle</td>
            <td>one of: "success", "warning", "danger", "info", "default", "primary", "link"
</td>
            <td>'default'</td>
            <td>Component visual or contextual style variants.</td>
                    </tr>
           <tr>
            <td>componentClass</td>
            <td>elementType
</td>
            <td></td>
            <td>You can use a custom element type for this component.</td>
          </tr>
                      <tr>
            <td>disabled</td>
            <td>boolean</td>
            <td>'false'</td>
            <td></td>
          </tr>
                      <tr>
            <td>href</td>
            <td>string</td>
            <td></td>
            <td></td>
          </tr>
                                  <tr>
            <td>type</td>
            <td>one of: 'button', 'reset', 'submit'</td>
            <td>'button'</td>
            <td>Defines HTML button type attribute</td>
          </tr>
          </tbody>
          </Table>
      <h2>MenuItem</h2>
      </span>
    ))
