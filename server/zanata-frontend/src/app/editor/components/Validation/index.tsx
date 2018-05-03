import React from 'react'
import * as PropTypes from 'prop-types'
import Collapse from 'antd/lib/collapse'
import 'antd/lib/collapse/style/css'
const Panel = Collapse.Panel
import './index.css'

/**
 * Validation Messages
 */
const Validation: React.SFC<ValidationProps> = ({messages, validationOptions}) => {

  const Messages = messages.map((m) => {
    return (
      <div>{m.label}: {m.defaultMessage}</div>
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
      <p>Validation Messages Collapse</p>
      <Collapse defaultActiveKey={['1']}>
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
  messages: message[],
  validationOptions: validationOption[]
}

type message = {
  id: string,
  label: string,
  defaultMessage: string
}

type validationOption = {
  id: string,
  label: string,
  active: boolean,
  disabled: boolean
}

Validation.propTypes = {
  messages: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string,
      label: PropTypes.string,
      defaultMessage: PropTypes.string,
      disabled: PropTypes.bool
    })
  ),
  validationOptions: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string,
      label: PropTypes.string,
      active: PropTypes.bool,
      disabled: PropTypes.bool
    })
  )
}

export default Validation
