import React, { PropTypes, Component } from 'react'
import { connect } from 'react-redux'
import { isEmpty, includes } from 'lodash'

import {
  ButtonLink,
  ButtonRound,
  LoaderText,
  Modal,
  Select,
  Row
} from 'zanata-ui'

import {
  glossaryImportFile,
  glossaryUpdateImportFile,
  glossaryToggleImportFileDisplay,
  glossaryUpdateImportFileLocale,
  FILE_TYPES
} from '../../actions/glossary'

class ImportModal extends Component {
  getUploadFileExtension (file) {
    return file ? file.name.split('.').pop() : ''
  }

  isSupportedFile (extension) {
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
          className='Maw(r16) Mb(r1)'
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
      messageSection = (<div className='C(danger) My(rq)'>
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
            className='Mb(r1)' />
          {messageSection}
          {isUploading
            ? transLocale && (<span className='Fz(ms2)'>
                {transLocale.label}</span>)
            : langSelection
          }
          <p>
            CSV and PO files are supported. <strong>The source language should
            be in {locale}</strong>. For more details on how to prepare glossary
            files, see our <a href={uploadGlossaryUrl} className='C(pri)'
              target='_blank'>glossary import documentation</a>.
          </p>
        </Modal.Body>
        <Modal.Footer>
          <Row theme={{ base: {j: 'Jc(c)'} }}>
            <ButtonLink
              atomic={{m: 'Mend(r1)'}}
              disabled={isUploading}
              onClick={() => handleImportFileDisplay(false)}>
              Cancel
            </ButtonLink>
            <ButtonRound
              type='primary'
              disabled={disableUpload}
              onClick={handleImportFile}>
              <LoaderText loading={isUploading} loadingText='Importing'>
                Import
              </LoaderText>
            </ButtonRound>
          </Row>
        </Modal.Footer>
      </Modal>)
    /* eslint-enable react/jsx-no-bind */
  }
}

ImportModal.propTypes = {
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
