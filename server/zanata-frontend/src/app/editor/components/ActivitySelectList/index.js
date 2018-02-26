import React from 'react'
import * as PropTypes from 'prop-types'
import SelectButtonList from '../../components/SelectButtonList'
import { filterActivityTypes } from '../../utils/activity-util'

const idType = PropTypes.oneOf(['all', 'comments', 'updates'])

class ActivitySelectList extends React.Component {
  static propTypes = {
    selected: idType.isRequired,
    selectItem: PropTypes.func.isRequired
  }

  render () {
    return (
      <SelectButtonList items={filterActivityTypes}
        className="Button--secondary" selected={this.props.selected}
        selectItem={this.props.selectItem}
      />
    )
  }
}

ActivitySelectList.idType = idType

export default ActivitySelectList
