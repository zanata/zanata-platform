import React from 'react'
import { storiesOf, action } from '@storybook/react'
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
    .add('Translation errors [critical]', () => (
        <RejectTranslationModal show criteria="Translation errors" priority="Critical"  textState="u-textDanger" />
    ))
    .add('Style Guide and Glossary Violations [minor]', () => (
        <RejectTranslationModal show criteria="Style Guide and Glossary Violations" priority="Minor" />
    ))

    .add('Other [major]', () => (
        <RejectTranslationModal show criteria="Other" priority="Major" textState="u-textWarning" />
    ))
