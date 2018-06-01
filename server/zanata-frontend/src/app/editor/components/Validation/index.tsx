import React from 'react'
import * as PropTypes from 'prop-types'
import { FormattedMessage, injectIntl, intlShape } from 'react-intl'
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

const validationLabels = {
  'HTML/XML tags': 'HTML_XML',
  'Leading/trailing newline (\\n)': 'NEW_LINE',
  'Tab characters (\\t)': 'TAB',
  'Java variables': 'JAVA_VARIABLES',
  'XML entity reference': 'XML_ENTITY',
  'Printf variables': 'PRINTF_VARIABLES',
  'Positional printf (XSI extension)': 'PRINTF_XSI_EXTENSION'
}

// react-intl message titles for validators
const validationIntlMessages = {
  HTML_XML: <FormattedMessage
    id='Validator.HTML_XML.title'
    defaultMessage='HTML/XML tags' />,
  NEW_LINE: <FormattedMessage
    id='Validator.NEW_LINE.title'
    defaultMessage='Leading/trailing newline (\\n)' />,
  TAB: <FormattedMessage
    id='Validator.TAB.title'
    defaultMessage='Tab characters (\\t)' />,
  JAVA_VARIABLES: <FormattedMessage
    id='Validator.JAVA_VARIABLES.title'
    defaultMessage='Java variables' />,
  XML_ENTITY: <FormattedMessage
    id='Validator.XML_ENTITY.title'
    defaultMessage='XML entity reference' />,
  PRINTF_VARIABLES: <FormattedMessage
    id='Validator.PRINTF_VARIABLES.title'
    defaultMessage='Printf variables' />,
  PRINTF_XSI_EXTENSION: <FormattedMessage
    id='Validator.PRINTF_XSI_EXTENSION.title'
    defaultMessage='Positional printf (XSI extension)' />,
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
        {validationIntlMessages[m.id]}: {messageBody}
      </div>
    )
  })
}

/**
 * Validation Messages presentational component
 */
const Validation: React.SFC<ValidationProps> = ({ intl, source, target, validationOptions }) => {
  const warningValidators = validationOptions.filter((v) => v.active && !v.disabled)
  const errorValidators = validationOptions.filter((v) => v.disabled)

  const validators = validatorFactory(validationOptions, intl.locale)

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
        id: validationLabels[validator.id],
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
        id: validationLabels[validator.id],
        label: validator.id,
        description: '',
        defaultMessage: message
      }
    }))
    errorMessages = errorMessages.concat(msgs)
  })

  const WarningMessageList = messageList(warningMessages)
  const ErrorMessageList = messageList(errorMessages)
  const warningCount = warningMessages.length
  const errorCount = errorMessages.length

  const header = (
    <FormattedMessage
      tagName = 'option'
      id = 'Validator.header'
      description = 'Indicator of the number of validation warnings and errors.'
      defaultMessage= 'Warnings: {warningCount}, Errors: {errorCount}'
      values={{ warningCount, errorCount }
      }
    />
  )
  // Only render if warnings or errors found
  return (warningMessages.length > 0 || errorMessages.length > 0)
    ? <div className='TextflowValidation'>
      <Collapse>
        <Panel
          key='1'
          header={header} >
          {ErrorMessageList}
          {WarningMessageList}
        </Panel>
      </Collapse>
    </div>
    : <span></span>
}

interface ValidationProps {
  intl: any,
  source: string,
  target: string,
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
  intl: intlShape.isRequired,
  source: PropTypes.string.isRequired,
  target: PropTypes.string.isRequired,
  validationOptions: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string.isRequired,
      label: PropTypes.string.isRequired,
      active: PropTypes.bool.isRequired,
      disabled: PropTypes.bool.isRequired
    })
  )
}

export default injectIntl(Validation)
