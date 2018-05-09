// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import { connect } from 'react-redux'
import { LoaderText } from '../../components'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/index.less'
import Modal from 'antd/lib/modal'
import 'antd/lib/modal/style/index.less'
import Select from 'antd/lib/select'
import 'antd/lib/select/style/index.less'

import {
  glossaryUpdateExportType,
  glossaryDownload,
  glossaryToggleExportFileDisplay,
  FILE_TYPES
} from '../../actions/glossary-actions'

class ExportModal extends Component {
  static propTypes = {
    show: PropTypes.bool,
    type: PropTypes.shape({
      label: PropTypes.string,
      value: PropTypes.string
    }),
    status: PropTypes.number,
    types: PropTypes.arrayOf(PropTypes.shape({
      label: PropTypes.string,
      value: PropTypes.string
    })),
    handleExportType: PropTypes.func,
    handleExportFileDisplay: PropTypes.func,
    handleExport: PropTypes.func
  }

  render () {
    const {
      show,
      type,
      status,
      types,
      handleExportType,
      handleExportFileDisplay,
      handleExport
    } = this.props

    const isExporting = status !== -1
    let message
    /* eslint-disable max-len */
    if (type.value === FILE_TYPES[0]) {
      message = <span>This will download glossary entries in <strong>all languages</strong> into <strong>csv</strong> format.</span>
    } else if (type.value === FILE_TYPES[1]) {
      message = <span>This will download a zip file of glossary entries in <strong>all languages</strong> in <strong>po</strong> format.</span>
    }
    /* eslint-enable max-len */
    const exportGlossaryUrl =
      'http://docs.zanata.org/en/release/user-guide/glossary/export-glossaries/'

    const options = types.map((t, index) => {
      return (
        <Select.Option key={index}>
          {t.value}
        </Select.Option>
      )
    })

    const exportType = (typeIndex) => {
      handleExportType(types[typeIndex])
    }

    /* eslint-disable react/jsx-no-bind */
    return (
      <Modal
        title={'Export Glossary'}
        visible={show}
        onHide={() => handleExportFileDisplay(false)}
        footer={[
          <Button
            key='back'
            aria-label='button'
            disabled={isExporting}
            onClick={() => handleExportFileDisplay(false)}>
            Cancel
          </Button>,
          <Button
            key='ok'
            aria-label='button'
            type='primary'
            disabled={isExporting}
            onClick={handleExport}>
            <LoaderText loading={isExporting} loadingText='Exporting'>
              Export
            </LoaderText>
          </Button>
        ]} >
        <Select
          disabled={isExporting}
          placeholder='Select a file type…'
          style={{ width: '95%' }}
          onChange={exportType}>
          {options}
        </Select>
        <p>
          {message}
          <br />
          For more details on how to export glossary
          files, see our <a href={exportGlossaryUrl} className='u-textInfo'
            target='_blank'>glossary export documentation</a>.
        </p>
      </Modal>)
    /* eslint-enable react/jsx-no-bind */
  }
}

const mapStateToProps = (state) => {
  const {
    exportFile
    } = state.glossary
  return {
    show: exportFile.show,
    type: exportFile.type,
    status: exportFile.status,
    types: exportFile.types
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    dispatch,
    handleExportFileDisplay: (display) =>
      dispatch(glossaryToggleExportFileDisplay(display)),
    handleExportType: (type) =>
      dispatch(glossaryUpdateExportType(type)),
    handleExport: () => dispatch(glossaryDownload())
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(ExportModal)
