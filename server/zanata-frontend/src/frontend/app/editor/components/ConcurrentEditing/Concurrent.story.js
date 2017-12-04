import React from 'react'
import { storiesOf, action } from '@storybook/react'
import { Alert, Button, Image, Panel, Badge } from 'react-bootstrap'
import { Icon, Modal, EditableText } from '../../../components'
import DateAndTimeDisplay from '../DateAndTimeDisplay'

/*
 * See .storybook/README.md for info on the component storybook.
 */

const lastModifiedTime = new Date(2016, 12, 4, 2, 19)

storiesOf('Concurrent editing', module)
    .add('notification', () => (
        <Alert bsStyle='danger'>
          <Icon name='warning' className='s2' />
          <strong>Concurrent editing detected.</strong>
          &nbsp;
          <span className="alert-link">
          <a href="">Resolve merge conflicts</a> before continuing.
        </span>
        </Alert>
    ))
    .add('transunit items', () => (
        <span>
          <h1>TransUnit items</h1>
          <Image src="https://i.imgur.com/yQWlJaH.png" responsive />
          <h2>Concurrent user notice</h2>
          <Button title="Click should trigger onClick action"
                  onClick={action('onClick')}
                  className="EditorButton Button--link Button--small
                  Button--concurrent">
            <Icon name="user" className="n1" /> username
          </Button>
          <h2>Resolve merge conflict button</h2>
           <Button title="Click should trigger onClick action"
                   onClick={action('onClick')}
                   className="EditorButton Button--secondary u-rounded">
          Resolve conflict
        </Button>
      </span>
    ))
    .add('modal', () => (
    <Modal show={true}
           onHide={close}
           closeButton
           id="ConflictsModal">
      <Modal.Header>
        <Modal.Title><small><span className="u-pullLeft">
         Current conflicts</span></small></Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <p><a href="">Username</a> has saved a new version while you are editing. Please resolve conflicts.</p>
        <Panel>
          <p>Username created a Translated revision <Badge bsStyle='default'>latest</Badge></p>
          <EditableText
              className='editable textarea
              EditorInputGroup EditorInputGroup--outlined EditorInputGroup--rounded is-focused'
              maxLength={255}
              editable={true}
              editing={true}
              placeholder='Add a description…'
              emptyReadOnlyText='No description'>
            Test text
          </EditableText>
          <DateAndTimeDisplay dateTime={lastModifiedTime}
                              className="u-block small u-sMT-1-2 u-sPB-1-4
          u-textMuted u-textSecondary"/>
        </Panel>
      </Modal.Body>
    </Modal>
    ))
