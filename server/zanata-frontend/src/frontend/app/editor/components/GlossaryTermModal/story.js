import React from 'react'
import { storiesOf } from '@kadira/storybook'
import { action, decorateAction } from '@kadira/storybook-addon-actions'
import { Modal } from 'zanata-ui'
/*
* See .storybook/README.md for info on the component storybook.
*/
storiesOf('GlossaryTermModal', module)
.add('default', () => (
  <Modal
    show
    className="suggestions-modal">
    <Modal.Header>
      <Modal.Title><small><span className="pull-left">
      Translation Memory Details</span></small></Modal.Title>
    </Modal.Header>
    <Modal.Body>
      Blah
    </Modal.Body>
  </Modal>
  )
)
