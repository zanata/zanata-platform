import React from 'react'
import {storiesOf, action} from '@storybook/react'
import { Alert, Button, Image } from 'react-bootstrap'
import { Icon } from '../../../components'
import ConcurrentModal from '.'

/*
 * See .storybook/README.md for info on the component storybook.
 */

storiesOf('Concurrent editing', module)
    .add('notification', () => (
        <Alert bsStyle='danger'>
          <Icon name='warning' className='s2'/>
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
          <Image src="https://i.imgur.com/yQWlJaH.png" responsive/>
          <h2>Concurrent user notice</h2>
          <Button title="Click should trigger onClick action"
                  onClick={action('onClick')}
                  className="EditorButton Button--link Button--small
                  Button--concurrent">
            <Icon name="user" className="n1"/> username
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
      <ConcurrentModal />
    ))
