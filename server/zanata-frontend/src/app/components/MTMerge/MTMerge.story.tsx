import React from 'react'
import { storiesOf } from '@storybook/react'
import { action } from '@storybook/addon-actions'
import { MTMergeOptions } from './MTMergeOptions'
import { Locale, LocaleId } from '../../utils/prop-types-util'
import { MTMergeModal } from './MTMergeModal'
import { STATUS_NEEDS_WORK } from '../../editor/utils/phrase'

class LocaleImpl implements Locale {
  readonly localeId: LocaleId
  readonly displayName: string
  readonly nativeName: string
  constructor (localeId: LocaleId, displayName: string) {
    this.localeId = localeId
    this.displayName = displayName
    this.nativeName = displayName
  }
}

function testLocale(code: LocaleId, displayName: string) {
  return new LocaleImpl(code, displayName)
}

const testLocales: Locale[] = [
  testLocale('ar-SA', 'Arabic (Saudi Arabia)'),
  testLocale('da-DK', 'Danish (Denmark)'),
  testLocale('de-DE', 'German (Germany)'),
  testLocale('el-GR', 'Modern Greek (Greece)'),
  testLocale('en-AU', 'English (Australia)'),
  testLocale('en-GB', 'English (United Kingdom)'),
  testLocale('en-IE', 'English (Ireland)'),
  testLocale('en-US', 'English (United States)'),
  testLocale('en-ZA', 'English (South Africa)'),
  testLocale('es-ES', 'Spanish (Spain)'),
  testLocale('es-MX', 'Spanish (Mexico)'),
  testLocale('fi-FI', 'Finnish (Finland)'),
  testLocale('fr-CA', 'French (Canada)'),
  testLocale('fr-FR', 'French (France)'),
  testLocale('he-IL', 'Hebrew (Israel)'),
  testLocale('hi-IN', 'Hindi (India)'),
  testLocale('hu-HU', 'Hungarian (Hungary)'),
  testLocale('id-ID', 'Indonesian (Indonesia)'),
  testLocale('it-IT', 'Italian (Italy)'),
  testLocale('ja-JP', 'Japanese (Japan)'),
  testLocale('ko-KR', 'Korean (Republic of Korea)'),
  testLocale('nl-BE', 'Dutch (Belgium)'),
  testLocale('nl-NL', 'Dutch (Netherlands)'),
  testLocale('no-NO', 'Norwegian (Norway)'),
  testLocale('pl-PL', 'Polish (Poland)'),
  testLocale('pt-BR', 'Portuguese (Brazil)'),
  testLocale('pt-PT', 'Portuguese (Portugal)'),
  testLocale('ro-RO', 'Romanian (Romania)'),
  testLocale('ru-RU', 'Russian (Russian Federation)'),
  testLocale('sk-SK', 'Slovak (Slovakia)'),
  testLocale('sv-SE', 'Swedish (Sweden)'),
  testLocale('th-TH', 'Thai (Thailand)'),
  testLocale('tr-TR', 'Turkish (Turkey)'),
  testLocale('zh-CN', 'Chinese (China)'),
  testLocale('zh-HK', 'Chinese (Hong Kong)'),
  testLocale('zh-TW', 'Chinese (Taiwan)'),
  testLocale('cs-CZ', 'Czech (Czech Republic)'),
]

storiesOf('MTMerge', module)
  // .add('multiple', () => (
  //   <MTMergeOptions
  //     allowMultiple={true}
  //     availableLocales={testLocales}
  //     checkedLocales={[]}
  //     saveAs={STATUS_NEEDS_WORK}
  //     overwriteFuzzy={false}
  //     projectSlug='myProject'
  //     versionSlug='myVersion'
  //     onLocaleChange={action('onLocaleChange')}
  //     onOverwriteFuzzyChange={action('onOverwriteFuzzyChange')}
  //     onSaveAsChange={action('onSaveAsChange')}
  //   />
  // ))
  .add('single', () => (
    <MTMergeOptions
      allowMultiple={false}
      availableLocales={testLocales}
      checkedLocales={[]}
      saveAs={STATUS_NEEDS_WORK}
      overwriteFuzzy={false}
      projectSlug='myProject'
      versionSlug='myVersion'
      onLocaleChange={action('onLocaleChange')}
      onOverwriteFuzzyChange={action('onOverwriteFuzzyChange')}
      onSaveAsChange={action('onSaveAsChange')}
    />
  ))

storiesOf('MTMergeModal', module)
  .add('single', () => (
    <MTMergeModal
      processStatus={undefined}
      allowMultiple={false}
      showMTMerge={true}
      availableLocales={testLocales}
      projectSlug='myProject'
      versionSlug='myVersion'
      onCancel={action('onCancel')}
      onCancelMTMerge={action('onCancelMTMerge')}
      onSubmit={action('onSubmit')}
      mergeProcessFinished={action('mergeProcessFinished')}
      queryMTMergeProgress={action('queryMTMergeProgress')}
    />
  ))
