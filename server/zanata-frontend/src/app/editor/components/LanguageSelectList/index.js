import * as React from 'react'
import * as PropTypes from 'prop-types'
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

const idType = PropTypes.oneOf(['current', 'all', 'source'])

class LanguageSelectList extends React.Component {
  static propTypes = {
    selected: idType.isRequired,
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

LanguageSelectList.idType = idType

export default LanguageSelectList
