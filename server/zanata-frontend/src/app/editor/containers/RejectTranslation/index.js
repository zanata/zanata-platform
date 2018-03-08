// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import { connect } from 'react-redux'
import RejectTranslationModal from '../../components/RejectTranslationModal'
import { rejectTranslation } from '../../actions/review-trans-actions'
import update from 'immutability-helper'
import { isUndefined, isEmpty } from 'lodash'
import {
  MINOR, MAJOR, CRITICAL, UNSPECIFIED, priorities
} from '../../utils/reject-trans-util'
const textLimit = 500

/**
 * Modal to collect feedback on the reason for rejecting a translation.
 */
export class RejectTranslation extends Component {
  static propTypes = {
    show: PropTypes.bool.isRequired,
    onHide: PropTypes.func.isRequired,
    transUnitID: PropTypes.number.isRequired,
    // Initial flyweight fetch of phrases does not include the revision detail
    revision: PropTypes.number,
    localeId: PropTypes.string.isRequired,
    criteriaList: PropTypes.arrayOf(PropTypes.shape({
      id: PropTypes.number,
      commentRequired: PropTypes.bool.isRequired,
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
      criteriaDescription: '',
      criteriaId: undefined,
      reviewComment: ''
    },
    charsLeft: textLimit,
    selectedCriteria: UNSPECIFIED
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
    const selectedCriteria = this.props.criteriaList.find(
      x => x.description === event.target.innerText)
    this.setState(prevState => ({
      review: update(prevState.review, {
        criteriaDescription: {$set: selectedCriteria.description},
        criteriaId: {$set: selectedCriteria.id},
        selectedPriority: {$set: selectedCriteria.priority}
      }),
      selectedCriteria: selectedCriteria
    }))
  }
  onUnspecifiedCriteria = () => {
    this.setState(prevState => ({
      review: update(prevState.review, {
        criteriaDescription: {$set: UNSPECIFIED.description},
        criteriaId: {$set: undefined},
        selectedPriority: {$set: UNSPECIFIED.priority}
      }),
      selectedCriteria: UNSPECIFIED
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
    if (!isUndefined(review.criteriaId) || !isEmpty(review.reviewComment)) {
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
    const { show, criteriaList } = this.props
    const { review, selectedCriteria, charsLeft } = this.state
    return (
      <RejectTranslationModal
        show={show}
        onHide={this.onHideResetState}
        onHideResetState={this.onHideResetState}
        onCriteriaChange={this.onCriteriaChange}
        onUnspecifiedCriteria={this.onUnspecifiedCriteria}
        onPriorityChange={this.onPriorityChange}
        textLimit={textLimit}
        charsLeft={charsLeft}
        criteriaList={criteriaList}
        saveTransReview={this.saveTransReview}
        setReviewComment={this.setReviewComment}
        selectedCriteria={selectedCriteria}
        review={review} />
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

export default connect(null, mapDispatchToProps)(RejectTranslation)
