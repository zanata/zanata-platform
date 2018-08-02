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

const FormItem = Form.Item
const Option = Select.Option

class Webhooks extends Component {

  render() {

    return (
      /* eslint-disable max-len */
      <div className='flexTab wideView permissions'>
        <h1 className='txt-info'><Icon name='code' className='s5 v-sub mr2' />
          <span className='fw4'>Webhooks</span></h1>
        <h2>Add webhook</h2>
        <Form layout='horizontal'>
          <Row>
            <FormItem label='Payload URL'>
              <Input />
            </FormItem>
          </Row>
          <Row>
              <FormItem label='Type'>
                <Select defaultValue="1">
                  <Option value="1">DocumentMilestoneEvent - Trigger when a document is 100% translated or approved</Option>
                  <Option value="2">DocumentStatsEvent - Trigger when translations are updated (singly or in a batch)</Option>
                </Select>
              </FormItem>
            </Row>
            <Row>
              <FormItem label='Secret'>
                <Input />
              </FormItem>
          </Row>
        </Form>
        <Row>
          <Button type='primary'>Save webhook</Button>
        </Row>
      </div>
      /* eslint-enable max-len */
    )
  }
}

export default Webhooks
