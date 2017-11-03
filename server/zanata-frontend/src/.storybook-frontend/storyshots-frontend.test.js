import initStoryshots from '@storybook/addon-storyshots'
import {
  mockAddons,
  notNoTestRegex,
  snapshotWithoutDecorators
} from '../.storybook-editor/storyshots-util'

initStoryshots({
  suite: 'Frontend Storyshots',
  configPath: '.storybook-frontend',
  framework: 'react',

  /* add components here that should not have their stories tested
   * (e.g. when they are under development and not used in the app yet)
   *
   * Regex structure:
   *  ^     start of component name
   *  (?!   negative lookahead, don't match anything in this group
   *  (EditorSearchInput|SettingOption|SettingsOptions)$
   *        component names to not match, add yours in here to not test it
   *        the $ ensures that excluding Foo does not block testing FooBar.
   *  ).*$  match any other characters to the end of the string
   */
  storyKindRegex: /^(?!(NOTHING|NOTHINGELSE)$).*$/,
  storyNameRegex: notNoTestRegex,
  test: snapshotWithoutDecorators
})
