/* eslint-disable */
// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import {Icon} from '../../../components'
import Input from 'antd/lib/input'
import 'antd/lib/input/style/css'
import Row from 'antd/lib/row'
import 'antd/lib/row/style/css'
import Col from 'antd/lib/col'
import 'antd/lib/col/style/css'
import Table from 'antd/lib/table'
import 'antd/lib/table/style/css'
import Divider from 'antd/lib/divider'
import 'antd/lib/divider/style/css'

const columnsEnabled = [{
  title: 'Name',
  dataIndex: 'name',
  key: 'name',
  render: text => <a href="javascript:;">{text}</a>,
}, {
  title: 'Code',
  dataIndex: 'code',
  key: 'code',
}, {
  title: 'Alias',
  dataIndex: 'alias',
  key: 'alias',
}, {
  title: 'Action',
  key: 'action',
  render: (text, record) => (
    <span>
      <a href="javascript:;">Add alias</a>
      <Divider type="vertical" />
      <a href="javascript:;">Disable</a>
    </span>
  ),
}]

const dataEnabled = [{
  key: '1',
  name: 'Chinese (Simplified)',
  code: 'zh-Hans',
  alias: '',
}, {
  key: '2',
  name: 'French',
  code: 'fr',
  alias: '',
}, {
  key: '3',
  name: 'Spanish',
  code: 'es',
  alias: '',
}]

const columnsDisabled = [{
  title: 'Name',
  dataIndex: 'name',
  key: 'name',
  render: text => <a href="javascript:;">{text}</a>,
}, {
  title: 'Code',
  dataIndex: 'code',
  key: 'code',
}, {
  title: 'Alias',
  dataIndex: 'alias',
  key: 'alias',
}, {
  title: 'Action',
  key: 'action',
  render: (text, record) => (
    <span>
      <a href="javascript:;">Enable</a>
    </span>
  ),
}]

const dataDisabled = [{
  key: '1',
  name: 'English (American)',
  code: 'en-US',
  alias: '',
}]

const Search = Input.Search
const searchBox = (
  <span className='mb3'>
    <Search
      placeholder="Filter languages"
      enterButton
    />
  </span>
)

const pagination = false

class Languages extends Component {
  render() {
    return (
      /* eslint-disable max-len */
      <div className='flexTab wideView'>
        <h1 className='txt-info'><Icon name='language' className='s5 v-sub mr2' /> Languages</h1>
          <h3 className='txt-primary'>Enabled</h3>
          <Row>
            <Col span={8}>
              {searchBox}
            </Col>
          </Row>
          <Table columns={columnsEnabled} dataSource={dataEnabled} pagination={pagination}/>
          <h3 className='txt-primary'>Disabled</h3>
          <Row>
            <Col span={8}>
              {searchBox}
            </Col>
          </Row>
          <Table columns={columnsDisabled} dataSource={dataDisabled} pagination={pagination}/>
      </div>
      /* eslint-enable max-len */
    )
  }
}

export default Languages
