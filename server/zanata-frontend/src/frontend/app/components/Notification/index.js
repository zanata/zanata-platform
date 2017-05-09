import Notification from './component'
import { connect } from 'react-redux'
import { clearMessage } from '../../actions/common-actions'

const mapDispatchToProps = (dispatch) => {
  return {
    dispatch,
    handleClearMessage: (termId) => dispatch(clearMessage())
  }
}

export default connect(null, mapDispatchToProps)(Notification)
