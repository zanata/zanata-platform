import React from 'react'
import * as PropTypes from 'prop-types'
import { FormattedMessage } from 'react-intl'
import { isEmpty } from 'lodash'
import createValidators from '../../../validators'
import ValidationAction from '../../../validators/ValidationAction'
import Collapse from 'antd/lib/collapse'
import 'antd/lib/collapse/style/css'
import Tooltip from 'antd/lib/tooltip'
import 'antd/lib/tooltip/style/css'
import './index.css'

const Panel = Collapse.Panel

const DO_NOT_RENDER: null = null

const messageList = (messages: Message[]) => {
  return messages.map((m, index) => {
    // If description exists, display in Tooltip
    const messageLabel = m.description
      ? <Tooltip placement='topLeft' title={m.description}>
        {m.label}
      </Tooltip>
      : m.label
    return (
      <div key={index}>
        {messageLabel}: {m.defaultMessage}
      </div>
    )
  })
}

export const getValidationMessages = (
  { locale, source, target, validationOptions }: ValidationProps): ValidationMessages => {
  let validationMessages: ValidationMessages = {} as ValidationMessages
  if (!source || !target || !validationOptions) {
    return validationMessages
  }
  const warningValidators = validationOptions.filter((v) => v.active && !v.disabled)
  const errorValidators = validationOptions.filter((v) => v.disabled)

  const validators = createValidators(locale)

  const warningProducers = warningValidators.map((warningOpt) => {
    return validators.find((validator) => {
      return validator.id === warningOpt.id
    })
  }) as ValidationAction[]

  const errorProducers = errorValidators.map((errorOpt) => {
    return validators.find((validator) => {
      return validator.id === errorOpt.id
    })
  }) as ValidationAction[]

  let warningMessages: Message[] = []
  warningProducers.forEach(validator => {
    const msgs = validator.doValidate(source, target).map(message => {
      return {
        id: validator.id,
        label: validator.label,
        description: validator.description,
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
        label: validator.label,
        description: '',
        defaultMessage: message
      }
    }))
    errorMessages = errorMessages.concat(msgs)
  })

  validationMessages = {
    warningMessages,
    errorMessages,
    warningCount: warningMessages.length,
    errorCount: errorMessages.length
  }
  return validationMessages
}

/**
 * Validation Messages presentational component
 */
const Validation: React.SFC<ValidationMessages> = (validationMessages) => {
  const {
    warningMessages, errorMessages, warningCount, errorCount
  } = validationMessages
  // Check input, do not render if no warnings or errors found
  if (isEmpty(validationMessages) || (warningCount <= 0 && errorCount <= 0)) {
    return DO_NOT_RENDER
  }
  const warningMessageList = warningMessages && messageList(warningMessages)
  const errorMessageList = errorMessages && messageList(errorMessages)
  const warningsMessage = warningCount > 0
    ? <FormattedMessage
      tagName='span'
      id='Validator.header.warnings'
      description='Indicator of the number of validation warnings.'
      defaultMessage='Warnings: {warningCount}'
      values={{ warningCount }
      }
    />
    : DO_NOT_RENDER
  const errorsMessage = errorCount > 0
    ? <FormattedMessage
      tagName='span'
      id='Validator.header.errors'
      description='Indicator of the number of validation errors.'
      defaultMessage='Errors: {errorCount}'
      values={{ errorCount }
      }
    />
    : DO_NOT_RENDER
  const header = (
    <span>
      {warningsMessage} {errorsMessage}
    </span>
  )

  return (
    <div className='TextflowValidation'>
      <Collapse>
        <Panel
          key='1'
          header={header} >
          {errorMessageList}{warningMessageList}
        </Panel>
      </Collapse>
    </div>
  )
}

export interface ValidationProps {
  locale: string,
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
  id: string,
  label: string,
  active: boolean,
  disabled: boolean
}

interface ValidationMessages {
  errorMessages: Message[],
  errorCount: number
  warningMessages: Message[],
  warningCount: number,
}

Validation.propTypes = {
  errorMessages: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string,
      label: PropTypes.string,
      active: PropTypes.bool,
      disabled: PropTypes.bool
    })
  ),
  errorCount: PropTypes.number,
  warningMessages: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string,
      label: PropTypes.string,
      active: PropTypes.bool,
      disabled: PropTypes.bool
    })
  ),
  warningCount: PropTypes.number
}

export default Validation
