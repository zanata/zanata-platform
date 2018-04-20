// @ts-nocheck
import React from 'react'
import { storiesOf } from '@storybook/react'
import { action } from '@storybook/addon-actions'
import { Modal, Button } from 'antd'

storiesOf('Modal', module)
  .add('default', () => (
    <div>
      <Button type="primary" onClick={action('onClick')}>Open</Button>
      <Modal
        title="Basic Modal"
        visible
        onOk={action('onClick')}
        onCancel={action('onClick')}
      >
        <p>A modal with header, body, and set of actions in the footer.</p>
        <a href='https://ant.design/components/modal'>ant.design Modal</a>
      </Modal>
    </div>
  ))
