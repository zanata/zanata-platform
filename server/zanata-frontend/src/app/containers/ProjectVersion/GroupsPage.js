/* eslint-disable */
// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import Col from 'antd/lib/col'
import 'antd/lib/col/style/css'
import Row from 'antd/lib/row'
import 'antd/lib/row/style/css'
import Input from 'antd/lib/input'
import 'antd/lib/input/style/css'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import Select from 'antd/lib/select'
import 'antd/lib/select/style/css'
import GroupsTable from './GroupsTable'

const Search = Input.Search
const Option = Select.Option

class GroupsPage extends Component {

  render() {

    return (
      /* eslint-disable max-len */
      <div className='flexTab wideView people'>
        <h1 className='txt-info'>
          <span className='fw4'>Groups
            <span className='txt-neutral ml3'>master</span>
          </span>
        </h1>
        <Button type='primary' icon='plus' className='mb4'>Add group</Button>
        <Row className='mb4'>
          <Col xs={24} sm={23} md={10}>
            <Search
              placeholder="Search groups"
              enterButton
            />
          </Col>
          <Col className='ml3' xs={10} sm={8}>
            <Select defaultValue="a" style={{ width: 180 }}>
              <Option value="a">Alphabetical</Option>
              <Option value="b">Last source update</Option>
            </Select>
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
        <GroupsTable />
      </div>
      /* eslint-enable max-len */
    )
  }
}

export default GroupsPage
