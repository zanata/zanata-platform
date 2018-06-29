import React from 'react'
import { Component } from 'react'
import Modal from 'antd/lib/modal'
import 'antd/lib/modal/style/css'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import Alert from 'antd/lib/alert'
import 'antd/lib/alert/style/css'
import Icon from 'antd/lib/icon'
import 'antd/lib/icon/style/css'
// import Select from 'antd/lib/select'
import 'antd/lib/select/style/css'
import Checkbox from 'antd/lib/checkbox'
import 'antd/lib/checkbox/style/css'
import Switch from 'antd/lib/switch'
import 'antd/lib/switch/style/css'
import Card from 'antd/lib/card'
import 'antd/lib/card/style/css'
// import { LocaleId } from '../../utils/prop-types-util'

// const Option = Select.Option
const CheckboxGroup = Checkbox.Group
// TODO use LocaleType and Locale interface
const plainOptions = [
  'Chinese',
  'Dutch',
  'English',
  'German',
  'Japanese',
  'Russian',
  'Spanish',
  'Laotian',
  'Slovenian',
  'Fijian'
]
const defaultCheckedList = ['Dutch', 'Slovenian']

interface MTMergeState {
  visible: boolean
  // TODO checkedList should use LocaleId, not display name
  checkedList: string[]
  indeterminate: boolean
  checkAll: boolean
}

class MTMerge extends Component<{}, MTMergeState> {
  state = {
    visible: false,
    checkedList: defaultCheckedList,
    indeterminate: true,
    checkAll: false
  }
  showModal = () => {
    this.setState({
      visible: true
    })
  }
  handleOk = (_: any) => {
    // console.log(_)
    this.setState({
      visible: false
    })
  }
  handleCancel = (_: any) => {
    // console.log(_)
    this.setState({
      visible: false
    })
  }
  render() {
    return (
      <div>
        <Button type="primary" className="btn-primary" onClick={this.showModal}>
          Open MTMerge modal
        </Button>
        <Modal
          title="MT Batch Merge"
          visible={this.state.visible}
          onOk={this.handleOk}
          onCancel={this.handleCancel}
        >
          <Alert message="Have you run TM Merge first?" type="warning" showIcon />
          <h3 className="txt-info mt4">
            <Icon type="global" /> Languages
          </h3>
          <div style={{ borderBottom: '1px solid #E9E9E9' }}>
            <Checkbox
              indeterminate={this.state.indeterminate}
              onChange={this.onCheckAllChange}
              checked={this.state.checkAll}
            >
              All languages
            </Checkbox>
          </div>
          <br />
          <CheckboxGroup options={plainOptions} value={this.state.checkedList} onChange={this.onChange} />
          <div className="mt4 mb4">
            <Card hoverable>
              <h3 className="txt-info mb4">
                <span className="di">
                  <span className="mr2">Save as</span>
                  <Switch className="transSwitch" checkedChildren="translated" unCheckedChildren="fuzzy" />
                </span>
              </h3>
              <Switch size="small" />
              <span className="txt-primary"> Override existing fuzzy translations with MT</span>
            </Card>
          </div>
        </Modal>
      </div>
    )
  }
  onChange = (checkedList: any) => {
    this.setState({
      checkedList,
      indeterminate: !!checkedList.length && checkedList.length < plainOptions.length,
      checkAll: checkedList.length === plainOptions.length
    })
  }
  onCheckAllChange = (e: any) => {
    this.setState({
      checkedList: e.target.checked ? plainOptions : [],
      indeterminate: false,
      checkAll: e.target.checked
    })
  }
}
export default MTMerge
