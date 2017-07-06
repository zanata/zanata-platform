import React from 'react'
import PropTypes from 'prop-types'
import SelectButtonList from '../../components/SelectButtonList'

const items = [
  {
    id: 'current',
    icon: 'language',
    label: 'Current'
  },
  {
    id: 'all',
    icon: 'language',
    label: 'All'
  },
  {
    id: 'source',
    icon: 'language',
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
        <SelectButtonList items={items} icon='language'
           className="Button--primary" selected={this.props.selected}
           selectItem={this.props.selectItem}
        />
    )
  }
}

export default LanguageSelectList
