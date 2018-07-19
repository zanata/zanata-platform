import React from 'react'
import { Component } from 'react'
import Modal from 'antd/lib/modal'
import 'antd/lib/modal/style/css'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import { replace } from 'lodash'
import { LocaleId, Locale, ProcessStatus } from '../../utils/prop-types-util'
import { getVersionLanguageSettingsUrl } from '../../utils/UrlHelper'
import { STATUS_NEEDS_WORK } from '../../editor/utils/phrase'
import { isProcessEnded } from '../../utils/EnumValueUtils'
import { MTMergeOptions, MTTranslationStatus } from './MTMergeOptions'
import CancellableProgressBar from '../ProgressBar/CancellableProgressBar'

export type MTTranslationStatus = MTTranslationStatus

export interface MTMergeAPIOptions {
  selectedLocales: LocaleId[]
  saveAs: MTTranslationStatus
  overwriteFuzzy?: boolean
}

// Redux state, ie connect's TStateProps
export type MTMergeModalStateProps = Readonly<{
  showMTMerge: boolean
  availableLocales: Locale[]
  processStatus?: ProcessStatus
}>

// Redux dispatch, ie connect's TDispatchProps
export type MTMergeModalDispatchProps = Readonly<{
  mergeProcessFinished: () => void
  queryMTMergeProgress: (url: string) => void
  onSubmit: (projectSlug: string, projectVersion: string, mtMergeOptions: MTMergeAPIOptions) => void
  onCancel: () => void
  onCancelMTMerge: (url: string) => void
}>

// connect's TOwnProps
export type MTMergeModalParams = Readonly<{
  allowMultiple: boolean
  projectSlug: string
  versionSlug: string
}>

type Props = MTMergeModalParams & MTMergeModalStateProps & MTMergeModalDispatchProps

type MTMergeUIState = Readonly<{
  checkedLocales: LocaleId[]
  saveAs: MTTranslationStatus
  overwriteFuzzy: boolean
}>

export class MTMergeModal extends Component<Props, MTMergeUIState> {

  constructor (props: Props) {
    super(props)
    this.state = {
      checkedLocales: [],
      saveAs: STATUS_NEEDS_WORK,
      overwriteFuzzy: false,
    }
  }

  // @ts-ignore any
  componentDidUpdate(prevProps) {
    const { processStatus } = this.props
    if (processStatus !== prevProps.processStatus && (processStatus)) {
      if (isProcessEnded(processStatus)) {
        setTimeout(this.props.mergeProcessFinished, 1000)
      }
    }
  }

  availableLocaleIds() {
    return this.props.availableLocales.map(loc => loc.localeId)
  }

  public render() {
    const {
      showMTMerge,
      projectSlug,
      versionSlug,
      availableLocales,
      processStatus,
      onCancelMTMerge,
      queryMTMergeProgress,
    } = this.props
    const enableSubmit = this.state.checkedLocales.length > 0 && !processStatus
    const queryProgress = () => {
      queryMTMergeProgress(processStatus ? processStatus.url : '')
    }
    const cancelMerge = () => {
      if (this.props.processStatus) {
        const cancelUrl = replace(this.props.processStatus.url,
          '/rest/process/', '/rest/process/cancel/')
        onCancelMTMerge(cancelUrl)
      }
    }
    const closable = processStatus ? isProcessEnded(processStatus) : true
    return (
      <Modal
        title='Machine Translation Merge'
        visible={showMTMerge}
        onOk={this.handleOk}
        onCancel={this.handleCancel}
        destroyOnClose={true}
        maskClosable={closable}
        closable={closable}
        footer={[
          <Button key='back' onClick={this.handleCancel} disabled={!closable}>
            Close
          </Button>,
          <Button key='submit' type='primary' onClick={this.handleOk} disabled={!enableSubmit}>
            Run Merge
          </Button>
        ]}
      >
        {processStatus ?
        (
          <CancellableProgressBar
            buttonLabel={'Cancel Operation'}
            onCancelOperation={cancelMerge}
            processStatus={processStatus}
            queryProgress={queryProgress}
          />
        )
        : availableLocales.length === 0 ?
        <p>This version has no enabled languages. You must enable at least one
          language to use TM merge. <br />
          <a href={getVersionLanguageSettingsUrl(projectSlug, versionSlug)}>
          Language Settings</a>
        </p>
        : (
        <MTMergeOptions
          allowMultiple={this.props.allowMultiple}
          availableLocales={availableLocales}
          checkedLocales={this.state.checkedLocales}
          saveAs={this.state.saveAs}
          overwriteFuzzy={this.state.overwriteFuzzy}
          onLocaleChange={this.onLocaleChange}
          onOverwriteFuzzyChange={this.onOverwriteFuzzyChange}
          onSaveAsChange={this.onSaveAsChange}
          projectSlug={this.props.projectSlug}
          versionSlug={this.props.versionSlug}
        />)}
      </Modal>
    )
  }

  private handleOk = (_: React.MouseEvent<any>) => {
    const opts: MTMergeAPIOptions =  {
      selectedLocales: this.state.checkedLocales,
      saveAs: this.state.saveAs,
      overwriteFuzzy: this.state.overwriteFuzzy
    }
    this.props.onSubmit(this.props.projectSlug, this.props.versionSlug, opts)
  }

  private handleCancel = (_: any) => {
    const { processStatus } = this.props
    this.props.onCancel()
    // If a merge has completed, reload the page to see the MT merge results
    if (processStatus && isProcessEnded(processStatus)) {
      setTimeout(window.location.reload.bind(window.location), 1000)
    }
  }

  private onLocaleChange = (checkedLocales: LocaleId[]) => {
    this.setState({
      checkedLocales
    })
  }

  private onOverwriteFuzzyChange = (overwriteFuzzy: boolean) => {
    this.setState ({
      overwriteFuzzy
    })
  }

  private onSaveAsChange = (saveAs: MTTranslationStatus) => {
    this.setState({
      saveAs
    })
  }

}
