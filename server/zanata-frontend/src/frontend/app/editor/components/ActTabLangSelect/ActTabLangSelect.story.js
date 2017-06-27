import React from 'react'
import { storiesOf } from '@kadira/storybook'
import { action, decorateAction } from '@kadira/storybook-addon-actions'
import ActTabLangSelect from '.'
import { Button, ButtonToolbar } from 'react-bootstrap'
import Icon from '../../../components/Icon'

/*
 * See .storybook/README.md for info on the component storybook.
 */
storiesOf('ActTabLangSelect', module)
    .add('default', () => (
      <div className="act-tab-langselect">
        <ButtonToolbar>
          <Button onClick={action('onClick')}
            className="Button Button--small u-rounded Button--primary
            is-active">
            <Icon name="language" className="n1" /> Current
          </Button>
          <Button onClick={action('onClick')}
            className="Button Button--small u-rounded Button--primary">
            <Icon name="language" className="n1" /> All
          </Button>
          <Button onClick={action('onClick')}
            className="Button Button--small u-rounded Button--primary">
            <Icon name="language" className="n1" /> Source
          </Button>
        </ButtonToolbar>
      </div>
    ))

.add('All', () => (
    <div className="act-tab-langselect">
      <ButtonToolbar>
        <Button onClick={action('onClick')}
          className="Button Button--small u-rounded Button--primary">
          <Icon name="language" className="n1" /> Current
        </Button>
        <Button onClick={action('onClick')}
          className="Button Button--small u-rounded Button--primary
          is-active">
          <Icon name="language" className="n1" /> All
        </Button>
        <Button onClick={action('onClick')}
          className="Button Button--small u-rounded Button--primary">
          <Icon name="language" className="n1" /> Source
        </Button>
      </ButtonToolbar>
    </div>
))

.add('Source only', () => (
    <div className="act-tab-langselect">
      <ButtonToolbar>
        <Button onClick={action('onClick')}
          className="Button Button--small u-rounded Button--primary">
          <Icon name="language" className="n1" /> Current
        </Button>
        <Button onClick={action('onClick')}
          className="Button Button--small u-rounded Button--primary">
          <Icon name="language" className="n1" /> All
        </Button>
        <Button onClick={action('onClick')}
          className="Button Button--small u-rounded Button--primary
          is-active">
          <Icon name="language" className="n1" /> Source
        </Button>
      </ButtonToolbar>
    </div>
))

