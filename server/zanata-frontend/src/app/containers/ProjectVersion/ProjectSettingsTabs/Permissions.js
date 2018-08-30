/* eslint-disable */
// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import Checkbox from 'antd/lib/checkbox'
import 'antd/lib/checkbox/style/css'
import Row from 'antd/lib/row'
import 'antd/lib/row/style/css'
import Input from 'antd/lib/input'
import 'antd/lib/input/style/css'
import {Icon} from '../../../components'
import Table from 'antd/lib/table'
import 'antd/lib/table/style/css'

const Search = Input.Search

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
    <Icon name='email' className='s1 v-sub' /> me@email.com
  </span>
)

const dataSource = [{
  key: '1',
  name: name,
  contact: lastactive

}, {
  key: '2',
  name: name,
  contact: lastactive
}]

const columns = [{
  title: '',
  dataIndex: 'name',
  key: 'name',
}, {
  title: '',
  dataIndex: 'contact',
  key: 'contact',
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

class Permissions extends Component {

  render() {

    return (
      /* eslint-disable max-len */
      <div className='flexTab wideView permissions'>
        <h1 className='txt-info'><Icon name='users' className='s5 v-sub mr2' />
          <span className='fw4'>Permissions</span></h1>
        <h2>Maintainers</h2>
        <Table pagination={pagination} dataSource={dataSource} columns={columns} />
        <Row className='mb4'>
          <h3 className='mt4'>Add a maintainer</h3>
          <Search
            placeholder="Search users"
            enterButton
          />
        </Row>
        <Row>
          <Checkbox>Restrict access to certain user roles?</Checkbox>
        </Row>
      </div>
      /* eslint-enable max-len */
    )
  }
}

export default Permissions
