// @ts-nocheck
import React from 'react'
import * as PropTypes from 'prop-types'
import { isEmpty } from 'lodash'
import Button from '../../components/Button'
import { Row } from 'react-bootstrap'
import { Modal } from '../../../components'
import PriorityDropdown from './PriorityDropdown'
import CriteriaDropdown from './CriteriaDropdown'
import {
  MINOR, MAJOR, CRITICAL, textState
} from '../../utils/reject-trans-util'

/*
 * RejectTranslationModal for reviewer rejecting translations feature.
 */
const RejectTranslationModal = ({
  show,
  onHide,
  onHideResetState,
  onCriteriaChange,
  onUnspecifiedCriteria,
  onPriorityChange,
  textLimit,
  charsLeft,
  criteriaList,
  saveTransReview,
  selectedCriteria,
  setReviewComment,
  review
}) => {
  const priorityTextState = textState(review.selectedPriority)
  const criteriaTile = (!isEmpty(criteriaList))
      ? <div className='flex'>
        <span id='CriteriaTitle'>
          Criteria
        </span>
        <CriteriaDropdown
          criteriaList={criteriaList}
          onCriteriaChange={onCriteriaChange}
          onUnspecifiedCriteria={onUnspecifiedCriteria}
          criteriaDescription={review.criteriaDescription} />
        <PriorityDropdown
          textState={priorityTextState}
          priority={review.selectedPriority}
          priorityChange={onPriorityChange} />
      </div>
      : undefined
  const commentPlaceholder = (selectedCriteria.commentRequired === true)
    ? 'You must provide a comment for why this translation has been rejected'
    : 'Provide a comment for why this translation has been rejected'
  const cantReject = (
    (isEmpty(review.reviewComment)) &&
    (selectedCriteria.commentRequired === true))
  return (
    <Modal show={show}
      onHide={onHideResetState}
      key='reject-translation-modal'
      id='RejectTranslationModal'>
      <Modal.Header>
        <Modal.Title>Reject translation</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        {criteriaTile}
        <div className='EditorRejection-input'>
          <textarea
            type='comment'
            placeholder={commentPlaceholder}
            cols='50'
            onChange={setReviewComment}
            rows='10'
            maxLength={textLimit}
            className='EditorInputGroup-input is-focused InputGroup--outlined
             Commenting' />
          <p>{charsLeft}</p>
        </div>
      </Modal.Body>
      <Modal.Footer>
        <span>
          <Row>
            <Button
              className='EditorButton Button--large u-rounded
              Button--secondary'
              onClick={onHideResetState}>
              Cancel
            </Button>
            <Button
              className='EditorButton Button--large u-rounded Button--primary'
              onClick={saveTransReview}
              disabled={cantReject}>
              Reject translation
            </Button>
          </Row>
        </span>
      </Modal.Footer>
    </Modal>
  )
}

RejectTranslationModal.propTypes = {
  show: PropTypes.bool.isRequired,
  onHide: PropTypes.func.isRequired,
  onHideResetState: PropTypes.func.isRequired,
  textLimit: PropTypes.number.isRequired,
  charsLeft: PropTypes.number.isRequired,
  criteriaList: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.number,
    commentRequired: PropTypes.bool.isRequired,
    description: PropTypes.string.isRequired,
    priority: PropTypes.oneOf([MINOR, MAJOR, CRITICAL]).isRequired
  })).isRequired,
  onCriteriaChange: PropTypes.func.isRequired,
  onUnspecifiedCriteria: PropTypes.func.isRequired,
  onPriorityChange: PropTypes.func.isRequired,
  saveTransReview: PropTypes.func.isRequired,
  selectedCriteria: PropTypes.shape({
    id: PropTypes.number,
    commentRequired: PropTypes.bool,
    description: PropTypes.string,
    priority: PropTypes.oneOf([MINOR, MAJOR, CRITICAL])
  }),
  setReviewComment: PropTypes.func.isRequired,
  review: PropTypes.shape({
    selectedPriority: PropTypes.oneOf([MINOR, MAJOR, CRITICAL]),
    priorityId: PropTypes.number,
    criteriaDescription: PropTypes.string,
    criteriaId: PropTypes.number,
    reviewComment: PropTypes.string
  })
}

export default RejectTranslationModal
