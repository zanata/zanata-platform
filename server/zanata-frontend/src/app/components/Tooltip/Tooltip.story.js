import React from 'react'
import { storiesOf } from '@storybook/react'
import { Well } from 'react-bootstrap'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import Tooltip from 'antd/lib/tooltip'
import 'antd/lib/tooltip/style/css'
import Layout from 'antd/lib/layout'
import 'antd/lib/layout/style/css'

const tooltip = <span id='tooltip'><strong>Tooltip ahoy!</strong></span>;

storiesOf('Tooltip', module)
    .add('default', () => (
        <span>
          <Layout>
          <h2><img src="https://i.imgur.com/v4qLk4p.png" width="42px" />Tooltip</h2>
          <Well bsSize="large">Tooltip component for a more stylish alternative to that anchor tag <code>title</code> attribute. Attach and position tooltips with <code>OverlayTrigger</code>.
            <hr />
            <ul><li><a href="https://react-bootstrap.github.io/components.html#tooltips-props">Props for react-bootstrap Tooltips</a></li></ul>
          </Well>
          <Tooltip placement='top' overlay={tooltip}>
            <Button className='btn-default'>Holy guacamole!</Button>
          </Tooltip>
          </Layout>
        </span>
    ))
