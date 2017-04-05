import React from 'react'
import { storiesOf } from '@kadira/storybook'
import { action, decorateAction } from '@kadira/storybook-addon-actions'
import { Modal, Button, Panel, Row, Table } from 'react-bootstrap'
import { Icon } from '../../../components'
import Lorem from 'react-lorem-component'
import GlossaryTermModal from './disconnected'

/*
* See .storybook/README.md for info on the component storybook.
*/
storiesOf('GlossaryTermModal', module)
  .addDecorator((story) => (
    <div>
      <h1>Lorem Ipsum</h1>
      <Lorem count={1} />
      <Lorem mode="list" />
      <h2>Dolor Sit Amet</h2>
      <Lorem />
      <Lorem mode="list" />
      <div className="static-modal">
        {story()}
      </div>
    </div>
  ))
  .add('with 1 detail item', () => (
    <GlossaryTermModal
      show={true}
      close={action('close')}
      sourceLocale="en-US"
      targetLocale="de"
      term={{
        source: 'bat',
        target: 'schlagen'
      }}
      details={[
        {
          description: 'Take a turn at batting in a game of sportsball.',
          pos: 'Verb',
          targetComment: 'It sounds a bit like "slugger" like someone might call a junior-league base kid ball.',
          lastModifiedDate: 1490687578793
        }
      ]}
    />
  ))
  .add('with 3 detail items', () => (
    <GlossaryTermModal
      show={true}
      close={action('close')}
      sourceLocale="en-US"
      targetLocale="de"
      term={{
        source: 'bat',
        target: 'schlagen'
      }}
      details={[
        {
          description: 'To hit something with a bat.',
          pos: 'Verb',
          targetComment: "So I schlagged 'im.",
          lastModifiedDate: 1490687563741
        },{
          description: 'Take a turn at batting in a game of sportsball.',
          pos: 'Verb',
          targetComment: 'It sounds a bit like "slugger" like someone might call a junior-league base kid ball.',
          lastModifiedDate: 1490687578793
        },{
          description: 'For one who is a bat, to hit something with themself.',
          pos: 'Norb',
          targetComment: 'I did not just make it up, it is a real thing.',
          lastModifiedDate: 409872360000
        }
      ]}
    />
  ))
