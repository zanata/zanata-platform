import React from 'react'
import { storiesOf } from '@kadira/storybook'
import { action, decorateAction } from '@kadira/storybook-addon-actions'
import { Modal, Button, Panel, Row, Table } from 'react-bootstrap'
import { Icon } from '../../../components'

/*
* See .storybook/README.md for info on the component storybook.
*/
storiesOf('GlossaryTermModal', module)
  .addDecorator((story) => (
    <div className="static-modal">
      {story()}
    </div>
  ))
.add('default', () => (
    <Modal show
    onHide={action('close')}>
      <Modal.Header closeButton>
        <Modal.Title>Glossary details</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Panel className="split-panel">
          <h3>Source Term [en-US]:</h3>
          <span className="modal-term">cat</span>
        </Panel>
        <Panel className="split-panel">
          <h3>Target Term [ar-BH]:</h3>
          <span className="modal-term">caaat</span>
        </Panel>
        <br />
        <Panel className="gloss-details-panel">
          <Table className="gloss-details-table">
            <thead>
              <tr>
                <th></th>
                <th>Description</th>
                <th>Part of speech</th>
                <th>Target comment</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>#1</td>
                <td>The animal equivalent to the apathetic housemate</td>
                <td>Noun</td>
                <td>
                  <Icon name="comment" className="comment-icon n1"/>
                This is a comment that was left on the target term
                </td>
              </tr>
            </tbody>
          </Table>
        </Panel>
        <span className="pull-right u-textMeta">
          <Row>
            <Icon name="history" className="s0 history-icon" />
            <span className="u-sML-1-4">Last modified on DATEY TIMEY</span>
          </Row>
        </span>
      </Modal.Body>
     </Modal>
  )
)
