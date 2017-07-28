import React, {Component} from 'react'
import PropTypes from 'prop-types'
import {connect} from 'react-redux'
import Helmet from 'react-helmet'
import TMMergeModal from './TMMergeModal'

import {
  toggleTMMergeModal
} from '../../actions/version-actions'

/**
 * Root component for Project Version Page
 */
class ProjectVersion extends Component {
  static propTypes = {
    toggleTMMergeModal: PropTypes.func.isRequired,
    params: PropTypes.shape({
      project: PropTypes.string.isRequired,
      version: PropTypes.string.isRequired
    })
  }

  render () {
    const { params } = this.props
    return (
      <div className='page wide-view-theme' id='version'>
        <Helmet title='ProjectVersion' />
        <div className='center-block'>
          <TMMergeModal projectSlug={params.project}
            versionSlug={params.version} />
        </div>
      </div>
    )
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    toggleTMMergeModal: () => {
      dispatch(toggleTMMergeModal())
    }
  }
}

export default connect(undefined, mapDispatchToProps)(ProjectVersion)
