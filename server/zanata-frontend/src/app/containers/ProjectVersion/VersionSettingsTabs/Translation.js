/* eslint-disable */
// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import {Icon} from '../../../components'
import Checkbox from 'antd/lib/checkbox'
import 'antd/lib/checkbox/style/css'
import Radio from 'antd/lib/radio'
import 'antd/lib/radio/style/css'
import Alert from 'antd/lib/alert'
import 'antd/lib/alert/style/css'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import Card from 'antd/lib/card'
import 'antd/lib/card/style/css'

class Translation extends Component {
  render() {
    return (
      /* eslint-disable max-len */
      <div className='flexTab wideView'>
        <h1 className='txt-info'>
          <Icon name='translate' className='s5 v-sub mr2' />
          Translation
        </h1>
        <h2>Validation</h2>
        <p>Use these validations to keep translations consistent with the
          source text.</p>
        <Button size='small' type='primary'>Copy translation validation settings
          from project</Button>
        <h3>HTML/XML tags</h3>
        <p>Check that XML/HTML tags are consistent. More…</p>
        <Radio.Group>
          <Radio.Button>Off</Radio.Button>
          <Radio.Button className='btn-warn'>Warning</Radio.Button>
          <Radio.Button className='btn-danger'>Error</Radio.Button>
        </Radio.Group>
        <h3>Java variables</h3>
        <p>Check that java style variables are consistent. More…</p>
        <Radio.Group>
          <Radio.Button>Off</Radio.Button>
          <Radio.Button className='btn-warn'>Warning</Radio.Button>
          <Radio.Button className='btn-danger'>Error</Radio.Button>
        </Radio.Group>
        <h3>Leading/trailing newline</h3>
        <p>Check for consistent leading and trailing newline. More…</p>
        <Radio.Group>
          <Radio.Button>Off</Radio.Button>
          <Radio.Button className='btn-warn'>Warning</Radio.Button>
          <Radio.Button className='btn-danger'>Error</Radio.Button>
        </Radio.Group>
        <h3>Positional printf (XSI extension)</h3>
        <p>Check that positional printf style (%n$x) variables are consistent.
          More…</p>
        <Radio.Group>
          <Radio.Button>Off</Radio.Button>
          <Radio.Button className='btn-warn'>Warning</Radio.Button>
          <Radio.Button className='btn-danger'>Error</Radio.Button>
        </Radio.Group>
        <h3>Printf variables</h3>
        <p>Check that printf style (%x) variables are consistent. More…</p>
        <Radio.Group>
          <Radio.Button>Off</Radio.Button>
          <Radio.Button className='btn-warn'>Warning</Radio.Button>
          <Radio.Button className='btn-danger'>Error</Radio.Button>
        </Radio.Group>
        <h3>Tab characters</h3>
        <p>Check whether source and target have the same number of tabs. More …
        </p>
        <Radio.Group>
          <Radio.Button>Off</Radio.Button>
          <Radio.Button className='btn-warn'>Warning</Radio.Button>
          <Radio.Button className='btn-danger'>Error</Radio.Button>
        </Radio.Group>
        <h3>XML entity reference</h3>
        <p>Check that XML entities are complete. More…</p>
        <Radio.Group>
          <Radio.Button>Off</Radio.Button>
          <Radio.Button className='btn-warn'>Warning</Radio.Button>
          <Radio.Button className='btn-danger'>Error</Radio.Button>
        </Radio.Group>
      </div>
      /* eslint-enable max-len */
    )
  }
}

export default Translation
