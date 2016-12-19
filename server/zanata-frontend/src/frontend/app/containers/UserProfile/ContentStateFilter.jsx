import React, { PropTypes } from 'react'
import {
  ContentStates,
  ContentStateStyles
} from '../../constants/Options'
import {
  Base
} from 'zanata-ui'
import { Button } from 'react-bootstrap'

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
      <Button bsStyle='default' key={option}
        active={active}
        className={ContentStateStyles[index]}
        onClick={() => handleFilterChanged(option)}>
        {option}
      </Button>
    )
    /* eslint-enable react/jsx-no-bind */
  })
  return (
    <Base>
      {optionItems}
    </Base>
  )
}

ContentStateFilter.propTypes = {
  selectedContentState: PropTypes.oneOf(ContentStates).isRequired,
  handleFilterChanged: PropTypes.func
}

export default ContentStateFilter
