// @ts-nocheck
import React from 'react'
import { storiesOf } from '@storybook/react'
import RejectTranslationModal from '.'
import Lorem from 'react-lorem-component'
import {
  MINOR, UNSPECIFIED
} from '../../utils/reject-trans-util'

const callback = () => {}

const defaultState = {
  review: {
    selectedPriority: MINOR,
    priorityId: 0,
    criteriaDescription: '',
    criteriaId: undefined,
    reviewComment: ''
  },
  charsLeft: 500,
  selectedCriteria: UNSPECIFIED
}

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
    .add('Initial state', () => (
      <RejectTranslationModal
        show
        onHide={callback}
        onHideResetState={callback}
        textLimit={1}
        charsLeft={500}
        criteriaList={[{
          id: 1,
          commentRequired: true,
          description: 'Needs more flair',
          priority: MINOR
        }]}
        onCriteriaChange={callback}
        onUnspecifiedCriteria={callback}
        onPriorityChange={callback}
        saveTransReview={callback}
        selectedCriteria={UNSPECIFIED}
        setReviewComment={callback}
        review={defaultState.review}
        />
    ))

    // .add('Criteria chosen', () => (
    //   <RejectTranslationModal
    //     show
    //     isOpen
    //     criteria="Translation Errors: terminology, mistranslated addition,
    //     omission, un-localized, do not translate, etc"
    //     priority={CRITICAL}
    //     textState="u-textDanger" />
    // ))
    // .add('Other - no criteria set', () => (
    //   <RejectTranslationModal
    //     show
    //     isOpen />
    // ))
