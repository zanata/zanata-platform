import React from 'react'
import cx from 'classnames'
import { storiesOf, action } from '@storybook/react'
import { withInfo } from '@storybook/addon-info'
import { host } from 'storybook-host'
import { withKnobs, text, boolean, select } from '@storybook/addon-knobs'
import Button from '.'

/*
 * See .storybook/README.md for info on the component storybook.
 */
storiesOf('Button', module)
  .addDecorator(withKnobs)
  .addDecorator(host({
    title: 'Button',
    align: 'center middle'
  }))
  .add('plain', withInfo()(() => (
    <Button title="Click should trigger onClick action"
      onClick={action('onClick')}>
      Unstyled button. Pretty plain. Should be used with some styles.
    </Button>
  )))
  .add('plain (disabled)', withInfo()(() => (
    <Button title="Should not dispatch onClick action"
      onClick={action('onClick')}
      disabled>
      Plain disabled button. Click event should <strong>not</strong> be fired.
    </Button>
  )))
  .add('colour styles', withInfo()(() => (
    <ul>
      <li>
        <Button title="Click should trigger onClick action"
          onClick={action('onClick')}
          className="EditorButton Button--primary">
          Button Button--primary
        </Button>
      </li>
      <li>
        <Button title="Click should trigger onClick action"
          onClick={action('onClick')}
          className="EditorButton Button--success">
          Button Button--success
        </Button>
      </li>
      <li>
        <Button title="Click should trigger onClick action"
          onClick={action('onClick')}
          className="EditorButton Button--unsure">
          Button Button--unsure
        </Button>
      </li>
      <li>
        <Button title="Click should trigger onClick action"
          onClick={action('onClick')}
          className="EditorButton Button--neutral">
          Button Button--neutral
        </Button>
      </li>
    </ul>
  )))
  .add('rounded and styled', withInfo()(() => (
    <Button title="Styles from suggestion panel button"
      onClick={action('onClick')}
      className="EditorButton Button--small u-rounded Button--primary">
      Button Button--small u-rounded Button--primary
    </Button>
  )))
  .add('BUTTON BUILDER', withInfo({
    text: 'Use KNOBS tab to adjust the settings, ' +
      'then copy your code from Story Source',
    maxPropsIntoLine: 1,
    maxPropStringLength: 500
  })(() => {
    return (
      <Button title={text('title (tooltip)', 'Click Me')}
        disabled={boolean('disabled', false)}
        onClick={action('onClick')}
        className={cx('Button',
          select('colour style', [
            'Button--default',
            'Button--primary',
            'Button--secondary',
            'Button--success',
            'Button--unsure',
            'Button--neutral',
            'Button--warning',
            'Button--danger'
          ], 'Button--default'),
          {
            'is-active': boolean('.is-active', false),
            'u-rounded': boolean('.u-rounded', false),
            'Button--snug': boolean('.Button--snug', false),
            'Button--small': boolean('.Button--small', false),
            'Button--invisible': boolean('.Button--invisible', false)
          }
        )}>
          {text('Content', 'Click Me')}
      </Button>
    )
  }))

// TODO add more variety of styles. See if there is a stylesheet to compare to
