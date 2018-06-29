import React from 'react'
import * as PropTypes from 'prop-types'
import { FormattedMessage, injectIntl, intlShape } from 'react-intl'
import Collapse from 'antd/lib/collapse'
import 'antd/lib/collapse/style/css'
import Tooltip from 'antd/lib/tooltip'
import 'antd/lib/tooltip/style/css'
import './index.css'
import createValidators from '../../../validators'
import ValidationAction from '../../../validators/ValidationAction'

const Panel = Collapse.Panel

const DO_NOT_RENDER: null = null

// @ts-ignore any
const messageList = (messages) => {
  // @ts-ignore any
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

/**
 * Validation Messages presentational component
 */
const Validation: React.SFC<ValidationProps> = ({ intl, source, target, validationOptions }) => {
  const warningValidators = validationOptions.filter((v) => v.active && !v.disabled)
  const errorValidators = validationOptions.filter((v) => v.disabled)

  const validators = createValidators(intl.locale)

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

  const WarningMessageList = messageList(warningMessages)
  const ErrorMessageList = messageList(errorMessages)
  const warningCount = warningMessages.length
  const errorCount = errorMessages.length
  const warningsMessage = warningCount > 0
    ? <FormattedMessage
      tagName='option'
      id='Validator.header.warnings'
      description='Indicator of the number of validation warnings.'
      defaultMessage='Warnings: {warningCount}'
      values={{ warningCount }
      }
    />
    : DO_NOT_RENDER
  const errorsMessage = errorCount > 0
    ? <FormattedMessage
      tagName='option'
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
    : DO_NOT_RENDER
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
  id: string,
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
