/* eslint-disable */
// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import Input from 'antd/lib/input'
import 'antd/lib/input/style/css'
import {Icon} from '../../../components'
import Form from 'antd/lib/form'
import 'antd/lib/form/style/css'
import Row from 'antd/lib/row'
import 'antd/lib/row/style/css'
import Select from 'antd/lib/select'
import 'antd/lib/select/style/css'
import Collapse from 'antd/lib/collapse'
import 'antd/lib/collapse/style/css'
import Checkbox from 'antd/lib/checkbox'
import 'antd/lib/checkbox/style/css'

const FormItem = Form.Item
const Option = Select.Option
const Panel = Collapse.Panel

class Webhooks extends Component {

  render() {
    const addWebhookContent = (
      <Form layout='horizontal'>
        <Row>
          <FormItem label='Payload URL'>
            <Input />
          </FormItem>
        </Row>
        <Row>
          <FormItem label='Webhook name'>
            <Input />
          </FormItem>
        </Row>
        <Row>
          <FormItem label='Secret'>
            <Input />
          </FormItem>
        </Row>
        <h3>Type</h3>
        <p>
          <Checkbox>Translation milestone</Checkbox><br />
          <span className='f6'>A document is 100% translated or approved</span>
        </p>
        <p>
          <Checkbox>Translation update</Checkbox><br />
          <span className='f6'>Translations are updated (singly or in a batch)</span>
        </p>
        <p>
          <Checkbox>Project version</Checkbox><br />
          <span className='f6'>Project version is created or removed</span>
        </p>
        <p>
          <Checkbox>Project maintainer update</Checkbox><br />
          <span className='f6'>Project maintainer is added or removed</span>
        </p>
        <p>
          <Checkbox>Document</Checkbox><br />
          <span className='f6'>Source document is added or removed</span>
        </p>
        <p>
          <Checkbox>Manual event</Checkbox><br />
          <span className='f6'>An event that can be triggered manually</span>
        </p>
      </Form>
    )
    return (
      /* eslint-disable max-len */
      <div className='flexTab wideView permissions'>
        <h1 className='txt-info'><Icon name='code' className='s5 v-sub mr2' />
          <span className='fw4'>Webhooks</span></h1>
        <Collapse defaultActiveKey={'1'}>
          <Panel header="Add webhook" key="1">
            <h2>New webhook</h2>
            {addWebhookContent}
            <Row>
              <Button disabled className='mr3'>Test webhook</Button>
              <Button type='primary'>Save webhook</Button>
            </Row>
          </Panel>
        </Collapse>
      </div>
      /* eslint-enable max-len */
    )
  }
}

export default Webhooks
