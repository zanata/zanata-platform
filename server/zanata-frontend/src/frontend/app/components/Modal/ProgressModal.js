import React, {Component} from 'react'
import PropTypes from 'prop-types'
import Modal from './index'
import {
  Button, Row, ProgressBar
} from 'react-bootstrap'
import {
  processStatusPropType, isProcessEnded
} from '../../utils/prop-types-util'

/**
 * This component can be used to show progress of a background task running on
 * server. e.g. CopyTrans, TM merge, Copy Version etc.
 * It offers a 'Cancel Operation' button to stop the task on server.
 * It should also track the progress of the task (before we have websocket ready
 * on server, we have to poll the server to get progress)
 */
class ProgressModal extends Component {
  static propTypes = {
    heading: PropTypes.string,
    show: PropTypes.bool.isRequired,
    onClose: PropTypes.func,
    onCancelOperation: PropTypes.func.isRequired,
    processStatus: processStatusPropType.isRequired,
    queryProgress: PropTypes.func.isRequired
  }
  constructor (props) {
    super(props)
    this.state = {
      showModal: this.props.show,
      // TODO pahuang helper state to stop the loop in dev mode
      stopTimer: false
    }
  }
  hideModal = () => {
    // we have an internal state to close/hide the modal
    // by default when click on the close icon it will hide the modal
    this.setState({showModal: false})
  }
  _queryProgressLoop = () => {
    this.props.queryProgress()
    this.timer = setTimeout(this._queryProgressLoop, 750)
  }
  _stopTimer = () => {
    if (this.timer) {
      clearTimeout(this.timer)
    }
  }
  componentDidMount () {
    this._queryProgressLoop()
  }
  componentWillUpdate (nextProp, nextState) {
    if (isProcessEnded(nextProp.processStatus) || nextState.stopTimer) {
      this._stopTimer()
    }
  }
  componentWillUnmount () {
    this._stopTimer()
  }
  render () {
    const {
      heading, show, onClose, onCancelOperation, processStatus
    } = this.props
    const actualShow = this.state.showModal && show
    const canCancel = isProcessEnded(processStatus)
    const modalHeading = heading &&
      <Modal.Header><Modal.Title>{heading}</Modal.Title></Modal.Header>
    const onHide = onClose || this.hideModal
    return (
      <Modal show={actualShow} onHide={onHide}>
        {modalHeading}
        <Modal.Body>
          <ProgressBar now={processStatus.percentageComplete}
            label={`${processStatus.percentageComplete}%`}
          />
        </Modal.Body>
        <Modal.Footer>
          <span className='bootstrap pull-right'>
            <Row>
              <Button bsStyle='link' className='btn-left' disabled={!canCancel}
                onClick={onCancelOperation}>
                Cancel Operation
              </Button>
            </Row>
          </span>
        </Modal.Footer>
      </Modal>
    )
  }
}

export default ProgressModal
