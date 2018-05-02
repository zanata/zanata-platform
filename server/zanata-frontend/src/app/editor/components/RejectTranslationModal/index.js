// @ts-nocheck
import React from 'react'
import * as PropTypes from 'prop-types'
import { isEmpty } from 'lodash'
import PriorityDropdown from './PriorityDropdown'
import CriteriaDropdown from './CriteriaDropdown'
import {
  MINOR, MAJOR, CRITICAL, textState
} from '../../utils/reject-trans-util'
import Modal from 'antd/lib/modal'
import 'antd/lib/modal/style/css'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import Row from 'antd/lib/row'
import 'antd/lib/row/style/css'
import Col from 'antd/lib/col'
import 'antd/lib/col/style/css'

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
      ? <Row>
        <Col span={21}>
          <span id='CriteriaTitle'>
            Criteria
          </span>
          <CriteriaDropdown
            criteriaList={criteriaList}
            onCriteriaChange={onCriteriaChange}
            onUnspecifiedCriteria={onUnspecifiedCriteria}
            criteriaDescription={review.criteriaDescription} />
        </Col>
        <Col span={3}>
          <PriorityDropdown
            textState={priorityTextState}
            priority={review.selectedPriority}
            priorityChange={onPriorityChange} />
        </Col>
      </Row>
      : undefined
  const commentPlaceholder = (selectedCriteria.commentRequired === true)
    ? 'You must provide a comment for why this translation has been rejected'
    : 'Provide a comment for why this translation has been rejected'
  const cantReject = (
    (isEmpty(review.reviewComment)) &&
    (selectedCriteria.commentRequired === true))
  return (
    <Modal
      visible={show}
      title={'Reject Translation'}
      onOk={saveTransReview}
      onCancel={onHideResetState}
      key='reject-translation-modal'
      id='RejectTranslationModal'
      width={'90%'}
      footer={[
        <Button key='back' onClick={onHideResetState}>
          Cancel
        </Button>,
        <Button
          key='ok'
          type='danger'
          onClick={saveTransReview}
          disabled={cantReject}>
          Reject translation
        </Button>]}>
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
