import React from 'react'
import * as PropTypes from 'prop-types'
import Collapse from 'antd/lib/collapse'
import 'antd/lib/collapse/style/css'
import Tooltip from 'antd/lib/tooltip'
import 'antd/lib/tooltip/style/css'
import './index.css'
import {
  Messages,
  ValidationId,
  HtmlXmlTagValidation,
  JavaVariablesValidation,
  NewlineLeadTrailValidation,
  PrintfVariablesValidation,
  PrintfXSIExtensionValidation,
  TabValidation,
  XmlEntityValidation
} from '../../../validators'

const Panel = Collapse.Panel

// TODO: only instantiate enabled validators (warning/error)
function validatorFactory(_validationOptions: ValidationOption[], locale: string): any {
  const validators = []
  validators.push(
    new HtmlXmlTagValidation(ValidationId.HTML_XML, '', Messages[locale], locale))
  validators.push(
    new JavaVariablesValidation(ValidationId.JAVA_VARIABLES, '', Messages[locale], locale))
  validators.push(
    new NewlineLeadTrailValidation(ValidationId.NEW_LINE, '', Messages[locale], locale))
  validators.push(
    new PrintfVariablesValidation(ValidationId.PRINTF_VARIABLES, '', Messages[locale], locale))
  validators.push(
    new PrintfXSIExtensionValidation(ValidationId.PRINTF_XSI_EXTENSION, '', Messages[locale]))
  validators.push(
    new TabValidation(ValidationId.TAB, '', Messages[locale], locale))
  validators.push(
    new XmlEntityValidation(ValidationId.XML_ENTITY, '', Messages[locale], locale))
  return validators
}

const messageList = (messages) => {
  return messages.map((m, index) => {
    // If description exists, display in Tooltip
    const messageBody = m.description
      ? <Tooltip placement='topRight' title={m.description}>
        {m.defaultMessage}
      </Tooltip>
      : m.defaultMessage
    return (
      <div key={index}>
        {m.label}: {messageBody}
      </div>
    )
  })
}

/**
 * Validation Messages presentational component
 */
const Validation: React.SFC<ValidationProps> = ({ source, target, localeId, validationOptions }) => {
  const warningValidators = validationOptions.filter((v) => v.active && !v.disabled)
  const errorValidators = validationOptions.filter((v) => v.disabled)
  const locale = localeId ? localeId : 'en-US'

  const validators = validatorFactory(validationOptions, locale)

  const warningProducers = warningValidators.map((warningOpt) => {
    return validators.find((validator) => {
      return validator.id === ValidationId[warningOpt.id]
    })
  })

  const errorProducers = errorValidators.map((warningOpt) => {
    return validators.find((validator) => {
      return validator.id === ValidationId[warningOpt.id]
    })
  })

  let warningMessages: Message[] = []
  warningProducers.forEach(validator => {
    const msgs = validator.doValidate(source, target).map(message => {
      return {
        id: validator.id,
        label: validator.id,
        description: '',
        defaultMessage: message
      }
    })
    warningMessages = warningMessages.concat(msgs)
  })

  let errorMessages: Message[] = []
  errorProducers.forEach(validator => {
    const msgs = (validator.doValidate(source, target).map(message => {
      return {
        id: validator.id,
        label: validator.id,
        description: '',
        defaultMessage: message
      }
    }))
    errorMessages = errorMessages.concat(msgs)
  })

  const WarningMessageList = messageList(warningMessages)
  const ErrorMessageList = messageList(errorMessages)
  return (
    <div className='TextflowValidation'>
      <Collapse>
        <Panel
          key='1'
          header={`Warnings: ${warningMessages.length}, Errors: ${errorMessages.length}`} >
          {ErrorMessageList}
          {WarningMessageList}
        </Panel>
      </Collapse>
    </div>
  )
}

interface ValidationProps {
  source: string,
  target: string,
  localeId?: string,
  validationOptions: ValidationOption[]
}

interface Message {
  id: string,
  label: string,
  defaultMessage: string,
  description?: string
}

interface ValidationOption {
  id: ValidationId,
  label: string,
  active: boolean,
  disabled: boolean
}

Validation.propTypes = {
  source: PropTypes.string.isRequired,
  target: PropTypes.string.isRequired,
  localeId: PropTypes.string,
  validationOptions: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string.isRequired,
      label: PropTypes.string.isRequired,
      active: PropTypes.bool.isRequired,
      disabled: PropTypes.bool.isRequired
    })
  )
}

export default Validation
