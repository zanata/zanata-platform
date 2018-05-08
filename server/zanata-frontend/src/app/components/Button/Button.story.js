// @ts-nocheck
import React from 'react'
import { storiesOf } from '@storybook/react'
import {Button} from 'antd'
storiesOf('Button', module)
    .add('default (no test)', () => (
        <span>
          <Button className='btn-default'>Default</Button>
          <br />
          <hr />
          <Button className='btn-primary'>Primary</Button>
          <br />
          <hr />
            <a href="https://ant.design/components/button/">
              Component information</a>
        </span>
    ))
