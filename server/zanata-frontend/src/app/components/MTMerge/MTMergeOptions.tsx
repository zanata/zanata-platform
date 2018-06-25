import React, { Component } from 'react'
import 'antd/lib/modal/style/css'
import Alert from 'antd/lib/alert'
import 'antd/lib/alert/style/css'
import Icon from 'antd/lib/icon'
import 'antd/lib/icon/style/css'
import Checkbox from 'antd/lib/checkbox'
import 'antd/lib/checkbox/style/css'
import { CheckboxChangeEvent } from 'antd/lib/checkbox'
import { Radio } from 'antd'
import 'antd/lib/radio/style/css'
import Switch from 'antd/lib/switch'
import 'antd/lib/switch/style/css'
import Card from 'antd/lib/card'
import 'antd/lib/card/style/css'
import { CheckboxValueType } from 'antd/lib/checkbox/Group';
import { LocaleId, Locale } from '../../utils/prop-types-util'
import { STATUS_NEEDS_WORK, STATUS_TRANSLATED } from '../../editor/utils/phrase';
import { RadioChangeEvent } from 'antd/lib/radio';

const CheckboxGroup = Checkbox.Group

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
      // TODO use a Fragment
      <div>
        <Alert message="Have you run TM Merge first?" type="warning"
          showIcon/>

        {/* Select Languages */}
        <h3 className="txt-info mt4">
          <Icon type="global" />
          {this.props.allowMultiple ? "Languages" : "Language"}
        </h3>
        {this.props.allowMultiple ?
          <div>
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
        :
        <Radio.Group onChange={this.onRadioGroupChange}>
          {this.props.availableLocales.map(loc =>
            <Radio key={loc.localeId} value={loc.localeId} checked={this.props.checkedLocales.includes(loc.localeId)}>
              {loc.displayName}
            </Radio>)}
        </Radio.Group>
        }

        {/* Other options */}
        {otherOptions &&
        <div className="mt4 mb4">
          <Card hoverable>
            <div>
              <h3 className="txt-info mb4">
                <span className="mr2">
                  Save as
                </span>
                <span className="di">
                  <Switch className="transSwitch" checkedChildren="translated"
                    unCheckedChildren="fuzzy" onChange={this.onSaveAsChange}/>
              </span>
              </h3>
            </div>

            <Switch size="small" checked={this.props.overwriteFuzzy} onChange={this.props.onOverwriteFuzzyChange} />
            <span className="txt-primary">
              Overwrite existing fuzzy translations with MT
            </span>
          </Card>
        </div>
        }
      </div>
    )
  }

  private availableLocaleIds() {
    return this.props.availableLocales.map(loc => loc.localeId)
  }

  private onSaveAsChange = (checked: boolean) => {
    this.props.onSaveAsChange(checked ? STATUS_TRANSLATED : STATUS_NEEDS_WORK)
  }

  private onRadioGroupChange = (e: RadioChangeEvent) => {
    const checkedLocale = e.target.value as LocaleId
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
