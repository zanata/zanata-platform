/* eslint-disable max-len */
// @ts-nocheck
import React from 'react'
import { storiesOf } from '@storybook/react'
import RejectionsForm from '.'
import {MINOR, MAJOR, CRITICAL} from './index'

const mockFunc = () => {}

storiesOf('RejectionsForm', module)
    .add('read only (no test)', () => (
      <RejectionsForm
        entityId={1}
        priority={MINOR} textState='text-info'
        criteriaPlaceholder='Format (mismatches, white-spaces, tag error or missing, special character, numeric format, truncated, etc.)'
        description={''}
        onSave={mockFunc}
        onDelete={mockFunc}
        commentRequired={false}
        isAdminMode={false}
        displayDelete={false}
        criterionId={1}
        />
      ))
    .add('editable (no test)', () => (
      <RejectionsForm
        entityId={1}
        priority={MINOR} textState='text-info'
        criteriaPlaceholder='Format (mismatches, white-spaces, tag error or missing, special character, numeric format, truncated, etc.)'
        description={''}
        onSave={mockFunc}
        onDelete={mockFunc}
        commentRequired
        isAdminMode
        displayDelete
        criterionId={1}
      />
    ))
    .add('admin screen (no test)', () => (
      <div className='container'>
        <h1>Reject translations settings</h1>
        <RejectionsForm
          entityId={0}
          priority={CRITICAL} textState='text-info'
          criteriaPlaceholder='Translation Errors (terminology, mistranslated, addition, omission, un-localized, do not translate, etc)'
          description={''}
          onSave={mockFunc}
          onDelete={mockFunc}
          commentRequired
          isAdminMode
          displayDelete
          criterionId={0}
        />
        <RejectionsForm
          entityId={1}
          priority={MINOR} textState='text-info'
          criteriaPlaceholder='Language Quality (grammar, spelling, punctuation, typo, ambiguous wording, product name, sentence structuring, readability, word choice, not natural, too literal, style and tone, etc)'
          description={''}
          onSave={mockFunc}
          onDelete={mockFunc}
          commentRequired
          isAdminMode
          displayDelete
          criterionId={1}
        />
        <RejectionsForm
          entityId={2}
          priority={MAJOR} textState='text-info'
          criteriaPlaceholder='Consistency (inconsistent style or vocabulary, brand inconsistency, etc.)'
          description={''}
          onSave={mockFunc}
          onDelete={mockFunc}
          commentRequired
          isAdminMode
          displayDelete
          criterionId={2}
        />
        <RejectionsForm
          entityId={3}
          priority={MINOR} textState='text-info'
          criteriaPlaceholder='Style Guide & Glossary Violations'
          description={''}
          onSave={mockFunc}
          onDelete={mockFunc}
          commentRequired
          isAdminMode
          displayDelete
          criterionId={3}
        />
        <RejectionsForm
          entityId={4}
          priority={CRITICAL} textState='text-info'
          criteriaPlaceholder='Translation Errors (terminology, mistranslated, addition, omission, un-localized, do not translate, etc)'
          description={''}
          onSave={mockFunc}
          onDelete={mockFunc}
          commentRequired
          isAdminMode
          displayDelete
          criterionId={4}
        />
        <RejectionsForm
          entityId={5}
          priority={CRITICAL} textState='text-info'
          criteriaPlaceholder='Other (reason may be in comment section/history if necessary)'
          description={''}
          onSave={mockFunc}
          onDelete={mockFunc}
          commentRequired
          isAdminMode
          displayDelete
          criterionId={5}
        />
      </div>
    ))
