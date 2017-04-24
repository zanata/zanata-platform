import React, { Component, PropTypes } from 'react'
import { connect } from 'react-redux'
import { Icon, Modal } from '../../components'
import { clearMessage } from '../../actions/common'
import { isEmpty } from 'lodash'
import { Button, ButtonGroup, Row } from 'react-bootstrap'

/**
 * Notification popup modal that overlays on the page.
 */
class Notification extends Component {

  getSeverityClass (severity) {
    switch (severity) {
      case 'warn':
      case 'error':
        return 'danger'
      default:
        return ''
    }
  }

  getIcon (severity) {
    switch (severity) {
      case 'warn':
      case 'error':
        return 'warning'
      default:
        return 'info'
    }
  }

  clearMessage () {
    this.props.handleClearMessage()
  }

  render () {
    const {
      severity,
      message,
      details,
      show
      } = this.props
    const severityClass = this.getSeverityClass(severity)
    const icon = this.getIcon(severity)

    /* eslint-disable react/jsx-no-bind, react/jsx-boolean-value */
    return (
      <Modal show={show} onHide={() => this.clearMessage()}>
        <Modal.Header>
          <Modal.Title>
            <Row
              bsStyle={severityClass}
              className='notify-row'>
              <Icon name={icon} className='s2 list-inline' />
              <span>Notification</span>
            </Row>
          </Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <div className={severityClass + ' notification-modal'}>
            {message}
          </div>
          {!isEmpty(details) &&
            (<div className='notification-modal-content'>
              {details}
            </div>)}
        </Modal.Body>
        <Modal.Footer>
          <span className='pull-right'>
            <Row>
              <ButtonGroup className='pull-right'>
                <Button bsStyle='primary'
                  id='btn-notification-close'
                  type='button' onClick={() => this.clearMessage()}>
                  Close
                </Button>
              </ButtonGroup>
            </Row>
          </span>
        </Modal.Footer>
      </Modal>
    )
    /* eslint-enable react/jsx-no-bind, react/jsx-boolean-value */
  }
}

Notification.propTypes = {
  severity: PropTypes.oneOf(['warn', 'error', 'info']).isRequired,
  message: PropTypes.string,
  details: PropTypes.string,
  show: PropTypes.bool,
  handleClearMessage: PropTypes.func
}

const mapDispatchToProps = (dispatch) => {
  return {
    dispatch,
    handleClearMessage: (termId) => dispatch(clearMessage())
  }
}

export default connect(null, mapDispatchToProps)(Notification)
