// @ts-nocheck
import React from 'react'
import * as PropTypes from 'prop-types'
import { ButtonToolbar } from 'react-bootstrap'
import SelectButton from '../SelectButton'

const SelectButtonList = ({ items, selected, className, selectItem }) => (
  <ButtonToolbar>
    {items.map(({ id, icon, label }) => (
      <SelectButton
        id={id}
        key={id}
        icon={icon}
        buttonName={label}
        className={className}
        selected={id === selected}
        selectItem={selectItem}
      />
    ))
    }
  </ButtonToolbar>
)

SelectButtonList.propTypes = {
  items: PropTypes.arrayOf(PropTypes.shape({
    icon: PropTypes.oneOf(['clock', 'comment', 'refresh', 'language']),
    buttonName: PropTypes.string,
    className: PropTypes.string
  })).isRequired,
  selected: PropTypes.string,
  className: PropTypes.string,
  selectItem: PropTypes.func.isRequired
}

export default SelectButtonList
