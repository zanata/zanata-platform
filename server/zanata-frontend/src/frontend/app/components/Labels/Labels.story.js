import React from 'react'
import { storiesOf, action } from '@kadira/storybook'
import { Label } from 'react-bootstrap'

storiesOf('Labels', module)
    .add('in-headings', () => (
    <span>
      <h1>Label <Label>New</Label></h1>
    <h2>Label <Label>New</Label></h2>
    <h3>Label <Label>New</Label></h3>
    <h4>Label <Label>New</Label></h4>
    <h5>Label <Label>New</Label></h5>
    <p>Label <Label>New</Label></p>
      </span>
    ))

    .add('validation-states', () => (
        <span>
    <Label bsStyle='default'>Default</Label>
    <Label bsStyle='primary'>Primary</Label>
    <Label bsStyle='success'>Success</Label>
    <Label bsStyle='warning'>Warning</Label>
    <Label bsStyle='danger'>Danger</Label>
        </span>
    ))
