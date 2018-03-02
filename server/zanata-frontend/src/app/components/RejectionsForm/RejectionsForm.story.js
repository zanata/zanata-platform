// @ts-nocheck
import React from 'react'
import { storiesOf, action } from '@storybook/react'
import { Button, Alert, OverlayTrigger, Tooltip } from 'react-bootstrap'
import { Icon } from '../../components'
import RejectionsForm from '.'
import {MINOR, MAJOR, CRITICAL} from "./index";

const tooltipDeleteConfirmation = (
    <Tooltip id='tooltip'>Are you sure you want to delete this criteria?<br/>
  <span className="button-spacing"><Button bsStyle='danger' type='button'
                                           className='btn-sm'
                                           onClick={action('onClick')}>Delete</Button>
  <Button bsStyle='default' type='button' className='btn-sm'
          onClick={action('onClick')}>Cancel</Button></span>
    </Tooltip>)

storiesOf('RejectionsForm', module)
    .add('read only', () => (
        <RejectionsForm
            editable={false}
            criteriaPlaceholder='Format (mismatches, white-spaces, tag error or missing, special character, numeric format, truncated, etc.)'
            priority={MINOR} textState='text-info'/>
    ))
    .add('editable', () => (
        <RejectionsForm
            className='active'
            editable={true}
            criteriaPlaceholder='Format (mismatches, white-spaces, tag error or missing, special character, numeric format, truncated, etc.)'
            priority={MINOR} textState='text-info'/>
    ))
    .add('admin screen', () => (
        <div className='container'>
          <h1>Reject translations settings</h1>
          <RejectionsForm
              editable={true}
              criteriaPlaceholder='Translation Errors (terminology, mistranslated, addition, omission, un-localized, do not translate, etc)'
              priority={CRITICAL} textState='text-danger'/>
          <RejectionsForm
              editable={true}
              className='active'
              criteriaPlaceholder='Language Quality (grammar, spelling, punctuation, typo, ambiguous wording, product name, sentence structuring, readability, word choice, not natural, too literal, style and tone, etc)'
              priority={MAJOR} textState='text-warning'/>
          <RejectionsForm
              editable={true}
              criteriaPlaceholder='Consistency (inconsistent style or vocabulary, brand inconsistency, etc.)'
              priority={MAJOR} textState='text-warning'/>
          <RejectionsForm
              editable={true}
              criteriaPlaceholder='Style Guide & Glossary Violations'
              priority={MINOR} textState='text-info'/>
          <RejectionsForm
              editable={true}
              criteriaPlaceholder='Format (mismatches, white-spaces, tag error or missing, special character, numeric format, truncated, etc.)'
              priority={MINOR} textState='text-info'/>
          <RejectionsForm
              editable={true}
              className='active'
              criteriaPlaceholder='Other (reason may be in comment section/history if necessary)'
              priority={CRITICAL} textState='text-danger'/>
          <div className='rejection-btns'>
            <Button bsStyle='primary' className='btn-left'>
              <Icon name='plus' className='s1'/> Add review criteria
            </Button>
          </div>
        </div>
    ))
    .add('**Saved criteria', () => (
        <div className='container'>
          <Alert bsStyle="success">Rejection criteria saved.</Alert>
          <h1>Reject translations settings</h1>
          <RejectionsForm
              editable={true}
              criteriaPlaceholder='Translation Errors (terminology, mistranslated, addition, omission, un-localized, do not translate, etc)'
              priority={CRITICAL} textState='text-danger'/>
          <RejectionsForm
              editable={true}
              className='active'
              criteriaPlaceholder='Language Quality (grammar, spelling, punctuation, typo, ambiguous wording, product name, sentence structuring, readability, word choice, not natural, too literal, style and tone, etc)'
              priority={MAJOR} textState='text-warning'/>
          <RejectionsForm
              editable={true}
              criteriaPlaceholder='Consistency (inconsistent style or vocabulary, brand inconsistency, etc.)'
              priority={MAJOR} textState='text-warning'/>
          <RejectionsForm
              editable={true}
              criteriaPlaceholder='Style Guide & Glossary Violations'
              priority={MINOR} textState='text-info'/>
          <RejectionsForm
              editable={true}
              criteriaPlaceholder='Format (mismatches, white-spaces, tag error or missing, special character, numeric format, truncated, etc.)'
              priority={MINOR} textState='text-info'/>
          <RejectionsForm
              editable={true}
              className='active'
              criteriaPlaceholder='Other (reason may be in comment section/history if necessary)'
              priority={CRITICAL} textState='text-danger'/>
          <div className='rejection-btns'>
            <Button bsStyle='primary' className='btn-left'>
              <Icon name='plus' className='s1'/> Add review criteria
            </Button>
          </div>
        </div>
    ))
    .add('**Confirm delete criteria', () => (
        <div className="pull-right">
          <OverlayTrigger placement='left' overlay={tooltipDeleteConfirmation}>
            <Button bsStyle='danger' className='btn-sm'
                    onClick={action('onClick')}>
              <Icon name='trash' className='s0 iconEdit'/>
            </Button>
          </OverlayTrigger>
        </div>
    ))
