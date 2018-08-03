/* eslint-disable */
// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import Row from 'antd/lib/row'
import 'antd/lib/row/style/css'
import Col from 'antd/lib/col'
import 'antd/lib/col/style/css'
import Input from 'antd/lib/input'
import 'antd/lib/input/style/css'
import {Icon} from '../../../components'
import Table from 'antd/lib/table'
import 'antd/lib/table/style/css'
import Select from 'antd/lib/select'
import 'antd/lib/select/style/css'

const Search = Input.Search
const Option = Select.Option

const name = (
  <span>
    <a href="">Readme.txt</a>
  </span>
)

const uploaded = (
  <span className='txt-muted'>
    <Icon name='upload' className='s1 v-sub' /> 1 month ago
  </span>
)

const dataSource = [{
  key: '1',
  name: name,
  uploaded: uploaded

}, {
  key: '2',
  name: name,
  uploaded: uploaded
}]

const columns = [{
  title: '',
  dataIndex: 'name',
  key: 'name',
}, {
  title: '',
  dataIndex: 'uploaded',
  key: 'uploaded',
}, {
  title: '',
  key: 'action',
  render: (text, record) => (
    <span className='fr'>
      <Button className='close'>
        <Icon name='cross' className='s0 v-sub mr2'/>
      </Button>
    </span>
  ),
}]

const pagination = { position: 'none' }

class Documents extends Component {

  render() {

    return (
      /* eslint-disable max-len */
      <div className='flexTab wideView documents'>
        <h1 className='txt-info'><Icon name='document' className='s5 v-sub mr2' />
          <span className='fw4'>Documents</span></h1>
        <h2>Source documents</h2>
        <Row className='mb3'>
          <Col span={12}>
            <Button type='primary' icon='plus'>Upload new source document</Button>
          </Col>
          <Col className='fr'>
            <Select defaultValue="20" style={{ width: 60 }}>
              <Option value="10">10</Option>
              <Option value="20">20</Option>
              <Option value="50">50</Option>
              <Option value="100">100</Option>
            </Select>
          </Col>
        </Row>
        <Row className='mb3'>
          <Col span={12}>
            <Search
              placeholder="Filter languages"
              enterButton
            />
          </Col>
        </Row>
        <Row>
          <Table pagination={pagination} dataSource={dataSource} columns={columns} />
        </Row>
      </div>
      /* eslint-enable max-len */
    )
  }
}

export default Documents
