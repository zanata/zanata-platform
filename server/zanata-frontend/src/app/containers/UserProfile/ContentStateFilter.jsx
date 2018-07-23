// @ts-nocheck
import React from 'react'
import * as PropTypes from 'prop-types'
import {
  ContentStates,
  ContentStateStyles
} from '../../constants/Options'
// import Button from 'antd/lib/button'
// import 'antd/lib/button/style/css'
import Radio from 'antd/lib/radio'
import 'antd/lib/radio/style/css'

const RadioButton = Radio.Button
const RadioGroup = Radio.Group

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
    // const active = selectedContentState === option
    // const filterChange = () => handleFilterChanged(option)
    /* eslint-disable react/jsx-no-bind */
    return (
      <RadioButton
        key={option}
        value={option}
        // active={active}
        aria-label='radio'
        className={ContentStateStyles[index]}
        size='small'>
        {option}
      </RadioButton>
    )
    /* eslint-enable react/jsx-no-bind */
  })
  const handleChange = (event) => {
    const newContentState = event.target.value
    handleFilterChanged(newContentState)
  }
  return (
    <RadioGroup
      buttonStyle='solid'
      value={selectedContentState}
      onChange={handleChange}>
      {optionItems}
    </RadioGroup>
  )
}

ContentStateFilter.propTypes = {
  selectedContentState: PropTypes.oneOf(ContentStates).isRequired,
  handleFilterChanged: PropTypes.func
}

export default ContentStateFilter
