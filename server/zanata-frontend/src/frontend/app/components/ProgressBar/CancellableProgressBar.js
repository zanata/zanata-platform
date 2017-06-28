import React, {Component} from 'react'
import PropTypes from 'prop-types'
import {
  Button, ProgressBar
} from 'react-bootstrap'
import { processStatusType } from '../../utils/prop-types-util'
import { isProcessEnded } from '../../utils/EnumValueUtils'

/**
 * This component can be used to show progress of a background task running on
 * server. e.g. CopyTrans, TM merge, Copy Version etc.
 * It offers a 'Cancel Operation' button to stop the task on server.
 * It should also track the progress of the task (before we have websocket ready
 * on server, we have to poll the server to get progress)
 */
class CancellableProgressBar extends Component {
  static propTypes = {
    heading: PropTypes.string,
    onCancelOperation: PropTypes.func.isRequired,
    processStatus: processStatusType.isRequired,
    queryProgress: PropTypes.func.isRequired
  }
  static defaultProps = {
    heading: ''
  }
  constructor (props) {
    super(props)
    this.state = {
      // helper state to stop the loop in dev mode (with chrome react add-on)
      stopTimer: false
    }
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
      heading, onCancelOperation, processStatus
    } = this.props
    return (
      <div>
        <ProgressBar now={processStatus.percentageComplete}
          label={`${heading} ${processStatus.percentageComplete}%`}
        />
        <Button bsStyle="primary" className="btn-danger"
          disabled={isProcessEnded(processStatus)}
          onClick={onCancelOperation}>
          Cancel Operation
        </Button>
      </div>
    )
  }
}

export default CancellableProgressBar
