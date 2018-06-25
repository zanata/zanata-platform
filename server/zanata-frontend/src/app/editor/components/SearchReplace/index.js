import React from 'react'
import { Component } from 'react'
import Modal from 'antd/lib/modal'
import 'antd/lib/modal/style/css'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import Select from 'antd/lib/select'
import 'antd/lib/select/style/css'
import Row from 'antd/lib/row'
import 'antd/lib/row/style/css'
import Col from 'antd/lib/col'
import 'antd/lib/col/style/css'
import Checkbox from 'antd/lib/checkbox'
import 'antd/lib/checkbox/style/css'
import Input from 'antd/lib/input'
import 'antd/lib/input/style/css'
import Card from 'antd/lib/card'
import 'antd/lib/card/style/css'
import Icon from 'antd/lib/icon'
import 'antd/lib/icon/style/css'
import Tag from 'antd/lib/tag'
import 'antd/lib/tag/style/css'
import Table from 'antd/lib/table'
import 'antd/lib/table/style/css'

const columns = [{
  title: 'Index',
  dataIndex: 'index',
}, {
  title: 'Source',
  dataIndex: 'source',
}, {
  title: 'Target',
  dataIndex: 'target',
}, {
  title: '',
  dataIndex: 'edit',
}]

const data = [];
for (let i = 0; i < 12; i++) {
  data.push({
    key: i,
    index: `${i}`,
    source: `text source`,
    target: <span>text <span className='highlight'>target</span></span>,
    edit: <Button className='btn-link fr'><Icon type='edit' className='f4' /></Button>
  });
}

const Option = Select.Option

class SearchReplace extends Component {
  state = {
    visible: false,
    selectedRowKeys: [], // Check here to configure the default column
    loading: false
  }
  showModal = () => {
    this.setState({
      visible: true
    });
  }
  handleOk = (e) => {
    console.log(e)
    this.setState({
      visible: false
    })
  }
  handleCancel = (e) => {
    console.log(e);
    this.setState({
      visible: false
    })
  }
  start = () => {
    this.setState({ loading: true });
    // ajax request after empty completing
    setTimeout(() => {
      this.setState({
        selectedRowKeys: [],
        loading: false,
      });
    }, 1000);
  }
  onSelectChange = (selectedRowKeys) => {
    console.log('selectedRowKeys changed: ', selectedRowKeys);
    this.setState({ selectedRowKeys });
  }
  render() {
    const { loading, selectedRowKeys } = this.state
    const rowSelection = {
      selectedRowKeys,
      onChange: this.onSelectChange,
    }
    const hasSelected = selectedRowKeys.length > 0
    return (
        <div>
          <Button type="primary" className='Button--invisible' icon="swap"
            onClick={this.showModal} />
          <Modal className='searchReplaceModal'
            title="Project-wide Search and Replace"
            visible={this.state.visible}
            onOk={this.handleOk}
            onCancel={this.handleCancel}>
            <Row>
              <span className="mr6">
                <span className="mr2">Search in:</span><Select defaultValue="source and target">
                  <Option value="source">source</Option>
                  <Option value="target">target</Option>
                  <Option value="source and target" disabled>source and target</Option>
                </Select>
              </span>
              <span className="mr4">
                <Checkbox>Match case</Checkbox>
              </span>
              <Checkbox checked>Require preview</Checkbox>
            </Row>
            <Row className="mt3 mb2">
              <Col span={23}>
              <Input
                placeholder="Search in project"
                prefix={<Icon type="search" style={{ color: 'rgba(84,102,119,1)' }} />}
              />
              </Col>
              <Col span={1}>
                <span className="icon-sub ml3">
                  <Icon type="cross" />
                </span>
              </Col>
            </Row>
            <Row className="mb2 mt2">
             <Col span={23}>
              <Input
                  placeholder="Replace in project"
                  prefix={<Icon type="swap" style={{ color: 'rgba(84,102,119,1)' }} />}
              />
             </Col>
              <Col span={1}>
                <span className="icon-sub ml3">
                  <Icon type="cross" />
                </span>
             </Col>
            </Row>
            <Row>
              <span className="fr mt2 mb4">
                <Button className='mr2 Button--secondary'>Search</Button>
                <Button type="primary" className='Button--secondary'>Replace</Button>
              </span>
            </Row>
            <Card className="searchReplaceResultsCard">
              <Row className="mb2">
                <Button size="small" icon="left" className="mr2 Button--invisible" />
                Document # of #
                <Button size="small" icon="right" className="ml2 mr4 Button--invisible" />
                <Tag># of # matching textflows selected</Tag>
                <span className="highlight fr f7">Matching textflows</span>
              </Row>
              <Row>
                <Col span={4}>
                  <h2 className="mt2 f4">Document name</h2>
                </Col>
                <Col className="docOptions">
                  <Button className="mr2 Button--invisible"><Icon type="eye" className="f4" /></Button>
                  <Button className="Button--invisible"><Icon type="search" className="f4" /></Button>
                </Col>
              </Row>
            </Card>
            <Table rowSelection={rowSelection} columns={columns} dataSource={data} />
          </Modal>
        </div>
    );
  }
}

export default SearchReplace
