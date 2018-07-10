// @ts-nocheck
import React from 'react'
import * as PropTypes from 'prop-types'
import {
  ContentStates,
  ContentStateStyles
} from '../../constants/Options'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'

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
        className={ContentStateStyles[index] + ' btn-default'}
        onClick={() => handleFilterChanged(option)} size='small'>
        {option}
      </Button>
    )
    /* eslint-enable react/jsx-no-bind */
  })
  return (
    <>
      {optionItems}
    </>
  )
}

ContentStateFilter.propTypes = {
  selectedContentState: PropTypes.oneOf(ContentStates).isRequired,
  handleFilterChanged: PropTypes.func
}

export default ContentStateFilter
