import React from 'react'
import { Component } from 'react'
import Modal from 'antd/lib/modal'
import 'antd/lib/modal/style/css'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import { LocaleId, Locale, ProcessStatus } from '../../utils/prop-types-util'
import { STATUS_NEEDS_WORK } from '../../editor/utils/phrase'
import { MTMergeOptions, MTTranslationStatus } from './MTMergeOptions'
import CancellableProgressBar from '../ProgressBar/CancellableProgressBar'
import { getVersionLanguageSettingsUrl } from '../../utils/UrlHelper'

export type MTTranslationStatus = MTTranslationStatus

export interface MTMergeAPIOptions {
  selectedLocales: LocaleId[]
  saveAs?: MTTranslationStatus
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
    // Typical usage (don't forget to compare props):
    const { processStatus } = this.props
    if (processStatus !== prevProps.processStatus
      && (processStatus && processStatus.percentageComplete >= 100)) {
        this.props.mergeProcessFinished()
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
      // notification,
      // triggered,
      processStatus,
      queryMTMergeProgress,
    } = this.props
    const enableSubmit = this.state.checkedLocales.length > 0
    const queryProgress = () => {
      queryMTMergeProgress(processStatus ? processStatus.url : '')
    }
    // TODO: disable or implement mt merge cancel
    const cancelMerge = () => 'cancel'
    return (
      <Modal
        title="Machine Translation Batch Merge"
        visible={showMTMerge}
        onOk={this.handleOk}
        onCancel={this.handleCancel}
        destroyOnClose={true}
        footer={[
          <Button key="back" onClick={this.handleCancel}>
            Cancel
          </Button>,
          <Button key="submit" type="primary" onClick={this.handleOk} disabled={!enableSubmit}>
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
    this.props.onCancel()
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
