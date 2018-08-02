import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
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
  static propTypes = {
    active: PropTypes.string.isRequired,
    onSelect: PropTypes.func.isRequired
  }
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
        <Menu.Item key="1">Copy translations</Menu.Item>
        <Menu.Item key="2">Merge translations</Menu.Item>
        <Menu.Item key="3">Merge Translation Memory</Menu.Item>
        <Menu.Item key="4">Merge Machine Translations</Menu.Item>
        <Menu.Item key="5">Copy to new version</Menu.Item>
        <Menu.Item key="6">Download config</Menu.Item>
        <Menu.Item key="7">Export version to TMX</Menu.Item>
      </Menu>
    )
    return (
      <Layout>
        <div className='ml1 mt4'>
          <h2 className='di txt-primary'>
            <Icon name='version' className='s2' /> VERSION
            <span className='ml3'>
              <Select placeholder='master' style={{ width: 120 }}>
                <Option value='draft'>draft</Option>
                <Option value='proof'>proof</Option>
                <Option value='master'>master</Option>
              </Select>
            </span>
          </h2>
          <span className='txt-success'>
            <span className='f4'>60%</span> translated
          </span>
          <VersionProgress counts={counts} />
          <Dropdown overlay={menu}>
            <Button className='mt2 mb3'>
              Version tools <Icon name="chevron-down" className='s0 v-mid' />
            </Button>
          </Dropdown>
          <Menu selectedKeys={this.props.active} onClick={this.props.onSelect}>
            <Menu.Item key='4'>
              <span>Languages</span>
            </Menu.Item>
            <Menu.Item key='5'>
              <span>Documents</span>
            </Menu.Item>
            <Menu.Item key='6'>
              <span>Groups</span>
            </Menu.Item>
            <Menu.Item key='7'>
              <span>Version settings</span>
            </Menu.Item>
          </Menu>
        </div>
      </Layout>
    )
  }
}

export default VersionPanel
