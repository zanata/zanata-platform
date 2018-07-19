import React from 'react'
import { Component } from 'react'
import Layout from 'antd/lib/layout'
import 'antd/lib/layout/style/css'
import Select from 'antd/lib/select'
import 'antd/lib/select/style/css'
import {Icon} from '../../components'
import Progress from 'antd/lib/progress'
import 'antd/lib/progress/style/css'
import Menu from 'antd/lib/menu'
import 'antd/lib/menu/style/css'

const Option = Select.Option

class VersionPanel extends Component {
  render () {
    return (
      <Layout>
        <span className='di'>
          <Icon name='version' className='s2' /> VERSION
          <Select placeholder='master' style={{ width: 120 }}>
            <Option value='draft'>draft</Option>
            <Option value='proof'>proof</Option>
            <Option disabled value='master'>master</Option>
          </Select>
        </span>
        <a>Version settings</a>
        <Progress percent={30} showInfo />
        <Menu>
          <Menu.Item key='1'>
            <span className='nav-text'>Languages</span>
          </Menu.Item>
          <Menu.Item key='2'>
            <span className='nav-text'>Documents</span>
          </Menu.Item>
          <Menu.Item key='3'>
            <span className='nav-text'>Groups</span>
          </Menu.Item>
        </Menu>
      </Layout>
    )
  }
}

export default VersionPanel
