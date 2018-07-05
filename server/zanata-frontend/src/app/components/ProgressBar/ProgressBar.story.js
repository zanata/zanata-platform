import React from 'react'
import { storiesOf } from '@storybook/react'
import Progress from 'antd/lib/progress'
import 'antd/lib/progress/style/css'

const now = 60

storiesOf('ProgressBar', module)
    .add('default', () => (
        <span>
          <Progress percent={now} showInfo={} />
        </span>
       ))
