import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { connect } from 'react-redux'
import { isEmpty, includes } from 'lodash'
import { Button, ButtonGroup } from 'react-bootstrap'

import {
  LoaderText,
  Modal,
  Select
} from '../../components'

import {
  glossaryImportFile,
  glossaryUpdateImportFile,
  glossaryToggleImportFileDisplay,
  glossaryUpdateImportFileLocale,
  FILE_TYPES
} from '../../actions/glossary-actions'

class ImportModal extends Component {
  static propTypes = {
    transLocales: PropTypes.array,
    srcLocale: PropTypes.object,
    file: PropTypes.object,
    show: PropTypes.bool,
    status: PropTypes.number,
    transLocale: PropTypes.object,
    handleImportFile: PropTypes.func,
    handleImportFileChange: PropTypes.func,
    handleImportFileDisplay: PropTypes.func,
    handleImportFileLocaleChange: PropTypes.func
  }

  getUploadFileExtension = (file) => {
    return file ? file.name.split('.').pop() : ''
  }

  isSupportedFile = (extension) => {
    return includes(FILE_TYPES, extension)
  }

  render () {
    const {
      transLocales,
      srcLocale,
      file,
      show,
      status,
      transLocale,
      handleImportFile,
      handleImportFileChange,
      handleImportFileDisplay,
      handleImportFileLocaleChange
    } = this.props

    const fileExtension = this.getUploadFileExtension(file)
    const isUploading = status !== -1
    const locale = srcLocale.locale ? srcLocale.locale.displayName : ''
    let messageSection
    let langSelection
    let disableUpload = true

    if (this.isSupportedFile(fileExtension) && !isUploading) {
      if (fileExtension === 'po') {
        if (!isEmpty(transLocale)) {
          disableUpload = false
        }
        langSelection = (<Select
          name='glossary-import-language-selection'
          className='modalSelect'
          placeholder='Select a translation language…'
          value={transLocale}
          options={transLocales}
          onChange={handleImportFileLocaleChange}
        />)
      } else {
        disableUpload = false
      }
    }

    if (file && !this.isSupportedFile(fileExtension)) {
      messageSection = (<div className='u-textUnsupported'>
        File '{file.name}' is not supported.
      </div>)
    }
    const uploadGlossaryUrl =
      'http://docs.zanata.org/en/release/user-guide/glossary/upload-glossaries/'
    /* eslint-disable react/jsx-no-bind */
    return (
      <Modal
        show={show}
        onHide={() => handleImportFileDisplay(false)}
        rootClose>
        <Modal.Header>
          <Modal.Title>Import Glossary</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <input
            type='file'
            onChange={handleImportFileChange}
            ref='file'
            multiple={false}
            disabled={isUploading}
            className='modalInput' />
          {messageSection}
          {isUploading
            ? transLocale && (<span className='modalLocale'>
                {transLocale.label}</span>)
            : langSelection
          }
          <p>
            CSV and PO files are supported. <strong>The source language should
            be in {locale}</strong>. For more details on how to prepare glossary
            files, see our <a href={uploadGlossaryUrl} className='u-textInfo'
              target='_blank'>glossary import documentation</a>.
          </p>
        </Modal.Body>
        <Modal.Footer>
          <ButtonGroup className='u-pullRight'>
            <Button bsStyle='link'
              disabled={isUploading}
              onClick={() => handleImportFileDisplay(false)}>
              Cancel
            </Button>
            <Button bsStyle='primary'
              type='button'
              disabled={disableUpload}
              onClick={handleImportFile}>
              <LoaderText loading={isUploading} loadingText='Importing'>
                Import
              </LoaderText>
            </Button>
          </ButtonGroup>
        </Modal.Footer>
      </Modal>)
    /* eslint-enable react/jsx-no-bind */
  }
}

const mapStateToProps = (state) => {
  const {
    stats,
    importFile
  } = state.glossary
  return {
    srcLocale: stats.srcLocale,
    transLocales: stats.transLocales,
    file: importFile.file,
    show: importFile.show,
    status: importFile.status,
    transLocale: importFile.transLocale
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    dispatch,
    handleImportFile: () => dispatch(glossaryImportFile()),
    handleImportFileChange: (event) =>
      dispatch(glossaryUpdateImportFile(event.target.files[0])),
    handleImportFileDisplay: (display) =>
      dispatch(glossaryToggleImportFileDisplay(display)),
    handleImportFileLocaleChange: (localeId) =>
      dispatch(glossaryUpdateImportFileLocale(localeId))
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(ImportModal)
