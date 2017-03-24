import React from 'react'
import { storiesOf } from '@kadira/storybook'
import { action, decorateAction } from '@kadira/storybook-addon-actions'
import { Modal, Button, Panel, Row } from 'react-bootstrap'
import { Icons, Icon } from '../../../components'
/*
* See .storybook/README.md for info on the component storybook.
*/
storiesOf('GlossaryTermModal', module)
.add('default', () => (
  <div className="static-modal">
    <Icons />
    <Modal show
    onHide={action('close')}>
      <Modal.Header closeButton>
        <Modal.Title>Glossary details</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Panel>
          <h3>Source Term [en-US]:</h3>
          <span className="modal-term">cat</span>
          <ul>
            <li className="list-group-item-heading">Description</li>
            <li>The animal equivalent to the apathetic housemate</li>
            <li className="list-group-item-heading">Part of speech</li>
            <li>Noun</li>
          </ul>
        </Panel>
        <Panel>
          <h3>Target Term [ar-BH]:</h3>
          <span className="modal-term">caaat</span>
          <br />
          <span className="comment-box">
            <span className="list-group-item-heading">Comments</span>
            <ul className="list-inline">
              <li className="s1">
                <Icon name="comment" title="comment" className="n2"/></li>
              <li>This is a comment that was left on the target term</li>
            </ul>
          </span>
        </Panel>
        <span className="pull-right u-textMeta">
          <Row>
            <Icon name="history" className="s0" />
            <span className="u-sML-1-4">Last modified on DATEY TIMEY</span>
          </Row>
        </span>
      </Modal.Body>
     </Modal>
    </div>
  )
)
