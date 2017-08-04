import React from 'react'
import { storiesOf, action } from '@storybook/react'
import { withInfo } from '@storybook/addon-info'
import { host } from 'storybook-host'
import Button from '.'

/*
 * See .storybook/README.md for info on the component storybook.
 */
storiesOf('Button', module)
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
          className="Button Button--primary">
          Button Button--primary
        </Button>
      </li>
      <li>
        <Button title="Click should trigger onClick action"
          onClick={action('onClick')}
          className="Button Button--success">
          Button Button--success
        </Button>
      </li>
      <li>
        <Button title="Click should trigger onClick action"
          onClick={action('onClick')}
          className="Button Button--unsure">
          Button Button--unsure
        </Button>
      </li>
      <li>
        <Button title="Click should trigger onClick action"
          onClick={action('onClick')}
          className="Button Button--neutral">
          Button Button--neutral
        </Button>
      </li>
    </ul>
  )))
  .add('rounded and styled', withInfo()(() => (
    <Button title="Styles from suggestion panel button"
      onClick={action('onClick')}
      className="Button Button--small u-rounded Button--primary">
      Button Button--small u-rounded Button--primary
    </Button>
  )))

// TODO add more variety of styles. See if there is a stylesheet to compare to
