import React from 'react'
import { storiesOf } from '@storybook/react'
import LanguageSelector from '.'
import { I18nextProvider } from 'react-i18next'
import i18n from './i18n'

storiesOf('LanguageSelector', module)
  .addDecorator(story => (
    <I18nextProvider i18n={i18n}>
       {story()}
    </I18nextProvider>
   ))
  .add('app view', () => (
    <LanguageSelector />
  ))
