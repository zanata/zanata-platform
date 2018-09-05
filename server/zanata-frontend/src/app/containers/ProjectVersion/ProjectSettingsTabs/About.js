/* eslint-disable */
// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import Input from 'antd/lib/input'
import 'antd/lib/input/style/css'
import {Icon} from '../../../components'
import Row from 'antd/lib/row'
import 'antd/lib/row/style/css'
import Alert from 'antd/lib/alert'
import 'antd/lib/alert/style/css'

const { TextArea } = Input

class About extends Component {

  render() {

    return (
      /* eslint-disable max-len */
      <div className='flexTab wideView permissions'>
        <h1 className='txt-info'><Icon name='info' className='s5 v-sub mr2' />
          <span className='fw4'>About</span></h1>
        <Alert message="This section can be used to add notes for translators or other contributors to help answer questions or complete translations." type="info" showIcon />
        <Row  className='mt3' >
          <TextArea rows={4} placeholder='Installed from: git clone git://github.com/definite/ibus-chewing.git'/>
        </Row>
        <p>Notes are parsed as <a href="https://spec.commonmark.org/0.18/">CommonMark Markdown</a></p>
        <Row>
          <Button type='primary' className='mb3'>Save notes</Button>
        </Row>
        <hr />
        <h2 className='mt3'>Preview</h2>
        <p>Installed from: git clone git://github.com/definite/ibus-chewing.git
        </p>
      </div>
      /* eslint-enable max-len */
    )
  }
}

export default About
