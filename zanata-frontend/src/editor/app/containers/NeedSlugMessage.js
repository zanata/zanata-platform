import React from 'react'
import { Link } from 'react-router'
import { connect } from 'react-redux'

/**
 * Component to use when the project and version slug are not both specified,
 * so the editor cannot be loaded.
 *
 * This is mainly for developers to find out immediately why nothing is showing
 * when they try to load the editor without the project and version slug.
 */
class NeedSlugMessage extends React.Component {
  render () {
    return (
      <div>
        <p>Need a URL in the form <code>
          .../project-slug/version-slug/translate
        </code></p>
        <p>If using <code>fake-zanata-server</code>, try using <Link
          to="/tiny-project/1/translate/hello.txt/fr">
              /tiny-project/1/translate/hello.txt/fr
        </Link>
        </p>
      </div>
    )
  }
}

const mapStateToProps = (state) => {
  return state
}

export default connect(mapStateToProps)(NeedSlugMessage)
