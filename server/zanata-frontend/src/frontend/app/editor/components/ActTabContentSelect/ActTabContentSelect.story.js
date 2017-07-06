import React from 'react'
import { storiesOf } from '@kadira/storybook'
import { action, decorateAction } from '@kadira/storybook-addon-actions'
import ActTabContentSelect from '.'
import { ButtonToolbar } from 'react-bootstrap'

/*
 * See .storybook/README.md for info on the component storybook.
 */
storiesOf('ActTabContentSelect', module)
    .add('Activity select', () => (
      <ButtonToolbar>
        <ActTabContentSelect icon="clock" buttonName="All" buttonClass="Button--secondary" />
        <ActTabContentSelect icon="comment" buttonName="Comments" buttonClass="Button--secondary" />
        <ActTabContentSelect icon="refresh" buttonName="Updates" buttonClass="Button--secondary"  />
      </ButtonToolbar>
    ))


    .add('Language select', () => (
       <ButtonToolbar>
          <ActTabContentSelect icon="language" buttonName="Current" buttonClass="Button--primary" />
          <ActTabContentSelect icon="language" buttonName="All" buttonClass="Button--primary" />
          <ActTabContentSelect icon="language" buttonName="Source" buttonClass="Button--primary"  />
       </ButtonToolbar>
    ))


