import React from 'react'
import { storiesOf, action } from '@storybook/react'
import { DropdownButton, MenuItem, Table, Label } from 'react-bootstrap'

storiesOf('Dropdown', module)
    .add('default', () => (
      <span>
        <h2>Dropdown</h2>
        <p>Dropdown button to be used when there are multiple actions (a maximum of 7 to 15) available. Use dropdowns where radio buttons would not make sense due to too many options.</p>
      <DropdownButton bsStyle='default' title='Dropdown button' id='dropdown-basic'>
        <MenuItem onClick={action('onClick')} eventKey='1'>
          Action</MenuItem>
        <MenuItem onClick={action('onClick')} eventKey='2'>
          Another action</MenuItem>
        <MenuItem  onClick={action('onClick')} eventKey='3' active>
          Active Item</MenuItem>
      </DropdownButton>
        <h3>Props</h3>
        <p>The Dropdown expects at least one component with <code>bsRole="toggle"</code> and exactly one with <code>bsRole="menu"</code></p>

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
            <td>bsSize</td>
            <td>one of: "lg", "large", "sm", "small", "xs", "xsmall"</td>
            <td></td>
            <td>Component size variations</td>
          </tr>
             <tr>
            <td>bsClass</td>
            <td>string</td>
            <td>'btn'</td>
            <td>Base CSS class and prefix for the component. Generally one should only change bsClass to provide new, non-Bootstrap, CSS styles for a component.</td>
          </tr>
           <tr>
            <td>componentClass</td>
            <td>elementType
</td>
            <td>ButtonGroup</td>
            <td>You can use a custom element type for this component.</td>
          </tr>
                     <tr>
            <td>defaultOpen</td>
            <td>boolean</td>
            <td></td>
            <td></td>
          </tr>
          <tr>
            <td>disabled</td>
            <td>boolean</td>
            <td></td>
            <td>Whether or not component was disabled.</td>
          </tr>
                      <tr>
            <td>dropup</td>
            <td>boolean</td>
                        <td></td>
            <td>The menu will open above the dropdown button, instead of below it.</td>
          </tr>
          <tr>
            <td>id <Label>required</Label></td>
            <td>string|number</td>
            <td>An html id attribute, necessary for assistive technologies, such as screen readers.</td>
            <td>Defines HTML button type attribute</td>
          </tr>
                                <tr>
            <td>noCaret</td>
            <td>boolean</td>
            <td></td>
            <td></td>
          </tr>
                                <tr>
            <td>onSelect</td>
            <td>function</td>
            <td></td>
            <td>A callback fired when a menu item is selected.

              <code>(eventKey: any, event: Object) => any</code></td>
          </tr>
          <tr>
              <td>onToggle</td>
            <td>function</td>
            <td></td>
            <td>A callback fired when the Dropdown wishes to change visibility. Called with the requested open value, the DOM event, and the source that fired it: <code>'click','keydown','rootClose', or 'select''</code></td>
          </tr>
                   <tr>
            <td>open</td>
            <td>boolean</td>
            <td></td>
            <td> controlled by: onToggle, initial prop: defaultOpen
Whether or not the Dropdown is visible.</td>
          </tr>
                   <tr>
            <td>pullRight</td>
            <td>boolean</td>
            <td></td>
            <td>Align the menu to the right side of the Dropdown toggle</td>
          </tr>
                   <tr>
            <td>role</td>
            <td>string</td>
            <td></td>
            <td>If 'menuitem', causes the dropdown to behave like a menu item rather than a menu button.</td>
          </tr>
                   <tr>
            <td>rootCloseEvent</td>
            <td>one of: 'click', 'mousedown'</td>
            <td></td>
            <td>Which event when fired outside the component will cause it to be closed</td>
          </tr>
              <tr>
            <td>title <Label>required</Label></td>
            <td>node</td>
            <td></td>
                        <td></td>

          </tr>
          </tbody>
          </Table>
      <h2>MenuItem</h2>
        <p>This component represents a menu item in a dropdown.<p>

          <p>It supports the basic anchor properties href, target, title.</p>
          <p>It also supports different properties of the normal Bootstrap MenuItem.</p>
<ul>
  <li>header: To add a header label to sections</li>
  <li>divider: Adds an horizontal divider between sections</li>
  <li>disabled: shows the item as disabled, and prevents onSelect from firing</li>
  <li>eventKey: passed to the callback</li>
  <li>onSelect: a callback that is called when the user clicks the item.</li>
</ul>
          <p>The callback is called with the following arguments: event and eventKey</p>
          <h3>Props</h3>
        <p>The Dropdown expects at least one component with <code>bsRole="toggle"</code> and exactly one with <code>bsRole="menu"</code></p>

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
            <td></td>
            <td>Highlight the menu item as active.</td>
          </tr>
                     <tr>
            <td>bsClass</td>
            <td>string</td>
            <td>'dropdown'</td>
            <td>Base CSS class and prefix for the component. Generally one should only change bsClass to provide new, non-Bootstrap, CSS styles for a component.</td>
          </tr>
          <tr>
            <td>disabled</td>
            <td>boolean</td>
            <td>false</td>
            <td>Disable the menu item, making it unselectable.</td>
          </tr>
                      <tr>
            <td>divider</td>
            <td>all</td>
                        <td>false</td>
            <td>
Styles the menu item as a horizontal rule, providing visual separation between groups of menu items.</td>
          </tr>
          <tr>
            <td>eventKey</td>
            <td>any</td>
            <td></td>
            <td>Value passed to the onSelect handler, useful for identifying the selected menu item.</td>
          </tr>
                                <tr>
            <td>header</td>
            <td>boolean</td>
            <td>false</td>
            <td>Styles the menu item as a header label, useful for describing a group of menu items.</td>
          </tr>
           <tr>
               <td>href</td>
            <td>string</td>
            <td></td>
            <td>HTML href attribute corresponding to a.href.</td>
          </tr>
            <tr>
               <td>onClick</td>
            <td>function</td>
            <td></td>
              <td>Callback fired when the menu item is clicked.</td></tr>
                                <tr>
            <td>onSelect</td>
            <td>function</td>
            <td></td>
                                  <td>Callback fired when the menu item is selected. (eventKey: any, event: Object) => any</td>

          </tr>
          </tbody>
          </Table>
      </span>
    ))
