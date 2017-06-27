import React from 'react'
import { storiesOf } from '@kadira/storybook'
import { action, decorateAction } from '@kadira/storybook-addon-actions'
import ActTabActSelect from '.'
import { Button, ButtonToolbar } from 'react-bootstrap'
import Icon from '../../../components/Icon'

/*
 * See .storybook/README.md for info on the component storybook.
 */
storiesOf('ActTabActSelect', module)
    .add('default', () => (
      <div>
        <ButtonToolbar>
          <Button onClick={action('onClick')}
            className="Button Button--small u-rounded Button--secondary
            is-active">
            <Icon name="clock" className="n1" /> All
          </Button>
          <Button onClick={action('onClick')}
            className="Button Button--small u-rounded Button--secondary">
            <Icon name="comment" className="n1" /> Comments
          </Button>
          <Button onClick={action('onClick')}
            className="Button Button--small u-rounded Button--secondary">
            <Icon name="refresh" className="n1" /> Updates
          </Button>
        </ButtonToolbar>
      </div>
    ))

  .add('Comments only', () => (
    <div>
      <ButtonToolbar>
        <Button onClick={action('onClick')}
          className="Button Button--small u-rounded Button--secondary">
          <Icon name="clock" className="n1" /> All
        </Button>
        <Button onClick={action('onClick')}
          className="Button Button--small u-rounded Button--secondary
          is-active">
          <Icon name="comment" className="n1" /> Comments
        </Button>
        <Button onClick={action('onClick')}
          className="Button Button--small u-rounded Button--secondary">
          <Icon name="refresh" className="n1" /> Updates
        </Button>
      </ButtonToolbar>
    </div>
  ))

.add('Updates only', () => (
    <div>
      <ButtonToolbar>
        <Button onClick={action('onClick')}
          className="Button Button--small u-rounded Button--secondary">
          <Icon name="clock" className="n1" /> All
        </Button>
        <Button onClick={action('onClick')}
          className="Button Button--small u-rounded Button--secondary">
          <Icon name="comment" className="n1" /> Comments
        </Button>
        <Button onClick={action('onClick')}
          className="Button Button--small u-rounded Button--secondary
          is-active">
          <Icon name="refresh" className="n1" /> Updates
        </Button>
      </ButtonToolbar>
    </div>
))


