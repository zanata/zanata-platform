import React from 'react'
import { Component } from 'react'
import Layout from 'antd/lib/layout'
import 'antd/lib/layout/style/css'
import Select from 'antd/lib/select'
import 'antd/lib/select/style/css'
import Menu from 'antd/lib/menu'
import 'antd/lib/menu/style/css'
import VersionProgress from './VersionProgress'
import Dropdown from 'antd/lib/dropdown'
import 'antd/lib/dropdown/style/css'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import {Icon} from '../../components'

const Option = Select.Option

// need boolean prop for showing processing panel

class VersionPanel extends Component {
  render () {
    const counts = {
      total: 20,
      approved: 3,
      translated: 8,
      needswork: 4,
      rejected: 1,
      untranslated: 4
    }
    const menu = (
      <Menu>
        <Menu.Item key="1">1st menu item</Menu.Item>
        <Menu.Item key="2">2nd menu item</Menu.Item>
        <Menu.Item key="3">3rd item</Menu.Item>
      </Menu>
    )
    return (
      <Layout>
        <h2 className='di mt3 txt-primary'>
          <Icon name='version' className='s2' /> VERSION
          <span className='fr'>
            <Select placeholder='master' style={{ width: 120 }}>
              <Option value='draft'>draft</Option>
              <Option value='proof'>proof</Option>
              <Option disabled value='master'>master</Option>
            </Select>
          </span>
        </h2>
        <Dropdown overlay={menu}>
          <Button style={{ marginLeft: 8 }}>
            Version tools <Icon name="chevron-down" className='s0 v-mid' />
          </Button>
        </Dropdown>
        <VersionProgress counts={counts} />
        <Menu>
          <Menu.Item key='1'>
            <span>Languages</span>
          </Menu.Item>
          <Menu.Item key='2'>
            <span>Documents</span>
          </Menu.Item>
          <Menu.Item key='3'>
            <span>Groups</span>
          </Menu.Item>
          <Menu.Item key='4'>
            <span>Version settings</span>
          </Menu.Item>
        </Menu>
      </Layout>
    )
  }
}

export default VersionPanel
