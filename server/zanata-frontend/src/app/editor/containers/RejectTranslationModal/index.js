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

/**
 * Modal to collect feedback on the reason for rejecting a translation.
 */
export class RejectTranslationModal extends Component {
  static propTypes = {
    show: PropTypes.bool,
    onHide: PropTypes.func,
    transUnitID: PropTypes.string,
    language: PropTypes.string,
    criteria: PropTypes.arrayOf(PropTypes.shape({
      editable: PropTypes.bool.isRequired,
      description: PropTypes.string.isRequired,
      priority: PropTypes.oneOf([MINOR, MAJOR, CRITICAL]).isRequired
    })),
    priority: PropTypes.oneOf(
      [
        MINOR,
        MAJOR,
        CRITICAL
      ]
    ).isRequired,
    textState: PropTypes.oneOf(
      [
        'u-textWarning',
        'u-textDanger'
      ]
    ),
    addNewTransReview: PropTypes.func.isRequired
  }
  defaultState = {
    review: {
      id: 0,
      selectedPriority: 'Minor',
      selectedCriteria: {
        index: 0,
        editable: true,
        description: '',
        priority: MINOR
      },
      reviewComment: ''
    }
  }
  constructor (props) {
    super(props)
    this.state = this.defaultState
  }
  onPriorityChange = (event) => {
    event.persist()
    this.setState(prevState => ({
      review: update(prevState.review, {
        selectedPriority: {$set: event.target.innerText}
      })
    }))
  }
  // TODO: Placeholder func, update Criteria prop of RejectTranslationModal
  onCriteriaChange = (criteria) => {
    return criteria
  }
  render () {
    const {
      show,
      onHide,
      language,
      criteria,
      textState,
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
              textState={textState}
              priority={review.selectedPriority}
              priorityChange={this.onPriorityChange} />
          </div>
          <div className="EditorRejection-input">
            <textarea ref="input"
              type="comment"
              placeholder="Provide a comment for why this translation has been rejected"
              cols="50"
              rows="10"
              className='EditorInputGroup-input is-focused InputGroup--outlined Commenting' />
          </div>
        </Modal.Body>
        <Modal.Footer>
          <span>
            <Row>
              <Button className="EditorButton Button--large u-rounded Button--secondary">
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
