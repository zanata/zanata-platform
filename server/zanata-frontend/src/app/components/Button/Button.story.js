/* global jest */
// @ts-nocheck
import React from 'react'
import { storiesOf } from '@storybook/react'
import Button from 'antd/lib/button'
import Layout from 'antd/lib/layout'

jest.mock('antd/lib/button', () => 'Button')

storiesOf('Button', module)
    .add('default', () => (
      <Layout>
        <Button type='primary'>Primary style</Button>
      </Layout>
    ))
