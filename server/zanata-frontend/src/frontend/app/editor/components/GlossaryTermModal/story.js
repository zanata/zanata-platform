import React from 'react'
import { storiesOf } from '@kadira/storybook'
import { action, decorateAction } from '@kadira/storybook-addon-actions'
import { Modal, Button, Panel } from 'react-bootstrap'
import { Icon } from '../../../components'
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
          <span>Source Term [en-US]:</span>
        </Panel>
        <Panel>
          <span>Target Term [ar-BH]:</span>
          <span className="comment-box">
            <h4 className="list-group-item-heading">Comments</h4>
            <ul className="list-inline">
              <li className="s1">
                <Icon name="comment" title="comment" className="n2"/></li>
              <li>'targetComment'</li>
            </ul>
          </span>
        </Panel>
      </Modal.Body>
     </Modal>
    </div>
  )
)
