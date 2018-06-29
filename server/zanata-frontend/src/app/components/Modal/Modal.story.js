import React from 'react'
import { storiesOf } from '@storybook/react'
import { action } from '@storybook/addon-actions'
import Modal from 'antd/lib/modal'
import MTMerge from '../../containers/ProjectVersion/MTMerge'
// import 'antd/lib/modal/style/index.less'
// import Button from 'antd/lib/button'
// import 'antd/lib/button/style/index.less'

storiesOf('Modal', module)
  .add('default', () => (
    <Modal
      title="Basic Modal"
      visible
      onOk={action('onClick')}
      onCancel={action('onClick')}
    >
      <p>A modal with header, body, and set of actions in the footer.</p>
      <a href='https://ant.design/components/modal'>ant.design Modal</a>
    </Modal>
  ))
    .add('MTMerge', () => (
      <MTMerge />
    ))
