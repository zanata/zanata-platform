import * as React from 'react'
import {storiesOf} from '@storybook/react'
import {Icon} from '../'
const iconList = require('../Icon/list')
import {Table, Well} from 'react-bootstrap'

function renderIcon(name, size) {
  return (
      <span key={name} title={name}> <Icon name={name}
                                           className={size}/> </span>
  )
}

function allIconsSize(size) {
  return iconList.map((name) => renderIcon(name, size))
}

function renderNamesIcons(name, size) {
  return (
      <p key={name} title={name}><Icon name={name}
        className={size}/> {name}</p>
  )
}

function allIcons(size) {
  return iconList.map((name) => renderNamesIcons(name, size))
}

storiesOf('Icon', module)

    .add('default', () => (
        <span>
      <h2><img
          src="https://upload.wikimedia.org/wikipedia/commons/4/49/Zanata-Logo.svg"
          width="42px"/> Icons</h2>
          <Well bsSize="large">Icons for use throughout frontend. Use <code>className</code> prop to set size.</Well>
      <hr/>
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
          <td>name</td>
          <td>string</td>
          <td></td>
          <td>Icon name</td>
        </tr>
        <tr>
          <td>className</td>
          <td>string</td>
          <td></td>
          <td>Icon size
          </td>
        </tr>
        </tbody>
      </Table>
          <hr/>
          <span className="col-xs-12">
            <h3>Icon list</h3>
            {allIcons('s2')}</span>
          <code>className="s1" name=name</code>
    </span>
    ))

    .add('sizes', () => (
        <span>
      <h2><img
          src="https://upload.wikimedia.org/wikipedia/commons/4/49/Zanata-Logo.svg"
          width="42px"/> Icon sizes</h2>
      <h3>Size n2</h3>
          {allIconsSize('n2')}
          <p><code>className="n2" name=name</code></p>
          <h3>Size n1</h3>
          {allIconsSize('n1')}
          <p><code>className="n1" name=name</code></p>
          <h3>Size s0</h3>
          {allIconsSize('s0')}
          <p><code>className="s0" name=name</code></p>
          <h3>Size s1</h3>
          {allIconsSize('s1')}
          <p><code>className="s1" name=name</code></p>
          <h3>Size s2</h3>
          {allIconsSize('s2')}
          <p><code>className="s2" name=name</code></p>
          <h3>Size s3</h3>
          {allIconsSize('s3')}
          <p><code>className="s3" name=name</code></p>
          <h3>Size s4</h3>
          {allIconsSize('s4')}
          <p><code>className="s4" name=name</code></p>
        </span>
    ))
