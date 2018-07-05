// @ts-nocheck
import React from 'react'
import { storiesOf } from '@storybook/react'
import Button from 'antd/lib/button'
import Layout from 'antd/lib/layout'
import 'antd/lib/button/style/css'
import 'antd/lib/layout/style/css'

storiesOf('Button', module)
    .add('default', () => (
        <Layout>
          <Button type='primary'>Primary style</Button>
        </Layout>
    ))
