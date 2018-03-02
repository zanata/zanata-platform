// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import Button from '../Button'
import { Row } from 'react-bootstrap'
import { Modal } from '../../../components'
/**
 * TODO add a concise description of this component
 */

export class RejectTranslationModalNoCrit extends Component {
  static propTypes = {
    show: PropTypes.bool,
    className: PropTypes.string,
    key: PropTypes.string,
    onHide: PropTypes.func,
    isOpen: PropTypes.bool
  }

  render () {
    const {
      show,
      key,
      className,
      onHide,
      isOpen
    } = this.props

    return (
        <Modal show={show}
               onHide={close}
               key="reject-translation-modal"
               id="RejectTranslationModal">
          <Modal.Header>
            <Modal.Title>Reject translation</Modal.Title>
          </Modal.Header>
          <Modal.Body>
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

export default RejectTranslationModalNoCrit
