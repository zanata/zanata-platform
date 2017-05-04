import React from 'react'
import { storiesOf } from '@kadira/storybook'
import { action, decorateAction } from '@kadira/storybook-addon-actions'
import { Button, Panel, Row, Table } from 'react-bootstrap'
import { Icon, Modal } from '../../components'
import Lorem from 'react-lorem-component'

storiesOf('Modal', module)
    .addDecorator((story) => (
          <div className="static-modal">
            {story()}
          </div>
    ))
    .add('default', () => (
        <Modal
            show={true}
            onHide={action('onHide')}>
          <Modal.Header>
            <Modal.Title>Modal heading</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <Lorem />
          </Modal.Body>
          <Modal.Footer>
          <span className='bootstrap pull-right'>
            <Row>
              <Button bsStyle='link'
                className='btn-left'
                onClick={action('onClick')}>
                Close
              </Button>
              <Button
                  bsStyle='primary'
                  onClick={action('onClick')}>
                Save
              </Button>
            </Row>
          </span>
          </Modal.Footer>
        </Modal>
    ))
