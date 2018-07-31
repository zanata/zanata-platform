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
        <span className='mt2'>
          <Card hoverable>
            Processing document <span className='b'>1 of 10</span>
            <Progress percent={30} showInfo />
            <Button type='danger' className='btn-danger mt1' size='small'>
            Stop</Button>
          </Card>
        </span>
      </Layout>
    )
  }
}

export default ProcessingSidebar
