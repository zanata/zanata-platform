import React from 'react'
import PropTypes from 'prop-types'
import SelectButtonList from '../../components/SelectButtonList'

const items = [
  {
    id: 'all',
    icon: 'clock',
    label: 'All'
  },
  {
    id: 'comments',
    icon: 'comment',
    label: 'Comments'
  },
  {
    id: 'updates',
    icon: 'refresh',
    label: 'Updates'
  }
]

class ActivitySelectList extends React.Component {
  static propTypes = {
    selected: PropTypes.oneOf(['all', 'comments', 'updates']),
    selectItem: PropTypes.func.isRequired
  }

  render () {
    return (
      <SelectButtonList items={items}
        className="Button--secondary" selected={this.props.selected}
        selectItem={this.props.selectItem}
      />
    )
  }
}

export default ActivitySelectList
