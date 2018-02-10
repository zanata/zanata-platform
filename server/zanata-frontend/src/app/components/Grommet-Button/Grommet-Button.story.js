import React from 'react'
import { storiesOf } from '@storybook/react'
import Button from 'grommet/components/Button'
import AddIcon from 'grommet/components/icons/base/Add'

function onClick() {
  alert('click')
}
storiesOf('Grommet-Button', module)
    .add('default', () => (
        <span>
          <Button
            onClick={() => onClick()} label='Test' />
        </span>
    ))
    .add('critical', () => (
        <span>
          <Button critical={true}
            onClick={() => onClick()} label='Test' />
        </span>
    ))
    .add('primary', () => (
        <span>
          <Button primary={true}
            onClick={() => onClick()} label='Test'/>
        </span>
    ))
    .add('secondary', () => (
        <span>
          <Button secondary={true}
            onClick={() => onClick()} label='Test'/>
        </span>
    ))
    .add('icon', () => (
        <span>
          <Button icon={<AddIcon />}
            onClick={() => onClick()} label='Test'/>
        </span>
    ))

