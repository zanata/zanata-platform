import React from 'react'
import { storiesOf, action } from '@storybook/react'
import RejectTranslationAdmin from '.'

/*
 * TODO add stories showing the range of states
 *      for RejectTranslationAdmin
 */
storiesOf('RejectTranslationAdmin', module)
  .add('editing', () => (
    <RejectTranslationAdmin />
  ))
