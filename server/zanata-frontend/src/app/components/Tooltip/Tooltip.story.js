import React from 'react'
import { storiesOf } from '@storybook/react'
import { Button, ButtonToolbar, OverlayTrigger,
  Tooltip } from 'react-bootstrap'

const tooltip = (
    <Tooltip id='tooltip'><strong>Tooltip ahoy!</strong> Check this info.
    </Tooltip>
)

storiesOf('Tooltip', module)
    .add('default', () => (
        <ButtonToolbar>
          <OverlayTrigger placement='left' overlay={tooltip}>
            <Button bsStyle='default'>Holy guacamole!</Button>
          </OverlayTrigger>
          <OverlayTrigger placement='top' overlay={tooltip}>
            <Button bsStyle='default'>Holy guacamole!</Button>
          </OverlayTrigger>
          <OverlayTrigger placement='bottom' overlay={tooltip}>
            <Button bsStyle='default'>Holy guacamole!</Button>
          </OverlayTrigger>
          <OverlayTrigger placement='right' overlay={tooltip}>
            <Button bsStyle='default'>Holy guacamole!</Button>
          </OverlayTrigger>
        </ButtonToolbar>
    ))
