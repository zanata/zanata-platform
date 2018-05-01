// @ts-nocheck
import React from 'react'
import * as PropTypes from 'prop-types'
import {
  ContentStates,
  ContentStateStyles
} from '../../constants/Options'
import Button from 'antd/lib/button'

/**
 * Component to filter statistics on content state
 * (approved, translated, need work)
 */
const ContentStateFilter = ({
  selectedContentState,
  handleFilterChanged,
  ...props
}) => {
  const optionItems = ContentStates.map(function (option, index) {
    const active = selectedContentState === option

    /* eslint-disable react/jsx-no-bind */
    return (
      <Button key={option} active={active} aria-label='button'
        className={ContentStateStyles[index] + ' btn-default btn-sm'}
        onClick={() => handleFilterChanged(option)}>
        {option}
      </Button>
    )
    /* eslint-enable react/jsx-no-bind */
  })
  return (
    <div>
      {optionItems}
    </div>
  )
}

ContentStateFilter.propTypes = {
  selectedContentState: PropTypes.oneOf(ContentStates).isRequired,
  handleFilterChanged: PropTypes.func
}

export default ContentStateFilter
