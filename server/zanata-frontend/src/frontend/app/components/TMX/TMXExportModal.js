import React, {Component} from 'react'
import PropTypes from 'prop-types'
import {forEach, isUndefined, size} from 'lodash'
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
      srcLanguages,
      handleOnClose,
      handleExportTMX,
      type,
      downloading,
      project,
      version
    } = this.props

    let title = ''
    let question = ''
    switch (type) {
      case TMX_ALL:
        title = 'Export all projects to TMX'
        question = 'Are you sure you want to all projects to TMX?'
        break
      case TMX_PROJECT:
        title = 'Export project \'' + project + '\' to TMX'
        question = 'Are you sure you want to export this project to TMX?'
        break
      case TMX_VERSION:
        title = 'Export version \'' + version + '\' to TMX'
        question = 'Are you sure you want to this version to TMX?'
        break
      default:
        break
    }
    let srcLanguagesRow = []
    if (!isUndefined(srcLanguages)) {
      forEach(srcLanguages, function (srcLang) {
        const localeId = srcLang.localeDetails
          ? srcLang.localeDetails.localeId : 'ALL'

        let downloadTooltip
        let tooltip
        if (srcLang.localeDetails) {
          downloadTooltip = (
            <Tooltip id={'download-' + localeId + '-tooltip'}>
              Download TMX for {localeId}
            </Tooltip>
          )
          tooltip = (
            <Tooltip id={localeId + '-locale-tooltip'}>
              {srcLang.localeDetails.displayName}<br />
              {srcLang.localeDetails.nativeName}
            </Tooltip>
          )
        } else {
          // all locales
          downloadTooltip = (
            <Tooltip id='download-all-tooltip'>
              Produces a TMX file (srclang=*all*)
              which some systems can't import.
            </Tooltip>
          )
          tooltip = (
            <Tooltip id={localeId + '-locale-tooltip'}>
              All source locales
            </Tooltip>
          )
        }

        const docCount = (
          <Tooltip id={localeId + '-doc-tooltip'}>
            {srcLang.docCount} documents
          </Tooltip>
        )
        const downloadTMX = handleExportTMX.bind(undefined,
            srcLang.localeDetails, project, version)
        srcLanguagesRow.push(
          <tr key={localeId}>
            <td>
              <OverlayTrigger placement='left' overlay={tooltip}>
                <Button bsStyle='link' className='button--link'>
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
              <span className='tmx-dl'>
                <OverlayTrigger placement='top' overlay={downloadTooltip}>
                  <Button
                    className={'button--primary ' +
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
          </tr>)
      })
    }
    const warningText = '* All translations of documents for the ' +
     'selected source language will be included.'

    return (
      <Modal id='tmx-export-modal' show={show} onHide={handleOnClose}
        keyboard backdrop>
        <Modal.Header>
          <Modal.Title>{title}</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <span className='tmx-export'>
            <p>{question}</p>
            {size(srcLanguagesRow) > 1 &&
              <p className='lead'>Source languages</p>}
            <Table className='tmx-table'>
              <tbody>
                {srcLanguagesRow}
              </tbody>
            </Table>
            <p className='text-warning'>
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
