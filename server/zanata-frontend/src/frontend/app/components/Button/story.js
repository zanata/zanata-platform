import React from 'react'
import { storiesOf, action } from '@kadira/storybook'
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
      <Button bsStyle='primary' bsSize='large'>
        Large button</Button>
      <Button bsSize='large'>Large button</Button>
      <Button bsStyle='primary'>Default button</Button>
      <Button>Default button</Button>
      <Button bsStyle='primary' bsSize='small'>
      Small button</Button>
    <Button bsSize='small'>Small button</Button>
    <Button bsStyle='primary' bsSize='xsmall'>
    Extra small button</Button>
    <Button bsSize='xsmall'>Extra small button</Button>
    </ButtonToolbar>
  ))
    .add('group', () => (
        <ButtonGroup>
          <Button>Left</Button>
          <Button>Middle</Button>
          <Button>Right</Button>
        </ButtonGroup>
   ))
    .add('toolbar', () => (
        <ButtonToolbar>
          <ButtonGroup>
            <Button>1</Button>
            <Button>2</Button>
            <Button>3</Button>
            <Button>4</Button>
          </ButtonGroup>
          <ButtonGroup>
            <Button>5</Button>
            <Button>6</Button>
            <Button>7</Button>
          </ButtonGroup>
          <ButtonGroup>
            <Button>8</Button>
          </ButtonGroup>
        </ButtonToolbar>
    ))
    .add('nested', () => (
        <ButtonGroup>
          <Button>1</Button>
          <Button>2</Button>
          <DropdownButton title='Dropdown' id='bg-nested-dropdown'>
            <MenuItem eventKey='1'>Dropdown link</MenuItem>
            <MenuItem eventKey='2'>Dropdown link</MenuItem>
          </DropdownButton>
        </ButtonGroup>
    ))
