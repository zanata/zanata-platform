import React from 'react'
import PropTypes from 'prop-types'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import Divider from 'antd/lib/divider'
import 'antd/lib/divider/style/css'
import Modal from 'antd/lib/modal'
import 'antd/lib/modal/style/css'
import { ValidationMessages } from '../Validation'
import { Phrase, Status } from '../../utils/phrase'
import SyntaxHighlighter, { registerLanguage } from 'react-syntax-highlighter/light'
import xml from 'react-syntax-highlighter/languages/hljs/xml'
import { atelierLakesideLight } from 'react-syntax-highlighter/styles/hljs'
registerLanguage('xml', xml)

/**
 * Modal to confirm saving a textflow with Validation errors
 */
const ValidaitonErrorsModal: React.SFC<ValidaitonErrorsModalProps> = (props) => {
  const {
    phrase,
    validationMessages,
    showErrorModal,
    savePhraseWithStatus,
    selectedButtonStatus
  } = props

  const syntaxStyle = {
    width: '90%',
    whiteSpace: 'pre-wrap',
    wordWrap: 'break-word'
  }
  const cancelSave = () => {
    showErrorModal(phrase.id, false)
  }
  const saveWithErrors = () => {
    showErrorModal(phrase.id, false)
    savePhraseWithStatus(phrase, selectedButtonStatus)
  }
  // @ts-ignore any
  const errorMessages = validationMessages.errorMessages.map((msg, i) => {
      return (
        <li className='red' key={i}>
          {msg.label}: {msg.defaultMessage}
        </li>
      )
    })
  return (
    <Modal
      title='You are trying to save an invalid translation'
      visible={phrase.showValidationErrorModal}
      onCancel={cancelSave}
      footer={[
        <Button
          key='back'
          aria-label='button'
          onClick={cancelSave}>
          Cancel
        </Button>,
        <Button
          key='ok'
          aria-label='button'
          type='primary'
          className='EditorButton u-sizeHeight-1_1-4 u-textCapitalize Button--unsure'
          onClick={saveWithErrors}>
          Save as Needs Work
        </Button>
      ]}>
      <h2>Translation</h2>
      <SyntaxHighlighter
        language='html'
        style={atelierLakesideLight}
        wrapLines
        lineStyle={syntaxStyle}>
        {phrase.newTranslations}
      </SyntaxHighlighter>
      <Divider />
      <div className='red'>
        <h2>Error Messages</h2>
        <ul>
          {errorMessages}
        </ul>
      </div>
    </Modal>
  )
}

ValidaitonErrorsModal.propTypes = {
  phrase: PropTypes.any.isRequired,
  showErrorModal: PropTypes.func.isRequired,
  validationMessages: PropTypes.any.isRequired
}

interface ValidaitonErrorsModalProps {
  phrase: Phrase
  savePhraseWithStatus: (phrase: Phrase, status: string) => void
  selectedButtonStatus: Status
  showErrorModal: (id: string | undefined, show: boolean) => void
  validationMessages: ValidationMessages
}

export default ValidaitonErrorsModal
