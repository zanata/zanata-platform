import React from 'react'
import Select from 'antd/lib/select'
import 'antd/lib/select/style/css'
import { Locale } from '../../utils/prop-types-util'
const Option = Select.Option

// Sort comparitor by locale Id
const compareLocId = (a: Locale, b: Locale) => {
    return a.localeId < b.localeId ? -1 : a.localeId > b.localeId ? 1 : 0
}
// Search by both id and displayname
const filterOpt = (input: string, option: any) => (
    (option.props.value + option.props.title).toLowerCase())
    .indexOf(input.toLowerCase()) >= 0

/**
 * Sorted and Searchable Locale Selector with options showing ID and displayname
 */
const LocaleSelect: React.SFC<LocaleSelectProps> = ({ locales, onChange, style }) => {
  return (
    <Select
      showSearch
      style={style}
      placeholder="Select a locale"
      onChange={onChange}
      filterOption={filterOpt}
    >
      {locales.sort(compareLocId).map(loc =>
        <Option key={loc.localeId} value={loc.localeId} title={loc.displayName}>
          <span className='blue'>{loc.localeId}</span> {loc.displayName}
        </Option>)}
    </Select>
  )
}

interface LocaleSelectProps {
  locales: Locale[]
  onChange: (value: any) => void
  style: object
}

export default LocaleSelect
