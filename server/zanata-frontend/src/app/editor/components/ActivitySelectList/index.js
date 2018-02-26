import React from 'react'
import * as PropTypes from 'prop-types'
import SelectButtonList from '../../components/SelectButtonList'
import {
  activityItems, filterActivityTypes
} from '../../utils/activity-util'

const idType = PropTypes.oneOf(filterActivityTypes)

class ActivitySelectList extends React.Component {
  static propTypes = {
    selected: idType.isRequired,
    selectItem: PropTypes.func.isRequired
  }

  render () {
    return (
      <SelectButtonList items={activityItems}
        className="Button--secondary" selected={this.props.selected}
        selectItem={this.props.selectItem}
      />
    )
  }
}

ActivitySelectList.idType = idType

export default ActivitySelectList
