// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import { Icon, Modal } from '../../components'
import { isEmpty } from 'lodash'
import { ButtonGroup, Row } from 'react-bootstrap'
import Button from 'grommet/components/Button'

/**
 * Notification popup modal that overlays on the page.
 */
class Notification extends Component {
  static propTypes = {
    severity: PropTypes.oneOf(['warn', 'error', 'info']).isRequired,
    message: PropTypes.string,
    details: PropTypes.string,
    show: PropTypes.bool,
    handleClearMessage: PropTypes.func
  }

  getSeverityClass = (severity) => {
    switch (severity) {
      case 'warn':
        return 'warning'
      case 'error':
        return 'danger'
      default:
        return ''
    }
  }

  getIcon = (severity) => {
    switch (severity) {
      case 'warn':
      case 'error':
        return 'warning'
      default:
        return 'info'
    }
  }

  clearMessage = () => {
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
      <Modal show={show} onHide={this.clearMessage}>
        <Modal.Header>
          <Modal.Title>
            <Row
              bsStyle={severityClass}
              className='rowNotify'>
              <Icon name={icon} className='s3 listInline' />
              <span> Notification</span>
            </Row>
          </Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <div className={severityClass + ' notification-modal'}>
            {message}
          </div>
          {!isEmpty(details) &&
            (<div className='modalNotification-content'>
              {details}
            </div>)}
        </Modal.Body>
        <Modal.Footer>
          <span className='u-pullRight'>
            <Row>
              <ButtonGroup className='u-pullRight'>
                <Button label='Close'
                  onClick={this.clearMessage}
                  primary />
              </ButtonGroup>
            </Row>
          </span>
        </Modal.Footer>
      </Modal>
    )
    /* eslint-enable react/jsx-no-bind, react/jsx-boolean-value */
  }
}

export default Notification
