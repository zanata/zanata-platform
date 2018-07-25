import React, { Component } from 'react'
import Alert from 'antd/lib/alert'
import 'antd/lib/alert/style/css'
import Card from 'antd/lib/card'
import 'antd/lib/card/style/css'
import Checkbox from 'antd/lib/checkbox'
import 'antd/lib/checkbox/style/css'
import Icon from 'antd/lib/icon'
import 'antd/lib/icon/style/css'
import Switch from 'antd/lib/switch'
import 'antd/lib/switch/style/css'
import Select from 'antd/lib/select'
import 'antd/lib/select/style/css'
import { CheckboxChangeEvent } from 'antd/lib/checkbox'
import { CheckboxValueType } from 'antd/lib/checkbox/Group'
import { LocaleId, Locale } from '../../utils/prop-types-util'
import { STATUS_NEEDS_WORK, STATUS_TRANSLATED } from '../../editor/utils/phrase'

const CheckboxGroup = Checkbox.Group
const Option = Select.Option

export type MTTranslationStatus = typeof STATUS_NEEDS_WORK | typeof STATUS_TRANSLATED

// TODO these options should be hidden until the MT REST service can handle them
const otherOptions = true

export type MTMergeOptionsParams = Readonly<{
  allowMultiple: boolean
  projectSlug: string
  versionSlug: string
  availableLocales: Locale[]
  checkedLocales: LocaleId[]
  saveAs: MTTranslationStatus
  overwriteFuzzy: boolean
}>

type Props = MTMergeOptionsParams & Readonly<{
  onLocaleChange: (selectedLocales: LocaleId[]) => void
  onSaveAsChange: (saveAs: MTTranslationStatus) => void
  onOverwriteFuzzyChange: (overwriteFuzzy: boolean) => void
}>

export class MTMergeOptions extends Component<Props> {

  constructor (props: Props) {
    super(props)
  }

  public render() {
    // are all the boxes checked?
    const allChecked = this.props.availableLocales.length === this.props.checkedLocales.length
    // are some of the boxes checked? (not zero, not all)
    const someChecked = ![0, this.props.availableLocales.length].includes(this.props.checkedLocales.length)

    return (
      <React.Fragment>
        <Alert message="Have you run TM Merge first?" type="warning"
          showIcon />
        <br />
        <Alert message="Only .po (gettext) and .properties files are supported"
          type="info" showIcon/>
        {/* Select Languages */}
        <h3 className="txt-info mt4 fw5">
          <Icon type="global" className="mr2 txt-neutral"/>
          {this.props.allowMultiple ? "Languages" : "Language"}
        </h3>
        {this.props.allowMultiple
          ? <div>
            {/*this is the "check all" checkbox*/}
            <div style={{borderBottom: '1px solid #E9E9E9'}}>
              <Checkbox
                checked={allChecked}
                indeterminate={someChecked}
                onChange={this.onCheckAllChange}
              >
                All languages
              </Checkbox>
            </div>
            <br />
            <CheckboxGroup
              onChange={this.onCheckboxGroupChange} value={this.props.checkedLocales}
            >
            {this.props.availableLocales.map(loc =>
              <Checkbox key={loc.localeId} value={loc.localeId}>
                {loc.displayName}
              </Checkbox>)}
            </CheckboxGroup>
          </div>
          : <Select
              showSearch
              style={{ width: '100%' }}
              placeholder="Select a locale"
              onChange={this.onSelectChange}
              filterOption={(input, option) => (
                // @ts-ignore
                (option.props.value + option.props.title).toLowerCase())
                .indexOf(input.toLowerCase()) >= 0}
            >
            {this.props.availableLocales.sort(this.compareLocId).map(loc =>
              <Option key={loc.localeId} value={loc.localeId} title={loc.displayName}>
                <span className='blue'>{loc.localeId}</span> {loc.displayName}
              </Option>)}
          </Select>
        }

        {/* Other options */}
        {otherOptions &&
        <div className="mt3">
          <Card hoverable>
            <div className="pb1">
              <Alert message="MT fill will save translations with validation errors as fuzzy" type="warning"
                 showIcon/>
              <h3 className="txt-info mt-toggleHeading">
                <span className="mr2 fw5">
                  Save as
                </span>
                <span className="di">
                  <Switch className="transSwitch" checkedChildren="translated"
                    unCheckedChildren="fuzzy" onChange={this.onSaveAsChange}/>
              </span>
              </h3>
            </div>
            {/* TODO: Consider removing this option entirely */}
            {/* <Switch size="small" checked={this.props.overwriteFuzzy} onChange={this.props.onOverwriteFuzzyChange} />
            <span className="txt-primary">
              Overwrite existing fuzzy translations with MT
            </span> */}
          </Card>
        </div>
        }
      </React.Fragment>
    )
  }

  // Sort comparitor by locale Id
  private compareLocId (a: Locale, b: Locale) {
    return a.localeId < b.localeId ? -1 : a.localeId > b.localeId ? 1 : 0
  }

  private availableLocaleIds() {
    return this.props.availableLocales.map(loc => loc.localeId)
  }

  private onSaveAsChange = (checked: boolean) => {
    this.props.onSaveAsChange(checked ? STATUS_TRANSLATED : STATUS_NEEDS_WORK)
  }

  // @ts-ignore any
  private onSelectChange = (value) => {
    const checkedLocale = value as LocaleId
    const checkedLocales = [checkedLocale]
    this.onLocaleChange(checkedLocales)
  }

  private onCheckboxGroupChange = (checked: CheckboxValueType[]) => {
    const checkedLocales = checked as LocaleId[]
    this.onLocaleChange(checkedLocales)
  }

  private onCheckAllChange = (e: CheckboxChangeEvent) => {
    const checkAll = e.target.checked
    const checkedLocales = checkAll ? this.availableLocaleIds() : []
    this.onLocaleChange(checkedLocales)
  }

  private onLocaleChange(checkedLocales: LocaleId[]) {
    this.setState({
      checkedLocales
    })
    this.props.onLocaleChange(checkedLocales)
  }

}
