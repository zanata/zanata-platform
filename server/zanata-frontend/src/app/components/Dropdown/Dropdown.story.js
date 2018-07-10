// @ts-nocheck
import React from 'react'
import { action } from '@storybook/addon-actions'
import { storiesOf } from '@storybook/react'
import {DropdownButton, MenuItem, Table, Label, Well} from 'react-bootstrap'

storiesOf('Dropdown', module)
    .add('default', () => (
        <React.Fragment>
          <h2><img src="https://i.imgur.com/v4qLk4p.png" width="42px" />Dropdown</h2>
          <Well bsSize="large">Use dropdowns where radio buttons
            would not make sense due to too many options (a
            maximum of 7 to 15).</Well>
          <DropdownButton bsStyle='default' title='Dropdown button'
                          id='dropdown-basic'>
            <MenuItem onClick={action('onClick')} eventKey='1'>
              Action</MenuItem>
            <MenuItem onClick={action('onClick')} eventKey='2'>
              Another action</MenuItem>
            <MenuItem onClick={action('onClick')} eventKey='3' active>
              Active Item</MenuItem>
          </DropdownButton>
          <hr />
          <h3>Props</h3>
          <p>The <code>Dropdown</code> expects at least one component
            with <code>bsRole="toggle"</code> and exactly one
            with <code>bsRole="menu"</code></p>
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
              <td>one of: <code>"lg"</code>, <code>"large"</code>, <code>"sm"</code>, <code>"small"</code>, <code>"xs"</code>, <code>"xsmall"</code></td>
              <td></td>
              <td>Component size variations</td>
            </tr>
            <tr>
              <td>bsClass</td>
              <td>string</td>
              <td>'btn'</td>
              <td>Base CSS class and prefix for the component. Generally one
                should only change bsClass to provide new, non-Bootstrap, CSS
                styles for a component.
              </td>
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
              <td>The menu will open above the dropdown button, instead of below
                it.
              </td>
            </tr>
            <tr>
              <td>id <Label>required</Label></td>
              <td>string|number</td>
              <td>An html id attribute, necessary for assistive technologies,
                such as screen readers.
              </td>
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
                <pre><code>(eventKey: any, event: Object) => any</code></pre></td>
            </tr>
            <tr>
              <td>onToggle</td>
              <td>function</td>
              <td></td>
              <td>A callback fired when the Dropdown wishes to change
                visibility. Called with the requested open value, the DOM event,
                and the source that fired it: <pre><code>'click','keydown','rootClose',
                  or 'select''</code></pre></td>
            </tr>
            <tr>
              <td>open</td>
              <td>boolean</td>
              <td></td>
              <td> controlled by: <code>onToggle</code>, <br />initial prop: <code>defaultOpen</code><br />
                Whether or not the Dropdown is visible.
              </td>
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
              <td>If <code>'menuitem'</code>, causes the dropdown to behave like a menu item
                rather than a menu button.
              </td>
            </tr>
            <tr>
              <td>rootCloseEvent</td>
              <td>one of: <code>'click'</code>, <code>'mousedown'</code></td>
              <td></td>
              <td>Which event when fired outside the component will cause it to
                be closed
              </td>
            </tr>
            <tr>
              <td>title <Label>required</Label></td>
              <td>node</td>
              <td></td>
              <td></td>

            </tr>
            </tbody>
          </Table>
          <hr />
          <h2>MenuItem</h2>
          <p>This component represents a menu item in a <code>Dropdown</code>.</p>

          <p>It supports the basic anchor properties href, target, title.</p>
          <p>It also supports different properties of the normal Bootstrap
            MenuItem.</p>
          <ul>
            <li><code>header</code>: To add a header label to sections</li>
            <li><code>divider</code>: Adds an horizontal divider between sections</li>
            <li><code>disabled</code>: shows the item as disabled, and prevents onSelect from
              firing
            </li>
            <li><code>eventKey</code>: passed to the callback</li>
            <li><code>onSelect</code>: a callback that is called when the user clicks the
              item.
            </li>
          </ul>
          <p>The callback is called with the following arguments: <code>event</code> and
            <code>eventKey</code></p>
          <hr />
          <h3>Props</h3>
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
              <td>Base CSS class and prefix for the component. Generally one
                should only change bsClass to provide new, non-Bootstrap, CSS
                styles for a component.
              </td>
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
                Styles the menu item as a horizontal rule, providing visual
                separation between groups of menu items.
              </td>
            </tr>
            <tr>
              <td>eventKey</td>
              <td>any</td>
              <td></td>
              <td>Value passed to the <code>onSelect</code> handler, useful for identifying
                the selected menu item.
              </td>
            </tr>
            <tr>
              <td>header</td>
              <td>boolean</td>
              <td>false</td>
              <td>Styles the menu item as a header label, useful for describing
                a group of menu items.
              </td>
            </tr>
            <tr>
              <td>href</td>
              <td>string</td>
              <td></td>
              <td>HTML href attribute corresponding to <code>a.href</code>.</td>
            </tr>
            <tr>
              <td>onClick</td>
              <td>function</td>
              <td></td>
              <td>Callback fired when the menu item is clicked.</td>
            </tr>
            <tr>
              <td>onSelect</td>
              <td>function</td>
              <td></td>
              <td>Callback fired when the menu item is selected.
                <pre><code>(eventKey: any, event: Object) => any</code></pre>
              </td>
            </tr>
            </tbody>
          </Table>
        </React.Fragment>
    ))
