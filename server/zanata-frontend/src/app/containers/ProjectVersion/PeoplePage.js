/* eslint-disable */
// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import Col from 'antd/lib/col'
import 'antd/lib/col/style/css'
import Row from 'antd/lib/row'
import 'antd/lib/row/style/css'
import Input from 'antd/lib/input'
import 'antd/lib/input/style/css'
import Select from 'antd/lib/select'
import 'antd/lib/select/style/css'
import Pagination from 'antd/lib/pagination'
import 'antd/lib/pagination/style/css'
import {Icon} from '../../components'
import Table from 'antd/lib/table'
import 'antd/lib/table/style/css'

const Search = Input.Search
const Option = Select.Option

const name = (
  <span>
    <a href="">
      <img src="https://www.gravatar.com/avatar/6068e27b4b6e165ca7c81fbc5f7f866a?d=mm&r=G&s=115"
       className='avatar mr2'/> John
    </a>
  </span>
)

const lastactive = (
  <span className='txt-muted'>
    <Icon name='clock' className='s1 v-sub' /> Last active today
  </span>
)

const dataSource = [{
  key: '1',
  name: name,
  lastactive: lastactive

}, {
  key: '2',
  name: name,
  lastactive: lastactive
}]

const columns = [{
  title: '',
  dataIndex: 'name',
  key: 'name',
}, {
  title: '',
  dataIndex: 'lastactive',
  key: 'lastactive',
}, {
  title: '',
  key: 'action',
  render: (text, record) => (
    <span className='fr'>
      <Button type='primary'><Icon name='settings' className='s0 v-sub mr2'/>
        Manage permissions
      </Button>
    </span>
  ),
}]

const pagination = { position: 'top' }

class PeoplePage extends Component {

  render() {

    return (
      /* eslint-disable max-len */
        <div className='flexTab wideView'>
          <h1 className='txt-info'><Icon name='users' className='s5 v-sub' />
            <span className='fw4'>People</span></h1>
          <Button type='primary' icon='plus' className='mb4'>Add someone</Button>
          <Row className='mb4'>
            <Col xs={24} sm={23} md={14}>
              <Search
                placeholder="Search project members"
                enterButton
              />
            </Col>
            <Col className='fr'>
              <span className='mr2'>Show</span>
              <Select defaultValue="20" style={{ width: 60 }}>
                <Option value="10">10</Option>
                <Option value="20">20</Option>
                <Option value="50">50</Option>
                <Option value="100">100</Option>
              </Select>
            </Col>
          </Row>
          <Table pagination={pagination} dataSource={dataSource} columns={columns} />
        </div>
        /* eslint-enable max-len */
    )
  }
}

export default PeoplePage
