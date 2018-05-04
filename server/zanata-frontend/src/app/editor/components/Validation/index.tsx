import React from 'react'
import * as PropTypes from 'prop-types'
import Collapse from 'antd/lib/collapse'
import 'antd/lib/collapse/style/css'
import Tooltip from 'antd/lib/tooltip'
import 'antd/lib/tooltip/style/css'
import './index.css'

const Panel = Collapse.Panel

/**
 * Validation Messages
 */
const Validation: React.SFC<ValidationProps> = ({messages, validationOptions}) => {

  const Messages = messages.map((m, index) => {
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
          {Messages}
        </Panel>
      </Collapse>
    </div>
  )
}

interface ValidationProps {
  messages: Message[],
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
  messages: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string.isRequired,
      label: PropTypes.string.isRequired,
      defaultMessage: PropTypes.string.isRequired,
      description: PropTypes.string,
      disabled: PropTypes.bool.isRequired
    })
  ),
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
