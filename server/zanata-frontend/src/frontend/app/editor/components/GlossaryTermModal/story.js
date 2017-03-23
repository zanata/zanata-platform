import React from 'react'
import { storiesOf } from '@kadira/storybook'
import { action, decorateAction } from '@kadira/storybook-addon-actions'
import { Modal, Button, Panel } from 'react-bootstrap'
/*
* See .storybook/README.md for info on the component storybook.
*/
storiesOf('GlossaryTermModal', module)
.add('default', () => (
  <div className="static-modal">
    <Modal show
    onHide={action('close')}>
      <Modal.Header closeButton>
        <Modal.Title>Glossary details</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Panel>
          Source
        </Panel>
        <Panel>
          Target
        </Panel>
      </Modal.Body>
     </Modal>
    </div>
  )
)
