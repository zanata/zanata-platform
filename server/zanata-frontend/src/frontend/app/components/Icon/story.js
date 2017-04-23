import React from 'react'
import { storiesOf, action } from '@kadira/storybook'
import { Icon } from '../'
import iconList from '../Icon/list'

const allIconNames = iconList

function renderIcon (name, size) {
  return <span title={name}> <Icon name={name} className={size} /> </span>
}

const sizen2Icons = allIconNames.map((name) => renderIcon(name, 'n2'))
const sizen1Icons = allIconNames.map((name) => renderIcon(name, 'n1'))
const size0Icons = allIconNames.map((name) => renderIcon(name, 's0'))
const size1Icons = allIconNames.map((name) => renderIcon(name, 's1'))
const size2Icons = allIconNames.map((name) => renderIcon(name, 's2'))
const size3Icons = allIconNames.map((name) => renderIcon(name, 's3'))
const size4Icons = allIconNames.map((name) => renderIcon(name, 's4'))

storiesOf('Icon', module)
    .add('default', () => (
    <div>
      <h2>Size n2</h2>
      {sizen2Icons}
      <h2>Size n1</h2>
      {sizen1Icons}
      <h2>Size 0</h2>
      {size0Icons}
      <h2>Size 1</h2>
      {size1Icons}
      <h2>Size 2</h2>
      {size2Icons}
      <h2>Size 3</h2>
      {size3Icons}
      <h2>Size 4</h2>
      {size4Icons}
    </div>
))




