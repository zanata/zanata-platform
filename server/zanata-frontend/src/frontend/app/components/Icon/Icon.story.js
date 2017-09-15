import React from 'react'
import { storiesOf } from '@storybook/react'
import { Icon } from '../'
import iconList from '../Icon/list'

function renderIcon (name, size) {
  return (
    <span key={name} title={name}> <Icon name={name} className={size} /> </span>
  )
}

function allIconsSize (size) {
  return iconList.map((name) => renderIcon(name, size))
}

storiesOf('Icon', module)
  .add('default', () => (
    <div>
      <h2>Size n2</h2>
      {allIconsSize('n2')}
      <h2>Size n1</h2>
      {allIconsSize('n1')}
      <h2>Size 0</h2>
      {allIconsSize('s0')}
      <h2>Size 1</h2>
      {allIconsSize('s1')}
      <h2>Size 2</h2>
      {allIconsSize('s2')}
      <h2>Size 3</h2>
      {allIconsSize('s3')}
      <h2>Size 4</h2>
      {allIconsSize('s4')}
    </div>
))
