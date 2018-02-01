import React, { Component } from 'react'
import { connect } from 'react-redux'
import PropTypes from 'prop-types'
import Button from '../../components/Button'
import { Row } from 'react-bootstrap'
import { Modal } from '../../../components'
import PriorityDropdown from './PriorityDropdown'
import CriteriaDropdown from './CriteriaDropdown'
import { addNewReview } from '../../actions/review-trans-actions'
import update from 'immutability-helper'

 /* eslint-disable max-len */
export const MINOR = 'Minor'
export const MAJOR = 'Major'
export const CRITICAL = 'Critical'
export const priorities = [MINOR, MAJOR, CRITICAL]

/**
 * Modal to collect feedback on the reason for rejecting a translation.
 */
export class RejectTranslationModal extends Component {
  static propTypes = {
    show: PropTypes.bool.isRequired,
    onHide: PropTypes.func.isRequired,
    transUnitID: PropTypes.number.isRequired,
    revision: PropTypes.number.isRequired,
    language: PropTypes.string.isRequired,
    criteria: PropTypes.arrayOf(PropTypes.shape({
      editable: PropTypes.bool.isRequired,
      description: PropTypes.string.isRequired,
      priority: PropTypes.oneOf([MINOR, MAJOR, CRITICAL]).isRequired
    })),
    addNewTransReview: PropTypes.func.isRequired
  }
  defaultState = {
    review: {
      id: 0,
      revision: 1,
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
        id: {$set: nextProps.transUnitID},
        revision: {$set: nextProps.revision},
        selectedCriteria: {$set: nextProps.criteria[0].description},
        selectedPriority: {$set: nextProps.criteria[0].priority}
      })
    }))
  }
  onPriorityChange = (event) => {
    event.persist()
    const priorityIdIndex = priorities.indexOf(event.target.innerText)
    this.setState(prevState => ({
      review: update(prevState.review, {
        selectedPriority: {$set: event.target.innerText},
        priorityId: {$set: priorityIdIndex}
      })
    }))
  }
  onCriteriaChange = (event) => {
    event.persist()
    const criteriaIdIndex = this.props.criteria.findIndex(x => x.description === event.target.innerText)
    this.setState(prevState => ({
      review: update(prevState.review, {
        selectedCriteria: {$set: event.target.innerText},
        // FIXME: The criteria on the server are not zero indexed
        criteriaId: {$set: criteriaIdIndex + 1}
      })
    }))
  }
  setReviewComment = (event) => {
    event.persist()
    this.setState(prevState => ({
      review: update(prevState.review, {
        reviewComment: {$set: event.target.value}
      })
    }))
  }
  textState = () => {
    if (this.state.review.selectedPriority === MAJOR) {
      return 'u-textWarning'
    } else if (this.state.review.selectedPriority === CRITICAL) {
      return 'u-textDanger'
    } else {
      return ''
    }
  }
  render () {
    const {
      show,
      onHide,
      language,
      criteria,
      addNewTransReview
    } = this.props
    const {
      review
    } = this.state
    const saveTransReview = () => {
      addNewTransReview(review, language)
    }
    return (
      <Modal show={show}
        onHide={onHide}
        key="reject-translation-modal"
        id="RejectTranslationModal">
        <Modal.Header>
          <Modal.Title>Reject translation</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <div className="flex">
            <span id="CriteriaTitle">
              Criteria
            </span>
            <CriteriaDropdown
              criteriaList={criteria}
              onCriteriaChange={this.onCriteriaChange}
              selectedCriteria={review.selectedCriteria} />
            <PriorityDropdown
              textState={this.textState()}
              priority={review.selectedPriority}
              priorityChange={this.onPriorityChange} />
          </div>
          <div className="EditorRejection-input">
            <textarea ref="input"
              type="comment"
              placeholder="Provide a comment for why this translation has been rejected"
              cols="50"
              onChange={this.setReviewComment}
              rows="10"
              className='EditorInputGroup-input is-focused InputGroup--outlined Commenting' />
          </div>
        </Modal.Body>
        <Modal.Footer>
          <span>
            <Row>
              <Button
                className="EditorButton Button--large u-rounded Button--secondary"
                onClick={onHide}>
                Cancel
              </Button>
              <Button
                className="EditorButton Button--large u-rounded Button--primary"
                onClick={saveTransReview}>
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
    addNewTransReview: (review, lang) => dispatch(addNewReview(review, lang))
  }
}

export default connect(null, mapDispatchToProps)(RejectTranslationModal)
