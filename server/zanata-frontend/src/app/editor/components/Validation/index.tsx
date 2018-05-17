import React from 'react'
import * as PropTypes from 'prop-types'
import Collapse from 'antd/lib/collapse'
import 'antd/lib/collapse/style/css'
import Tooltip from 'antd/lib/tooltip'
import 'antd/lib/tooltip/style/css'
import './index.css'
import * as Validators from '../../../validators'

const Panel = Collapse.Panel

/**
 * Validation Messages presentational component
 */
const Validation: React.SFC<ValidationProps> = ({ source, target, localeId, validationOptions }) => {
  const {
    Messages,
    ValidationId,
    // HtmlXmlTagValidation,
    // JavaVariablesValidation,
    // NewlineLeadTrailValidation,
    // PrintfVariablesValidation,
    // PrintfXSIExtensionValidation,
    TabValidation,
    // XmlEntityValidation
  } = Validators
  const locale = localeId ? localeId : 'en-US'
  const TabValidator =
    new TabValidation(ValidationId.TAB, '', Messages[locale], locale)
  const messages: Message[] = TabValidator.doValidate(source, target).map(message => {
    return {
      id: ValidationId.TAB,
      label: OptionLabels[ValidationId.TAB],
      description: '',
      defaultMessage: message
    }
  })

  const MessageList = messages.map((m, index) => {
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

  const warningValidators = validationOptions.filter((v) => v.active && !v.disabled)
  const errorValidators = validationOptions.filter((v) => v.disabled)
  function getWarnings(total, m) {
    return warningValidators.find((v) => v.id === m.id)
      ? ++total
      : total
  }
  function getErrors(total, m) {
    return errorValidators.find((v) => v.id === m.id)
      ? ++total
      : total
  }
  const errorCount = messages.reduce(getErrors, 0)
  const warningCount = messages.reduce(getWarnings, 0)
  return (
    <div className='TextflowValidation'>
      <Collapse>
        <Panel
          key='1'
          header={`Warnings: ${warningCount}, Errors: ${errorCount}`} >
          {MessageList}
        </Panel>
      </Collapse>
    </div>
  )
}

enum OptionLabels {
  'html-xml-tags' = 'HTML/XML tags',
  'java-variables' = 'Java variables',
  'leading-trailing-newline' = 'Leading/trailing newline',
  'positional-printf' = 'Positional printf (XSI extension)',
  'printf-variables' = 'Printf variables',
  'tab-characters' = 'Tab characters',
  'xml-entity-reference' = 'XML entity reference',
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
  id: string,
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
