// @ts-nocheck
import React from 'react'
import * as PropTypes from 'prop-types'
import {IGNORE_CHECK, FUZZY, REJECT} from '../../utils/EnumValueUtils'
import { Tag } from 'antd'

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
      return <Tag className={activeOrDefault(isActive, 'bg-success white fw6')}>
        Copy as Translated
      </Tag>
    case FUZZY:
      return <Tag className={activeOrDefault(isActive, 'bg-warn white fw6')}>
        Copy as Fuzzy
      </Tag>
    case REJECT:
      return <Tag className={activeOrDefault(isActive, 'bg-error white fw6')}>
        Discard
      </Tag>
  }
  return <span />
}
CopyLabel.propTypes = {
  type: PropTypes.oneOf([IGNORE_CHECK, FUZZY, REJECT]).isRequired,
  value: PropTypes.oneOf([IGNORE_CHECK, FUZZY, REJECT]).isRequired
}
