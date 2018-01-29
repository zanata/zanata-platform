import * as React from 'react'
import * as PropTypes from 'prop-types'
import {IGNORE_CHECK, FUZZY, REJECT} from '../../utils/EnumValueUtils'
import {Label} from 'react-bootstrap'

const mergeOptionValues = [IGNORE_CHECK, FUZZY, REJECT]
export const TMMergeOptionsValuePropType = {
  differentProject: PropTypes.oneOf(mergeOptionValues).isRequired,
  differentDocId: PropTypes.oneOf(mergeOptionValues).isRequired,
  differentContext: PropTypes.oneOf(mergeOptionValues).isRequired,
  fromImportedTM: PropTypes.oneOf(mergeOptionValues).isRequired
}

export const TMMergeOptionsCallbackPropType = {
  onDifferentProjectChange: PropTypes.func.isRequired,
  onDifferentDocIdChange: PropTypes.func.isRequired,
  onDifferentContextChange: PropTypes.func.isRequired
}

const activeOrDefault =
  (isActive, activeStyle) => isActive ? activeStyle : 'default'

export const CopyLabel = ({type, value}) => {
  const isActive = value === type
  switch (type) {
    case IGNORE_CHECK:
      return <Label bsStyle={activeOrDefault(isActive, 'success')}>
        Copy as Translated
      </Label>
    case FUZZY:
      return <Label bsStyle={activeOrDefault(isActive, 'warning')}>
        Copy as Fuzzy
      </Label>
    case REJECT:
      return <Label bsStyle={activeOrDefault(isActive, 'danger')}>
        Discard
      </Label>
  }
  return <span />
}
CopyLabel.propTypes = {
  type: PropTypes.oneOf([IGNORE_CHECK, FUZZY, REJECT]).isRequired,
  value: PropTypes.oneOf([IGNORE_CHECK, FUZZY, REJECT]).isRequired
}
