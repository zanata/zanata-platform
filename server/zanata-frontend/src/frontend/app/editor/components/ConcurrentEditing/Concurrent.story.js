import React from 'react'
import { storiesOf, action } from '@storybook/react'
import { Alert, Button, Image, Panel, Label } from 'react-bootstrap'
import { Icon, Modal } from '../../../components'
import DateAndTimeDisplay from '../DateAndTimeDisplay'
import Textarea from 'react-textarea-autosize'

/*
 * See .storybook/README.md for info on the component storybook.
 */

const lastModifiedTime = new Date(2016, 12, 4, 2, 19)
const value = 'A wonderful serenity has taken possession of my entire soul, like these sweet mornings of spring which I enjoy with my whole heart.'

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
        <p className="u-sizeHeight-1_1-2"><a href="">Username</a> has saved a new version while you are editing. Please resolve conflicts.</p>
        <Panel>
          <p className="u-sizeHeight-1_1-2"><strong>Username</strong> created a <span className='u-textSuccess'>Translated</span> revision <Label bsStyle='default'>latest</Label></p>
          <span className="revisionBox">
            <Textarea
              className='form-control'
              value={value}
              placeholder={value}
              onFocus={action('onFocus')}
              onChange={action('onFocus')}
              onSelect={action('onSelect')} />
          </span>
          <span className='u-floatLeft'>
          <DateAndTimeDisplay dateTime={lastModifiedTime}
                              className="u-block small u-sMT-1-2 u-sPB-1-4
          u-textMuted u-textSecondary"/>
          </span>
          <span className='u-floatRight'>
            <Button title="Click should trigger onClick action"
                    onClick={action('onClick')}
                    className="EditorButton Button--secondary u-rounded">Use latest
            </Button>
          </span>
        </Panel>
        <Panel>
          <p className="u-sizeHeight-1_1-2">You created an Unsaved revision.</p>
          <span className="revisionBox">
            <Textarea
                className='form-control'
                value={value}
                placeholder={value}
                onFocus={action('onFocus')}
                onChange={action('onFocus')}
                onSelect={action('onSelect')} />
          </span>
          <span className='u-floatLeft'>
          <DateAndTimeDisplay dateTime={lastModifiedTime}
                              className="u-block small u-sMT-1-2 u-sPB-1-4
          u-textMuted u-textSecondary"/>
          </span>
          <span className='u-floatRight'>
            <Button title="Click should trigger onClick action"
                    onClick={action('onClick')}
                    className="EditorButton Button--primary u-rounded">Use original
            </Button>
          </span>
        </Panel>
      </Modal.Body>
    </Modal>
    ))
