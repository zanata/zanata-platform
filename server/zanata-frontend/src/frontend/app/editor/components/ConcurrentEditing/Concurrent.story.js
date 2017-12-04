import React from 'react'
import { storiesOf } from '@storybook/react'
import { Alert } from 'react-bootstrap'
import Icon from '../../../components/Icon'

/*
 * See .storybook/README.md for info on the component storybook.
 */
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
