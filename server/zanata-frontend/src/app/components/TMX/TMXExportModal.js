import React, {Component} from 'react'
import PropTypes from 'prop-types'
import {size} from 'lodash'
import {connect} from 'react-redux'
import {Icon, Modal} from '../index'
import {Badge, Button, OverlayTrigger, Table, Tooltip} from 'react-bootstrap'

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
      question = 'Are you sure you want to all projects to TMX?'
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
    type: PropTypes.oneOf(TMX_ALL, TMX_PROJECT, TMX_VERSION).isRequired,
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

      const downloadTooltip = (
        <Tooltip id={`download-${localeId}-tooltip`}>
          {downloadTooltipMsg}
        </Tooltip>
      )
      const tooltip = (
        <Tooltip id={`${localeId}-locale-tooltip`}>
          {localeTooltipMsg}
        </Tooltip>
      )

      const docCount = (
        <Tooltip id={`${localeId}-doc-tooltip`}>
          {srcLang.docCount} documents
        </Tooltip>
      )
      const downloadTMX = handleExportTMX.bind(undefined,
        srcLang.localeDetails, project, version)
      return (
        <tr key={localeId}>
          <td>
            <OverlayTrigger placement='left' overlay={tooltip}>
              <Button bsStyle='link'>
                {localeId}
              </Button>
            </OverlayTrigger>
          </td>
          <td>
            <OverlayTrigger placement='top' overlay={docCount}>
              <Badge>
                {srcLang.docCount} <Icon name='document' className='n1' />
              </Badge>
            </OverlayTrigger>
          </td>
          <td>
            <span className='tmxDownload'>
              <OverlayTrigger placement='top' overlay={downloadTooltip}>
                <Button
                  className={'btn-primary ' +
                  (downloading[localeId] ? 'disabled' : '')}
                  disabled={downloading[localeId]}
                  bsStyle='primary'
                  bsSize='small'
                  onClick={downloadTMX}>
                  {downloading[localeId] ? 'Downloading' : 'Download'}
                </Button>
              </OverlayTrigger>
              <span className='asterix'>*</span>
            </span>
          </td>
        </tr>
      )
    })
    const warningText = '* All translations of documents for the ' +
     'selected source language will be included.'

    return (
      <Modal id='tmxExportModal' show={show} onHide={handleOnClose}
        keyboard backdrop>
        <Modal.Header>
          <Modal.Title>{title}</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <span className='tmxExport'>
            <p>{question}</p>
            {size(srcLangRows) > 1 &&
              <p className='lead'>Source languages</p>}
            <Table className='tmxTable'>
              <tbody>
                {srcLangRows}
              </tbody>
            </Table>
            <p className='u-textWarning'>
              {warningText}
            </p>
          </span>
        </Modal.Body>
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
