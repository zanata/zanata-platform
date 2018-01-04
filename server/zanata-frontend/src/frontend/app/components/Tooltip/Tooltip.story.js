import React from 'react'
import { storiesOf } from '@storybook/react'
import { Button, ButtonToolbar, OverlayTrigger,
  Tooltip, Well } from 'react-bootstrap'

const tooltip = (
    <Tooltip id='tooltip'><strong>Tooltip ahoy!</strong> Check this info.
    </Tooltip>
)

storiesOf('Tooltip', module)
    .add('default', () => (
        <span>
                  <h2><img src="https://react-bootstrap.github.io/assets/logo.png" width="42px" />Tooltip</h2>

          <Well>Tooltip component for a more stylish alternative to that anchor tag <code>title</code> attribute. Attach and position tooltips with <code>OverlayTrigger</code>.
            <hr />
            <ul><li><a href="https://react-bootstrap.github.io/components.html#tooltips-props">Props for react-bootstrap Tooltips</a></li></ul>
          </Well>
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
        </span>
    ))
