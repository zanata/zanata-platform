import React, { Component, PropTypes } from 'react'
import { connect } from 'react-redux'
import {
  ButtonRound,
  Icon,
  Modal,
  Row
} from 'zanata-ui'
import { clearMessage } from '../../actions/common'
import { isEmpty } from 'lodash'

/**
 * Notification popup modal that overlays on the page.
 */
class Notification extends Component {

  getSeverityClass (severity) {
    switch (severity) {
      case 'warn':
      case 'error':
        return 'C(danger)'
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
              theme={{ base: {W: 'W(100%)', C: severityClass, Jc: 'Jc(c)'} }}>
              <Icon name={icon} atomic={{m: 'Mend(re)'}} size='2' />
              <span>Notification</span>
            </Row>
          </Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <div className={severityClass + ' My(rh)'}>
            {message}
          </div>
          {!isEmpty(details) &&
            (<div className='Brds(rq) Bxsh(sh1) P(rh) Fz(msn1)'>
              {details}
            </div>)}
        </Modal.Body>
        <Modal.Footer>
          <Row theme={{ base: {j: 'Jc(c)'} }}>
            <ButtonRound type='primary' onClick={() => this.clearMessage()}>
              Close
            </ButtonRound>
          </Row>
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
