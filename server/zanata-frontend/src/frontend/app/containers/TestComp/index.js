import React, { Component } from 'react'
import { Button, ButtonToolbar, Tooltip,
  OverlayTrigger } from 'react-bootstrap'
import { Icon } from '../../components'

class TestComp extends Component {

  render () {
    const tooltip = (
      <Tooltip id='tooltip-bottom' role='tooltip'>
      This is some tooltip text</Tooltip>
    )
    return (
      <div>
        <Icon name='search' size='s2' /> Icon test
        <ButtonToolbar>
          <OverlayTrigger placement='bottom' overlay={tooltip} >
            <Button bsStyle='default'>Test tooltip</Button>
          </OverlayTrigger>
        </ButtonToolbar>
      </div>
    )
  }
}

export default TestComp
