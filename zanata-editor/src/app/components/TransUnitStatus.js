import React, { PropTypes } from 'react'
import Icon from 'zanata-ui'
import cx from 'classnames'

/**
 * Status indicator showing the state of the translations
 * and some other metadata about a phrase.
 */
const TransUnitStatus = React.createClass({

  propTypes: {
    phrase: PropTypes.object.isRequired
  },

  statusNames: {
    untranslated: 'Untranslated',
    needswork: 'Needs Work',
    translated: 'Translated',
    approved: 'Approved'
  },

  render: function () {
    const phrase = this.props.phrase
    const className = cx('TransUnit-status', {
      // loading if there is an in-progress save object
      'is-loading': !!phrase.inProgressSave
    })

    const comments = phrase.comments
      ? (
      <li className="TransUnit-metaDataItem TransUnit-metaDataComments">
        <button tabIndex="-1"
          className="TransUnit-metaDataButton"
          title={phrase.comments + ' comments'}>
          <Icon name="comment" title="Comments" />
          <br />
          <span>{phrase.comments}</span>
        </button>
      </li>
      )
      : undefined

    const errors = phrase.errors
      ? (
      <li className="TransUnit-metaDataItem TransUnit-metaDataErrors">
        <button tabIndex="-1"
          className="TransUnit-metaDataButton"
          title="1 Error">
          <span className="u-textDanger">
            <Icon name="dot" title="Error" size="n1" />
          </span>
          <br />
          <span>{phrase.comments}</span>
        </button>
      </li>
      )
      : undefined

    return (
      <div className={className}>
        <span className="u-hiddenVisually">
          {this.statusNames[phrase.status]}
        </span>
        <ul className="TransUnit-metaData">
          {comments}
          {errors}
        </ul>
      </div>
    )
  }
})

export default TransUnitStatus
