// @ts-nocheck
import React from 'react'
import { storiesOf } from '@storybook/react'
import { Button } from 'react-bootstrap'
import { Icon, TextInput } from '../../components'
import RejectionsForm from '.'
import { MINOR, MAJOR, CRITICAL } from "./index";

storiesOf('RejectionsForm', module)
  .add('read only', () => (
      <RejectionsForm
          commentRequired={false}
          criteriaPlaceholder='Format (mismatches, white-spaces, tag error or missing, special character, numeric format, truncated, etc.)'
          priority={MINOR} textState='text-info' />
   ))
   .add('commentRequired', () => (
        <RejectionsForm
            className='active'
            commentRequired={true}
            criteriaPlaceholder='Format (mismatches, white-spaces, tag error or missing, special character, numeric format, truncated, etc.)'
            priority={MINOR} textState='text-info' />
    ))
   .add('Admin screen', () => (
      <div className='container'>
        <h1>Reject translations settings</h1>
        <RejectionsForm
            commentRequired={false}
            criteriaPlaceholder='Translation Errors (terminology, mistranslated, addition, omission, un-localized, do not translate, etc)'
            priority={CRITICAL} textState='text-danger' />
        <RejectionsForm
            commentRequired={true}
            className='active'
            criteriaPlaceholder='Language Quality (grammar, spelling, punctuation, typo, ambiguous wording, product name, sentence structuring, readability, word choice, not natural, too literal, style and tone, etc)'
            priority={MAJOR} textState='text-warning' />
        <RejectionsForm
            commentRequired={false}
            criteriaPlaceholder='Consistency (inconsistent style or vocabulary, brand inconsistency, etc.)'
            priority={MAJOR} textState='text-warning' />
        <RejectionsForm
            commentRequired={false}
            criteriaPlaceholder='Style Guide & Glossary Violations'
            priority={MINOR} textState='text-info' />
        <RejectionsForm
            commentRequired={false}
            criteriaPlaceholder='Format (mismatches, white-spaces, tag error or missing, special character, numeric format, truncated, etc.)'
            priority={MINOR} textState='text-info' />
        <RejectionsForm
            commentRequired={true}
            className='active'
            criteriaPlaceholder='Other (reason may be in comment section/history if necessary)'
            priority={CRITICAL} textState='text-danger' />
        <div className='rejection-btns'>
         <Button bsStyle='primary' className='btn-left'>
            <Icon name='plus' className='s1' /> Add review criteria
          </Button>
          <Button bsStyle='info'>
           <Icon name='tick' className='s1' /> Save changes
          </Button>
        </div>
      </div>
  ))
