import React from 'react'
import { storiesOf } from '@storybook/react'
import RejectTranslationModal from '.'
import RejectTranslationModalNoCrit from './RejectTranslationModalNoCrit'
import Lorem from 'react-lorem-component'
import { CRITICAL } from './index.js'

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
    .add('Criteria chosen', () => (
        <RejectTranslationModal show isOpen
         criteria="Translation Errors: terminology, mistranslated addition, omission, un-localized, do not translate, etc"
         priority={CRITICAL}  textState="u-textDanger" />
    ))

    .add('Other - no criteria set', () => (
        <RejectTranslationModalNoCrit show isOpen/>
    ))
