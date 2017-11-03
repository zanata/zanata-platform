import React from 'react'
import { storiesOf, action } from '@storybook/react'
import { DropdownButton, MenuItem } from 'react-bootstrap'

storiesOf('Dropdown', module)
    .add('default', () => (
      <DropdownButton bsStyle='default' title='Dropdown button'
        id='dropdown-basic'>
        <MenuItem onClick={action('onClick')} eventKey='1'>
          Action</MenuItem>
        <MenuItem onClick={action('onClick')} eventKey='2'>
          Another action</MenuItem>
        <MenuItem  onClick={action('onClick')}eventKey='3' active>
          Active Item</MenuItem>
      </DropdownButton>
    ))
