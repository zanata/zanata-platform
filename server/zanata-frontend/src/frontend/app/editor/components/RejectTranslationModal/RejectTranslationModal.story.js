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
        <RejectTranslationModal show
         criteria={<span><strong>Translation Errors</strong> (terminology,
         mistranslated addition, omission, un-localized, do not translate, etc)</span>}
         priority="Critical"  textState="u-textDanger" />
    ))
    .add('Style Guide and Glossary Violations [minor]', () => (
        <RejectTranslationModal show criteria={<span><strong>Style Guide and Glossary Violations</strong></span>} priority="Minor" />
    ))

    .add('Other [major]', () => (
        <RejectTranslationModal show
         criteria={<span><strong>Other</strong> (reason may be in comment section/history if necessary)</span>}
         priority="Major" textState="u-textWarning" />
    ))
