import React, { Component } from 'react'
import PropTypes from 'prop-types'
import Button from '../../components/Button'
import { Row } from 'react-bootstrap'
import { Modal } from '../../../components'
import PriorityDropdown from './PriorityDropdown'
import CriteriaDropdown from './CriteriaDropdown'

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
    criteriaList: PropTypes.arrayOf(PropTypes.string).isRequired
  }
  constructor (props) {
    super(props)
    this.state = {
      selectedCriteria: this.props.criteriaList[0]
    }
  }
  // TODO: Placeholder func, update Priority prop of RejectTranslationModal
  onPriorityChange = (priority) => {
    return priority
  }
  // TODO: Placeholder func, update Criteria prop of RejectTranslationModal
  onCriteriaChange = (criteria) => {
    return criteria
  }
  render () {
    const {
      show,
      onHide,
      criteriaList,
      priority,
      textState
    } = this.props
    const {
      selectedCriteria
    } = this.state
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
              criteriaList={criteriaList}
              onCriteriaChange={this.onCriteriaChange}
              selectedCriteria={selectedCriteria} />
            <PriorityDropdown
              textState={textState}
              priority={priority}
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
              <Button className="EditorButton Button--large u-rounded Button--primary">
                Reject translation
              </Button>
            </Row>
          </span>
        </Modal.Footer>
      </Modal>
    )
  }
}

export default RejectTranslationModal
