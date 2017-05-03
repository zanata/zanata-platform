import React from 'react'
import { storiesOf, action } from '@kadira/storybook'
import { DropdownButton, MenuItem } from 'react-bootstrap'

storiesOf('Dropdowns', module)
    .add('default', () => (
      <DropdownButton bsStyle='default' title='Dropdown button'
        id='dropdown-basic'>
        <MenuItem eventKey='1'>Action</MenuItem>
        <MenuItem eventKey='2'>Another action</MenuItem>
        <MenuItem eventKey='3' active>Active Item</MenuItem>
        <MenuItem divider />
        <MenuItem eventKey='4'>Separated link</MenuItem>
      </DropdownButton>
    ))
