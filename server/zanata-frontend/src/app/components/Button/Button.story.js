// @ts-nocheck
import React from 'react'
import { storiesOf } from '@storybook/react'
import Button from 'antd/lib/button'
import Layout from 'antd/lib/layout'

storiesOf('Button', module)
    .add('default (no test)', () => (
        <Layout>
          <Button type='primary'>Primary style</Button>
        </Layout>
    ))
