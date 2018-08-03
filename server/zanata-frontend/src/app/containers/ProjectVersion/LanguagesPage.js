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
import {Icon} from '../../components'
import Table from 'antd/lib/table'
import 'antd/lib/table/style/css'
import VersionProgress from '../../components/Sidebar/VersionProgress'

const Search = Input.Search
const Option = Select.Option

const counts = {
  total: 20,
  approved: 3,
  translated: 8,
  needswork: 4,
  rejected: 1,
  untranslated: 4
}

const name = (
  <span>
    <a href="">
      Japanese
    </a>
  </span>
)

const lastmodified = (
  <span className='txt-muted fr'>
    <Icon name='clock' className='s1 v-sub' /> Last modified today
  </span>
)

const translated = (
  <Row>
    <span className='txt-success'>60%
      <VersionProgress counts={counts}/>
    </span>
  </Row>
)

const dataSource = [{
  key: '1',
  name: name,
  translated: translated,
  lastmodified: lastmodified
}, {
  key: '2',
  name: name,
  translated: translated,
  lastmodified: lastmodified
}]

const columns = [{
  title: '',
  dataIndex: 'name',
  key: 'name',
}, {
  title: '',
  dataIndex: 'translated',
  key: 'translated',
}, {
  title: '',
  dataIndex: 'lastmodified',
  key: 'lastmodified'
}]

const pagination = { position: 'top' }

class LanguagesPage extends Component {

  render() {

    return (
      /* eslint-disable max-len */
      <div className='flexTab wideView people'>
        <h1 className='txt-info'>
          <span className='fw4'>Languages
            <span className='txt-neutral ml3'>master</span>
          </span>
        </h1>
        <Row className='mb4'>
          <Col xs={24} sm={23} md={10}>
            <Search
              placeholder="Search languages"
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
        <Table pagination={pagination} dataSource={dataSource} columns={columns} />
      </div>
      /* eslint-enable max-len */
    )
  }
}

export default LanguagesPage
