import * as PropTypes from 'prop-types'
import React from 'react'
import { Icon } from '../../components'
import cx from 'classnames'
import { Phrase } from '../utils/phrase'

const statusNames = {
  untranslated: 'Untranslated',
  needswork: 'Needs Work',
  translated: 'Translated',
  approved: 'Approved',
  rejected: 'Rejected',
}

interface TransUnitStatusProps {
  phrase: Phrase
}

/**
 * Status indicator showing the state of the translations
 * and some other metadata about a phrase.
 */
class TransUnitStatus extends React.Component<TransUnitStatusProps, any> {
  // @ts-ignore: unused
  private static propTypes = {
    phrase: PropTypes.object.isRequired
  }

  public render () {
    const phrase = this.props.phrase
    const className = cx('TransUnit-status', {
      // loading if there is an in-progress save object
      'is-loading': !!phrase.inProgressSave
    })

    const comments = phrase.comments
      ? (
      <li className="TransUnit-metaDataItem TransUnit-metaDataComments">
        <button tabIndex={-1}
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
        <button tabIndex={-1}
          className="TransUnit-metaDataButton"
          title="1 Error">
          <span className="u-textDanger">
            <Icon name="dot" title="Error" className="n1" />
          </span>
        </button>
      </li>
      )
      : undefined

    const mtIndicator = phrase.transSourceType === 'MT' && (
      <li className="TransUnit-metaDataItem TransUnit-metaDataMT">
        <button tabIndex={-1}
          className="TransUnit-metaDataButton">
          <span className="b">MT</span>
        </button>
      </li>
    )

    return (
      <div className={className}>
        <span className="u-hiddenVisually">
          {statusNames[phrase.status!]}
        </span>
        <ul className="TransUnit-metaData">
          {comments}
          {errors}
          {mtIndicator}
        </ul>
      </div>
    )
  }
}

export default TransUnitStatus
