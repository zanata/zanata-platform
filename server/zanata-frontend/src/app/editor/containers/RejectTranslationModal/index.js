import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import { connect } from 'react-redux'
import Button from '../../components/Button'
import { Row } from 'react-bootstrap'
import { Modal } from '../../../components'
import PriorityDropdown from './PriorityDropdown'
import CriteriaDropdown from './CriteriaDropdown'
import { rejectTranslation } from '../../actions/review-trans-actions'
import update from 'immutability-helper'
import { isUndefined } from 'lodash'
import {
  MINOR, MAJOR, CRITICAL, priorities, textState
} from '../../utils/reject-trans-util'

const textLimit = 500

/**
 * Modal to collect feedback on the reason for rejecting a translation.
 */
export class RejectTranslationModal extends Component {
  static propTypes = {
    show: PropTypes.bool.isRequired,
    onHide: PropTypes.func.isRequired,
    transUnitID: PropTypes.number.isRequired,
    // Initial flyweight fetch of phrases does not include the revision detail
    revision: PropTypes.number,
    localeId: PropTypes.string.isRequired,
    criteria: PropTypes.arrayOf(PropTypes.shape({
      id: PropTypes.number.isRequired,
      editable: PropTypes.bool.isRequired,
      description: PropTypes.string.isRequired,
      priority: PropTypes.oneOf([MINOR, MAJOR, CRITICAL]).isRequired
    })).isRequired,
    addNewTransReview: PropTypes.func.isRequired,
    selectedPhrase: PropTypes.object
  }
  defaultState = {
    review: {
      selectedPriority: MINOR,
      priorityId: 0,
      selectedCriteria: '-- Select a predefined criteria --',
      criteriaId: undefined,
      reviewComment: ''
    },
    charsLeft: textLimit
  }
  constructor (props) {
    super(props)
    this.state = this.defaultState
  }
  onPriorityChange = (event) => {
    const selectedPriority = event.target.innerText
    const priorityIdIndex = priorities.indexOf(event.target.innerText)
    this.setState(prevState => ({
      review: update(prevState.review, {
        selectedPriority: {$set: selectedPriority},
        priorityId: {$set: priorityIdIndex}
      })
    }))
  }
  onCriteriaChange = (event) => {
    const selectedCriteria = event.target.innerText
    const criteriaId = this.props.criteria.find(
      x => x.description === event.target.innerText).id
    this.setState(prevState => ({
      review: update(prevState.review, {
        selectedCriteria: {$set: selectedCriteria},
        criteriaId: {$set: criteriaId}
      })
    }))
  }
  setReviewComment = (event) => {
    const reviewComment = event.target.value
    const charCount = event.target.value.length
    const charLeft = textLimit - charCount
    this.setState(prevState => ({
      review: update(prevState.review, {
        reviewComment: {$set: reviewComment}
      }),
      charsLeft: charLeft
    }))
  }
  saveTransReview = () => {
    const review = {
      localeId: this.props.localeId,
      transUnitId: this.props.transUnitID,
      revision: this.props.revision,
      criteriaId: this.state.review.criteriaId,
      reviewComment: this.state.review.reviewComment,
      phrase: this.props.selectedPhrase
    }
    if (!isUndefined(review.criteriaId) || review.reviewComment.length > 0) {
      this.props.addNewTransReview(review)
      this.onHideResetState()
    } else {
      console.error('Must either select a criteria or provide a comment.')
    }
  }
  onHideResetState = () => {
    this.setState(this.defaultState)
    this.props.onHide()
  }
  /* eslint-disable max-len */
  render () {
    const { show, criteria } = this.props
    const { review } = this.state
    const priorityTextState = textState(review.selectedPriority)
    const criteriaTile = (criteria.length > 0)
        ? <div className='flex'>
          <span id='CriteriaTitle'>
            Criteria
          </span>
          <CriteriaDropdown
            criteriaList={criteria}
            onCriteriaChange={this.onCriteriaChange}
            selectedCriteria={review.selectedCriteria} />
          <PriorityDropdown
            textState={priorityTextState}
            priority={review.selectedPriority}
            priorityChange={this.onPriorityChange} />
        </div>
        : undefined
    return (
      <Modal show={show}
        onHide={this.onHideResetState}
        key='reject-translation-modal'
        id='RejectTranslationModal'>
        <Modal.Header>
          <Modal.Title>Reject translation</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {criteriaTile}
          <div className='EditorRejection-input'>
            <textarea ref='input'
              type='comment'
              placeholder='Provide a comment for why this translation has been rejected'
              cols='50'
              onChange={this.setReviewComment}
              rows='10'
              maxLength={textLimit}
              className='EditorInputGroup-input is-focused InputGroup--outlined
               Commenting' />
            <p>{this.state.charsLeft}</p>
          </div>
        </Modal.Body>
        <Modal.Footer>
          <span>
            <Row>
              <Button
                className='EditorButton Button--large u-rounded
                Button--secondary'
                onClick={this.onHideResetState}>
                Cancel
              </Button>
              <Button
                className='EditorButton Button--large u-rounded Button--primary'
                onClick={this.saveTransReview}>
                Reject translation
              </Button>
            </Row>
          </span>
        </Modal.Footer>
      </Modal>
    )
  }
}
/* eslint-enable max-len */
const mapDispatchToProps = dispatch => {
  return {
    addNewTransReview: (review) => dispatch(rejectTranslation(dispatch, review)
    )
  }
}

export default connect(null, mapDispatchToProps)(RejectTranslationModal)
