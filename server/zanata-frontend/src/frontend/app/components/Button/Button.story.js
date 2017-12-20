import React from 'react'
import { storiesOf, action } from '@storybook/react'
import { Button, ButtonToolbar, ButtonGroup,
  DropdownButton, MenuItem, Table } from 'react-bootstrap'

storiesOf('Button', module)
    .add('default', () => (
        <span>
          <h2>Buttons</h2>
        <Button onClick={action('onClick')}>
          Unstyled button
        </Button>
          <h3>Button spacing</h3>
<p>Because React doesn't output newlines between elements, buttons on the same line are displayed flush against each other. To preserve the spacing between multiple inline buttons, wrap your button group in ButtonToolbar.</p>
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
        </span>
    ))
    .add('primary', () => (
        <span>
        <h2>Primary button</h2>
        <Button bsStyle='primary' onClick={action('onClick')}>
          Primary button
        </Button>
          <p><strong>bsStyle:</strong> "primary"</p>
        </span>
    ))
    .add('info', () => (
        <span>
          <h2>Info button</h2>
        <Button bsStyle='info' onClick={action('onClick')}>
          Info button
        </Button>
          <p><strong>bsStyle:</strong> "info"</p>
        </span>
    ))
    .add('warning', () => (
        <span>
          <h2>Warning button</h2>
        <Button bsStyle='warning' onClick={action('onClick')}>
          Warning button
        </Button>
          <p><strong>bsStyle:</strong> "warning"</p>
        </span>
    ))
    .add('danger', () => (
        <span>
          <h2>Danger button</h2>
        <Button bsStyle='danger' onClick={action('onClick')}>
          Danger button
        </Button>
          <p><strong>bsStyle:</strong> "danger"</p>
        </span>
    ))
    .add('success', () => (
        <span>
          <h2>Success button</h2>
        <Button bsStyle='success' onClick={action('onClick')}>
          Success button
        </Button>
          <p><strong>bsStyle:</strong> "success"</p>
        </span>
    ))
    .add('block', () => (
        <span>
          <h2>Button block</h2>
        <Button block onClick={action('onClick')}>
          Button block
        </Button>
          <p><strong>block:</strong> true</p>
        </span>
    ))
    .add('sizes', () => (
        <span>
          <h2>Button sizes</h2>
          <ButtonGroup>
      <Button bsStyle='primary' bsSize='large' onClick={action('onClick')}>
        Large button</Button>
            <Button bsSize='large' onClick={action('onClick')}>Large button</Button>
          </ButtonGroup>
      <ButtonGroup>
          <Button bsStyle='primary' onClick={action('onClick')}>
        Default button</Button>
      <Button onClick={action('onClick')}>Default button</Button>
      </ButtonGroup>
          <ButtonGroup>
      <Button bsStyle='primary' bsSize='small' onClick={action('onClick')}>
        Small button</Button>
      <Button bsSize='small' onClick={action('onClick')}>Small button</Button>
          </ButtonGroup>
          <ButtonGroup>
      <Button bsStyle='primary' bsSize='xsmall' onClick={action('onClick')}>
        Extra small button</Button>
      <Button bsSize='xsmall' onClick={action('onClick')}>Extra small button</Button>
    </ButtonGroup>
          <p><strong>bsSize:</strong> "lg", "large", "sm", "small", "xs", "xsmall"</p>
        </span>
  ))
    .add('group', () => (
        <span>
        <h2>ButtonGroup</h2>
           <p><strong>Guideline:</strong> group a series of buttons together on a single line with the button group.</p>
<ButtonGroup>
          <Button onClick={action('onClick')}>Left</Button>
          <Button onClick={action('onClick')}>Middle</Button>
          <Button onClick={action('onClick')}>Right</Button>
        </ButtonGroup>
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
            <td>block</td>
            <td>boolean</td>
            <td>'false'</td>
            <td></td>
          </tr>
           <tr>
            <td>bsClass</td>
            <td>string</td>
            <td>'btn-group'</td>
            <td>Base CSS class and prefix for the component. Generally one should only change bsClass to provide new, non-Bootstrap, CSS styles for a component.</td>
          </tr>
                    <tr>
            <td>justified</td>
            <td>boolean</td>
            <td>'false'</td>
            <td></td>
          </tr>
                    <tr>
            <td>vertical</td>
            <td>boolean</td>
            <td>'false'</td>
            <td></td>
          </tr>
          </tbody>
          </Table>
        </span>
   ))
    .add('toolbar', () => (
        <span>
          <h2>Button toolbar</h2>
          <p><strong>Guideline:</strong> Combine sets of ButtonGroups into a ButtonToolbar for more complex components.</p>
        <ButtonToolbar>
          <ButtonGroup>
            <Button onClick={action('onClick')}>1</Button>
            <Button onClick={action('onClick')}>2</Button>
            <Button onClick={action('onClick')}>3</Button>
            <Button onClick={action('onClick')}>4</Button>
          </ButtonGroup>
          <ButtonGroup>
            <Button onClick={action('onClick')}>5</Button>
            <Button onClick={action('onClick')}>6</Button>
            <Button onClick={action('onClick')}>7</Button>
          </ButtonGroup>
          <ButtonGroup>
            <Button onClick={action('onClick')}>8</Button>
          </ButtonGroup>
        </ButtonToolbar>
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
            <td>bsClass</td>
            <td>string</td>
            <td>'btn-toolbar'</td>
            <td>Base CSS class and prefix for the component. Generally one should only change bsClass to provide new, non-Bootstrap, CSS styles for a component.</td>
          </tr>
          </tbody>
             </Table>
        </span>
    ))
    .add('nested', () => (
        <span>
          <h2>Nested ButtonGroup</h2>
          <p><strong>Guideline:</strong> for adding dropdowns to button groups</p>
        <ButtonGroup>
          <Button onClick={action('onClick')}>1</Button>
          <Button onClick={action('onClick')}>2</Button>
          <DropdownButton onClick={action('onClick')}
            title='Dropdown' id='bg-nested-dropdown'>
            <MenuItem eventKey='1'>Dropdown link</MenuItem>
            <MenuItem eventKey='2'>Dropdown link</MenuItem>
          </DropdownButton>
        </ButtonGroup>
        </span>
    ))
