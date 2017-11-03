import React from 'react'
import { storiesOf, action } from '@storybook/react'
import { Button, ButtonToolbar, ButtonGroup,
  DropdownButton, MenuItem } from 'react-bootstrap'

storiesOf('Button', module)
    .add('default', () => (
        <Button onClick={action('onClick')}>
          Unstyled button
        </Button>
    ))
    .add('primary', () => (
        <Button bsStyle='primary' onClick={action('onClick')}>
          Primary button
        </Button>
    ))
    .add('info', () => (
        <Button bsStyle='info' onClick={action('onClick')}>
          Info button
        </Button>
    ))
    .add('warning', () => (
        <Button bsStyle='warning' onClick={action('onClick')}>
          Warning button
        </Button>
    ))
    .add('danger', () => (
        <Button bsStyle='danger' onClick={action('onClick')}>
          Danger button
        </Button>
    ))
    .add('success', () => (
        <Button bsStyle='success' onClick={action('onClick')}>
          Success button
        </Button>
    ))
    .add('sizes', () => (
    <ButtonToolbar>
      <Button bsStyle='primary' bsSize='large' onClick={action('onClick')}>
        Large button</Button>
      <Button bsSize='large' onClick={action('onClick')}>Large button</Button>
      <Button bsStyle='primary' onClick={action('onClick')}>
        Default button</Button>
      <Button onClick={action('onClick')}>Default button</Button>
      <Button bsStyle='primary' bsSize='small' onClick={action('onClick')}>
        Small button</Button>
      <Button bsSize='small' onClick={action('onClick')}>Small button</Button>
      <Button bsStyle='primary' bsSize='xsmall' onClick={action('onClick')}>
        Extra small button</Button>
      <Button bsSize='xsmall' onClick={action('onClick')}>Extra small button</Button>
    </ButtonToolbar>
  ))
    .add('group', () => (
        <ButtonGroup>
          <Button onClick={action('onClick')}>Left</Button>
          <Button onClick={action('onClick')}>Middle</Button>
          <Button onClick={action('onClick')}>Right</Button>
        </ButtonGroup>
   ))
    .add('toolbar', () => (
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
    ))
    .add('nested', () => (
        <ButtonGroup>
          <Button onClick={action('onClick')}>1</Button>
          <Button onClick={action('onClick')}>2</Button>
          <DropdownButton onClick={action('onClick')}
            title='Dropdown' id='bg-nested-dropdown'>
            <MenuItem eventKey='1'>Dropdown link</MenuItem>
            <MenuItem eventKey='2'>Dropdown link</MenuItem>
          </DropdownButton>
        </ButtonGroup>
    ))
