// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import {size} from 'lodash'
import {connect} from 'react-redux'
import {Icon} from '../index'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/index.less'
import Col from 'antd/lib/col'
import 'antd/lib/col/style/'
import Modal from 'antd/lib/modal'
import 'antd/lib/modal/style/index.less'
import Row from 'antd/lib/row'
import 'antd/lib/row/style/'
import Tooltip from 'antd/lib/tooltip'
import 'antd/lib/tooltip/style/index.less'

import {
  exportTMX,
  showExportTMXModal,
  TMX_ALL,
  TMX_PROJECT,
  TMX_VERSION,
  tmxInitialLoad
} from '../../actions/tmx-actions'

const toDownloadTooltipMsg = (localeDetails) => {
  return localeDetails
    ? `Download TMX for ${localeDetails.localeId}`
    : "Produces a TMX file (srclang=*all*) which some systems can't import."
}

const toLocaleTooltipMsg = (localeDetails) => {
  return localeDetails
    ? `${localeDetails.displayName} - ${localeDetails.nativeName}`
    : 'All source locales'
}

const localeDetailsToLocaleId = (localeDetails) => {
  return localeDetails ? localeDetails.localeId : 'ALL'
}

const fromTMXTypeToTitleAndQuestion = (tmxType, project, version) => {
  let title, question
  switch (tmxType) {
    case TMX_ALL:
      title = 'Export all projects to TMX'
      question = 'Are you sure you want to export all projects to TMX?'
      break
    case TMX_PROJECT:
      title = `Export project '${project}' to TMX`
      question = 'Are you sure you want to export this project to TMX?'
      break
    case TMX_VERSION:
      title = `Export version '${version}' to TMX`
      question = 'Are you sure you want to export this version to TMX?'
      break
    default:
      break
  }
  return {title, question}
}

class TMXExportModal extends Component {
  static propTypes = {
    show: PropTypes.bool,
    showSourceLanguages: PropTypes.bool,
    type: PropTypes.oneOf([TMX_ALL, TMX_PROJECT, TMX_VERSION]).isRequired,
    srcLanguages: PropTypes.arrayOf(PropTypes.object),
    handleOnClose: PropTypes.func,
    handleExportTMX: PropTypes.func,
    handleInitLoad: PropTypes.func.isRequired,
    project: PropTypes.string,
    version: PropTypes.string,
    downloading: PropTypes.object
  }

  componentDidMount () {
    const { project, version } = this.props
    this.props.handleInitLoad(project, version)
  }

  render () {
    const {
      show,
      srcLanguages = [],
      handleOnClose,
      handleExportTMX,
      type,
      downloading,
      project,
      version
    } = this.props

    const {title, question} =
      fromTMXTypeToTitleAndQuestion(type, project, version)

    const srcLangRows = srcLanguages.map(srcLang => {
      const localeDetails = srcLang.localeDetails
      const localeId = localeDetailsToLocaleId(localeDetails)
      const downloadTooltipMsg = toDownloadTooltipMsg(localeDetails)
      const localeTooltipMsg = toLocaleTooltipMsg(localeDetails)

      const downloadTMX = handleExportTMX.bind(undefined,
        srcLang.localeDetails, project, version)
      return (
        <Row type="flex" justify="center" key={localeId}>
          <Col span={6}>
            <Tooltip title={localeTooltipMsg}>
              <a href='#'>{localeId}</a>
            </Tooltip>
          </Col>
          <Col span={6}>
            <Tooltip title={`${srcLang.docCount} Documents`}>
              <Icon name='document' className='n1' /> <span>
              {srcLang.docCount} Documents</span>
            </Tooltip>
          </Col>
          <Col span={6}>
            <Tooltip title={downloadTooltipMsg}>
              <Button
                type='primary'
                size='small'
                disabled={downloading[localeId]}
                onClick={downloadTMX}>
                {downloading[localeId] ? 'Downloading' : 'Download'}
              </Button>
            </Tooltip>
            <span className='asterix'>*</span>
          </Col>
        </Row>
      )
    })
    const warningText = '* All translations of documents for the ' +
     'selected source language will be included.'

    return (
      <Modal
        title={title}
        visible={show}
        onCancel={handleOnClose}
        style={{ textAlign: 'center' }}
        footer={null}
        >
        <span className='tmxExport'>
          <p>{question}</p>
          {size(srcLangRows) > 1 &&
            <p className='lead'>Source languages</p>}
            {srcLangRows}
          <p className='u-textWarning'>
            {warningText}
          </p>
        </span>
      </Modal>
    )
  }
}

const mapStateToProps = (state) => {
  const { tmxExport } = state.tmx
  return {
    show: tmxExport.showModal,
    srcLanguages: tmxExport.sourceLanguages,
    showSourceLanguages: tmxExport.showSourceLanguages,
    type: tmxExport.type,
    downloading: tmxExport.downloading
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    dispatch,
    handleOnClose: () => {
      dispatch(showExportTMXModal(false))
    },
    handleExportTMX: (localeDetails, project, version) => {
      const localeId = localeDetails ? localeDetails.localeId : undefined
      dispatch(exportTMX(localeId, project, version))
    },
    handleInitLoad: (project, version) => {
      dispatch(tmxInitialLoad(project, version))
    }
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(TMXExportModal)
