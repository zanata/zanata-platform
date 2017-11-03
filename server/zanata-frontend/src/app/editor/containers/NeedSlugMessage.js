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
    /* eslint-disable max-len */
    return (
      <div>
        <p>Need a URL in the form <code>
          .../project/translate/project-slug/v/version-slug/doc-id-with-slashes?lang=locale-id
        </code></p>
        <p>e.g. <Link
          to="project/translate/my-project/v/my-version/document.txt?lang=fr">
            project/translate/my-project/v/my-version/document.txt?lang=fr
        </Link>
        </p>
      </div>
    )
    /* eslint-enable max-len */
  }
}

const mapStateToProps = (state) => {
  return state
}

export default connect(mapStateToProps)(NeedSlugMessage)
