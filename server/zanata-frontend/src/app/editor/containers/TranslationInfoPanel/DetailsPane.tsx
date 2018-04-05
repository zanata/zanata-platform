import React from 'react'
import * as PropTypes from 'prop-types'
import { isEmpty, isUndefined } from 'lodash'
import Icon from '../../../components/Icon'
import { FormattedDate, FormattedTime } from 'react-intl'
import { SelectedPhrase } from '../../utils/phrase'

interface DetailsPaneProps {
  hasSelectedPhrase: boolean,
  selectedPhrase?: SelectedPhrase,
  isRTL?: boolean
}

const DetailsPane: React.SFC<DetailsPaneProps> = ({
  hasSelectedPhrase,
  selectedPhrase,
  isRTL
}) => {
  if (!hasSelectedPhrase) {
    return <span>Select a phrase to see details.</span>
  }
  const detailItem = (label, value) => {
    const valueDisplay = isEmpty(value)
        ? <span className="SidebarEditor-details--nocontent">No content</span>
        : <span className="SidebarEditor-details--content">{value}</span>
    return (
      <li>
        <span>{label}</span> {valueDisplay}
      </li>
    )
  }
  const lastModifiedDisplay = (lastModifiedBy, lastModifiedTime) => {
    if (isUndefined(lastModifiedBy) && isUndefined(lastModifiedTime)) {
      return undefined
    }
    const modifiedByIcon = isUndefined(lastModifiedBy) ? undefined
        : <Icon name="user" className="n1" />
    const modifiedTimeIcon = isUndefined(lastModifiedTime) ? undefined
        : <Icon name="clock" className="n1" />
    const modifiedDate = isUndefined(lastModifiedTime) ? undefined
        : <FormattedDate value={lastModifiedTime} format="medium" />
    const modifiedTime = isUndefined(lastModifiedTime) ? undefined
        : <FormattedTime value={lastModifiedTime} />
    return (
      <span>
        {modifiedByIcon} {lastModifiedBy} {modifiedTimeIcon
        } {modifiedDate} {modifiedTime}
      </span>
    )
  }
  const {
    msgctxt,
    resId,
    sourceComment,
    sourceFlags,
    sourceReferences
  } = selectedPhrase
  const directionClass = isRTL ? 'rtl' : 'ltr'
  return (
    <ul className={directionClass + ' SidebarEditor-details'}>
      {detailItem('Resource ID', resId)}
      {detailItem('Message Context', msgctxt)}
      {detailItem('Reference', sourceReferences)}
      {detailItem('Flags', sourceFlags)}
      {detailItem('Source Comment', sourceComment)}
      {detailItem('Last Modified',
          lastModifiedDisplay(selectedPhrase.lastModifiedBy, selectedPhrase.lastModifiedTime))}
    </ul>
  )
}

DetailsPane.propTypes = {
  hasSelectedPhrase: PropTypes.bool.isRequired,
  selectedPhrase: PropTypes.shape({
    msgctxt: PropTypes.string,
    resId: PropTypes.string.isRequired,
    sourceComment: PropTypes.string,
    sourceFlags: PropTypes.string,
    sourceReferences: PropTypes.string,
    lastModifiedBy: PropTypes.string,
    lastModifiedTime: PropTypes.instanceOf(Date),
    revision: PropTypes.number
  }),
  isRTL: PropTypes.bool.isRequired
}

export default DetailsPane
