import React from 'react'
import { storiesOf, action } from '@kadira/storybook'
import RejectTranslationModal from '.'
import Lorem from 'react-lorem-component'

/*
 * TODO add stories showing the range of states
 *      for RejectTranslationModal
 */
storiesOf('RejectTranslationModal', module)
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
  .add('default', () => (
    <RejectTranslationModal show />
  ))
