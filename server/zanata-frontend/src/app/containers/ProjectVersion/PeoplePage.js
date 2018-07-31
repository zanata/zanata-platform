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

const dataSource = [{
  key: '1',
  name: 'Mike',
  lastactive: 'today'

}, {
  key: '2',
  name: 'John',
  lastactive: 'today'
}]

const columns = [{
  title: 'Name',
  dataIndex: 'name',
  key: 'name',
}, {
  title: 'Last active',
  dataIndex: 'lastactive',
  key: 'lastactive',
}, {
  title: 'Action',
  key: 'action',
  render: (text, record) => (
    <span className='fr'>
      <Button type='primary'>Manage permissions</Button>
    </span>
  ),
}]

class PeoplePage extends Component {

  render() {

    return (
      /* eslint-disable max-len */
        <div className='flexTab wideView'>
          <h1 className='txt-info'><Icon name='users' className='s5 v-sub' />
            <span className='fw4'>People</span></h1>
          <Button type='primary' icon='plus' className='mb4'>Add someone</Button>
          <Row className='mb4'>
            <Col xs={24} sm={23} md={8} className='mr2'>
              <Search
                placeholder="input search text"
                enterButton
              />
            </Col>
            <Col xs={24} sm={6} md={8}>
              <span className='ml4'>
              <span className='mr2'>Show</span>
                <Select defaultValue="20" style={{ width: 60 }}>
                  <Option value="10">10</Option>
                  <Option value="20">20</Option>
                  <Option value="50">50</Option>
                  <Option value="100">100</Option>
                </Select>
              </span>
            </Col>
            <Col className='fr'>
              <Pagination
                total={85}
                pageSize={20}
                defaultCurrent={1}
              />
            </Col>
          </Row>
          <Table dataSource={dataSource} columns={columns} />
        </div>
        /* eslint-enable max-len */
    )
  }
}

export default PeoplePage
