// @ts-nocheck
import React from 'react'
import * as PropTypes from 'prop-types'
import {
  ContentStates,
  ContentStateStyles
} from '../../constants/Options'
import Tag from 'antd/lib/tag'
import 'antd/lib/tag/style/css'

/**
 * Component to filter statistics on content state
 * (approved, translated, need work)
 */
const ContentStateFilter = ({
  selectedContentState,
  handleFilterChanged,
  ...props
}) => {
  const { CheckableTag } = Tag

  function onChange (e, option) {
    handleFilterChanged(option)
  }

  const optionItems = ContentStates.map(function (option, index) {
    const active = selectedContentState === option
    /* eslint-disable react/jsx-no-bind */
    return (
      <CheckableTag key={option} checked={active}
        onChange={(e) => onChange(e, option)}
        className={ContentStateStyles[index] + ' f6'}>
        {option}
      </CheckableTag>
    )
    /* eslint-enable react/jsx-no-bind */
  })
  return (
    <React.Fragment>
      {optionItems}
    </React.Fragment>
  )
}

ContentStateFilter.propTypes = {
  selectedContentState: PropTypes.oneOf(ContentStates).isRequired,
  handleFilterChanged: PropTypes.func
}

export default ContentStateFilter
