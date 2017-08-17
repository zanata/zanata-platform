import React from 'react'
import { storiesOf } from '@storybook/react'
import { ProgressBar } from 'react-bootstrap'

const now = 60

storiesOf('ProgressBar', module)
    .add('default', () => (
        <span>
          <ProgressBar now={now} label={`${now}%`} />
          <ProgressBar now={60} />
        </span>
    ))

    .add('translation states', () => (
        <span>
          Translated
          <ProgressBar bsStyle='success' now={40} />
          Approved
          <ProgressBar bsStyle='info' now={20} />
          Needs Work
          <ProgressBar bsStyle='warning' now={60} />
          Rejected
          <ProgressBar bsStyle='danger' now={80} />
        </span>
    ))

    .add('stacked', () => (
        <ProgressBar>
          <ProgressBar striped bsStyle='success' now={35} key={1} />
          <ProgressBar bsStyle='warning' now={20} key={2} />
          <ProgressBar active bsStyle='danger' now={10} key={3} />
        </ProgressBar>
    ))
