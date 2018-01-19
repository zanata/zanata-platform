import React from 'react'
import { storiesOf } from '@storybook/react'
import RejectTranslationModal from '.'
import Lorem from 'react-lorem-component'
import { MINOR, MAJOR, CRITICAL } from './index.js'

/* eslint-disable max-len */
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
    .add('Translation errors (critical)', () => (
      <RejectTranslationModal show
        criteriaList={
          ['Translation Errors (terminology, mistranslated addition, omission, un-localized, do not translate, etc)',
          'Other (reason may be in comment section/history if necessary)']
        }
        priority={CRITICAL} textState="u-textDanger" />
    ))
    .add('Style Guide and Glossary Violations (minor)', () => (
      <RejectTranslationModal show
        criteriaList={
          ['Translation Errors (terminology, mistranslated addition, omission, un-localized, do not translate, etc)',
          'Other (reason may be in comment section/history if necessary)']
        }
        priority={MINOR} />
    ))

    .add('Other (major)', () => (
      <RejectTranslationModal show
        criteriaList={
          ['Translation Errors (terminology, mistranslated addition, omission, un-localized, do not translate, etc)',
          'Other (reason may be in comment section/history if necessary)']
        }
        priority={MAJOR} textState="u-textWarning" />
    ))
