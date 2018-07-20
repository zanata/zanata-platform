import React from 'react'
import { Component } from 'react'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import Layout from 'antd/lib/layout'
import 'antd/lib/layout/style/css'
import Select from 'antd/lib/select'
import 'antd/lib/select/style/css'
import {Icon} from '../../components'
import Menu from 'antd/lib/menu'
import 'antd/lib/menu/style/css'
import VersionProgress from './VersionProgress'

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
        <Button type="primary" size="small" className="w-80 center">
          Version settings
        </Button>
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
        </Menu>
      </Layout>
    )
  }
}

export default VersionPanel
