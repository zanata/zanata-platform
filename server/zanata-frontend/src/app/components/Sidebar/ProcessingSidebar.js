import React from 'react'
import { Component } from 'react'
import Layout from 'antd/lib/layout'
import 'antd/lib/layout/style/css'
import Card from 'antd/lib/card'
import 'antd/lib/card/style/css'
import Progress from 'antd/lib/progress'
import 'antd/lib/progress/style/css'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'

class ProcessingSidebar extends Component {
  render () {
    return (
      <Layout>
        <Card title='Task title'>
          Processing document 1 of 10
          <Progress percent={30} showInfo />
          <Button type='danger' className='btn-danger' size='small'>Stop</Button>
        </Card>
      </Layout>
    )
  }
}

export default ProcessingSidebar
