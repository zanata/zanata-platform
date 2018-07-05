// @ts-nocheck
import React from 'react'
import { action } from '@storybook/addon-actions'
import { storiesOf } from '@storybook/react'
import Alert from 'antd/lib/alert'
import 'antd/lib/alert/style/css'
import { Icon } from '../../../components'
import ConcurrentModal from '.'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'

/*
 * See .storybook/README.md for info on the component storybook.
 */

storiesOf('Concurrent editing', module)
    .add('notification', () => (
        <Alert type='danger'>
          <Icon name='warning' className='s2'/>
          <strong>Concurrent editing detected.</strong>
          &nbsp;
          <span className="alert-link">
          <a href="">Resolve merge conflicts</a> before continuing.
        </span>
        </Alert>
    ))
    .add('transunit items', () => (
        /* eslint-disable max-len */
        <span>
          <h1>TransUnit items</h1>
          <img src="https://i.imgur.com/yQWlJaH.png" />
          <h2>Concurrent user notice</h2>
          <Button
            onClick={action('onClick')}
            className="EditorButton Button--link Button--small Button--concurrent">
            <Icon name="user" className="n1"/> username
          </Button>
          <h2>Resolve merge conflict button</h2>
           <Button
             onClick={action('onClick')}
             className="EditorButton Button--secondary u-rounded">
          Resolve conflict
        </Button>
      </span>
      /* eslint-enable max-len */
    ))
    .add('modal', () => (
      <ConcurrentModal />
    ))
