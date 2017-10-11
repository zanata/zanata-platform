import React from 'react'
import { storiesOf } from '@storybook/react'
import DateAndTimeDisplay from '.'

storiesOf('DateAndTimeDisplay', module)
  .add('default', () => (
    <DateAndTimeDisplay dateTime={new Date(2016, 11, 16, 2, 18)} />
  ))
  .add('styling examples', () => (
    <ul>
      <li>
        <DateAndTimeDisplay dateTime={new Date(1985, 9, 26, 1, 21)} />
      </li>
      <li>
        <DateAndTimeDisplay dateTime={new Date(1985, 9, 26, 1, 22)}
          className="u-textMicro" />
      </li>
      <li>
        <DateAndTimeDisplay dateTime={new Date(1985, 9, 26, 1, 20)}
          className="u-textMuted u-textMini" />
      </li>
      <li className="u-sP-1-4">
        <DateAndTimeDisplay dateTime={new Date(2015, 9, 21)}
          className="u-bgHigher u-sP-1-4" />
      </li>
      <li className="u-sP-1-2">
        <DateAndTimeDisplay dateTime={new Date(1955, 10, 12)}
          className="u-bgHigher u-sP-1-2 u-textMuted" />
      </li>
    </ul>
  ))
