import React, { Component } from 'react'
import { connect } from 'react-redux'
import PropTypes from 'prop-types'
import Button from '../../components/Button'
import { Row } from 'react-bootstrap'
import { Modal } from '../../../components'
import PriorityDropdown from './PriorityDropdown'
import CriteriaDropdown from './CriteriaDropdown'
import { rejectTranslation } from '../../actions/review-trans-actions'
import update from 'immutability-helper'
import {
  MINOR, MAJOR, CRITICAL, priorities, textState
} from '../../utils/reject-trans-util'

/**
 * Modal to collect feedback on the reason for rejecting a translation.
 */
export class RejectTranslationModal extends Component {
  static propTypes = {
    show: PropTypes.bool.isRequired,
    onHide: PropTypes.func.isRequired,
    transUnitID: PropTypes.number.isRequired,
    revision: PropTypes.number.isRequired,
    localeId: PropTypes.string.isRequired,
    criteria: PropTypes.arrayOf(PropTypes.shape({
      id: PropTypes.number.isRequired,
      editable: PropTypes.bool.isRequired,
      description: PropTypes.string.isRequired,
      priority: PropTypes.oneOf([MINOR, MAJOR, CRITICAL]).isRequired
    })),
    addNewTransReview: PropTypes.func.isRequired
  }
  defaultState = {
    review: {
      selectedPriority: MINOR,
      priorityId: 0,
      selectedCriteria: '',
      criteriaId: 1,
      reviewComment: ''
    }
  }
  constructor (props) {
    super(props)
    this.state = this.defaultState
  }
  componentWillReceiveProps (nextProps) {
    this.setState(prevState => ({
      review: update(prevState.review, {
        selectedCriteria: {$set: nextProps.criteria[0].description},
        selectedPriority: {$set: nextProps.criteria[0].priority}
      })
    }))
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
    const criteriaIdIndex = this.props.criteria.findIndex(
      x => x.description === event.target.innerText)
    const criteriaId = this.props.criteria[criteriaIdIndex]
    this.setState(prevState => ({
      review: update(prevState.review, {
        selectedCriteria: {$set: selectedCriteria},
        criteriaId: {$set: criteriaId}
      })
    }))
  }
  setReviewComment = (event) => {
    const reviewComment = event.target.value
    this.setState(prevState => ({
      review: update(prevState.review, {
        reviewComment: {$set: reviewComment}
      })
    }))
  }
  saveTransReview = () => {
    const review = {
      localeId: this.props.localeId,
      transUnitId: this.props.transUnitID,
      revision: this.props.revision,
      criteriaId: this.state.review.criteriaId,
      reviewComment: this.state.review.reviewComment
    }
    this.props.addNewTransReview(review)
    this.props.onHide()
  }
  render () {
    const {
      show,
      onHide,
      criteria
    } = this.props
    const {
      review
    } = this.state
    const priorityTextState = textState(review.selectedPriority)
    return (
      <Modal show={show}
        onHide={onHide}
        key='reject-translation-modal'
        id='RejectTranslationModal'>
        <Modal.Header>
          <Modal.Title>Reject translation</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <div className='flex'>
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
          <div className='EditorRejection-input'>
            <textarea ref='input'
              type='comment'
              placeholder='Provide a comment for why this translation has been
               rejected'
              cols='50'
              onChange={this.setReviewComment}
              rows='10'
              className='EditorInputGroup-input is-focused InputGroup--outlined
               Commenting' />
          </div>
        </Modal.Body>
        <Modal.Footer>
          <span>
            <Row>
              <Button
                className='EditorButton Button--large u-rounded
                Button--secondary'
                onClick={onHide}>
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

const mapDispatchToProps = dispatch => {
  return {
    addNewTransReview: (review) => dispatch(rejectTranslation(review)
    )
  }
}

export default connect(null, mapDispatchToProps)(RejectTranslationModal)
