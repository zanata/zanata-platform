/* eslint-disable */
import React from 'react'
import { storiesOf } from '@storybook/react'
import { LoaderText } from '../'
import { Well } from 'react-bootstrap'

storiesOf('LoaderText', module)
    .add('default', () => (
        <span>
  <h2><img
      src="https://upload.wikimedia.org/wikipedia/commons/4/49/Zanata-Logo.svg"
      width="42px"/>LoaderText</h2>
          <Well bsSize="large">Use this when there is content that needs to be loaded so that the user is given feedback.</Well>
      <LoaderText loading={true} loadingText='Updating'>
        Update
      </LoaderText>
        </span>
    ))
