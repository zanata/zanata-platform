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


class MTMerge extends Component {
  state = { visible: false }
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
  render() {
    return (
        <div>
          <Button type="primary" className='btn-primary' onClick={this.showModal}>Open MTMerge modal</Button>
          <Modal
              title="MT Batch Merge"
              visible={this.state.visible}
              onOk={this.handleOk}
              onCancel={this.handleCancel}>
            <Alert message="Warning" type="warning" showIcon />
            <h4 className='txt-info mt4'><Icon type="global" /> Languages</h4>
          </Modal>
        </div>
    );
  }
}

export default MTMerge
