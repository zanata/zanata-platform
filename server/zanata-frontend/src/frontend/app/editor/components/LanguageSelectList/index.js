import React from 'react'
import PropTypes from 'prop-types'
import SelectButtonList from '../../components/SelectButtonList'

const items = [
  {
    id: 'current',
    label: 'Current'
  },
  {
    id: 'all',
    label: 'All'
  },
  {
    id: 'source',
    label: 'Source'
  }
]

class LanguageSelectList extends React.Component {
  static propTypes = {
    selected: PropTypes.oneOf(['current', 'all', 'source']),
    selectItem: PropTypes.func.isRequired
  }

  render () {
    return (
      <SelectButtonList items={items}
        className="Button--primary" selected={this.props.selected}
        selectItem={this.props.selectItem}
      />
    )
  }
}

export default LanguageSelectList
