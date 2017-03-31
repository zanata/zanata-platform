import React from 'react'
import { storiesOf } from '@kadira/storybook'
import { action, decorateAction } from '@kadira/storybook-addon-actions'
import { Modal, Button, Panel, Row, Table } from 'react-bootstrap'
import { Icon } from '../../../components'
import GlossaryTermModal from '.'

/*
* See .storybook/README.md for info on the component storybook.
*/
storiesOf('GlossaryTermModal', module)
  .addDecorator((story) => (
    <div className="static-modal">
      {story()}
    </div>
  ))
  .add('with 1 detail item', () => (
    <GlossaryTermModal
      show={true}
      close={action('close')}
      sourceLocale="en-US"
      targetLocale="de"
      term={{
        source: 'bat',
        target: 'schlagen'
      }}
      details={[
        {
          description: 'Take a turn at batting in a game of sportsball.',
          pos: 'Verb',
          transComment: 'It sounds a bit like "slugger" like someone might call a junior-league base kid ball.',
          lastModifiedTime: new Date(1490687578793)
        }
      ]}
    />
  ))
  .add('with 3 detail items', () => (
    <GlossaryTermModal
      show={true}
      close={action('close')}
      sourceLocale="en-US"
      targetLocale="de"
      term={{
        source: 'bat',
        target: 'schlagen'
      }}
      details={[
        {
          description: 'To hit something with a bat.',
          pos: 'Verb',
          transComment: "So I schlagged 'im.",
          lastModifiedTime: new Date(1490687563741)
        },{
          description: 'Take a turn at batting in a game of sportsball.',
          pos: 'Verb',
          transComment: 'It sounds a bit like "slugger" like someone might call a junior-league base kid ball.',
          lastModifiedTime: new Date(1490687578793)
        },{
          description: 'For one who is a bat, to hit something with themself.',
          pos: 'Norb',
          transComment: 'I did not just make it up, it is a real thing.',
          lastModifiedTime: new Date(409872360000)
        }
      ]}
    />
  ))
.add('hard-coded story', () => (
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
