/* global jest expect */
import * as renderer from 'react-test-renderer'

/*
 * Test function that renders stories into snapshots.
 *
 * This custom test function strips the decorating icon and other components
 * so that the tests are cleaner and not dependent on the wrapping decorators.
 */
// @ts-ignore any
export function snapshotWithoutDecorators ({ story, context }) {
  const storyElement = story.render(context)
  // @ts-ignore
  const tree = renderer.create(storyElement, {}).toJSON()

  // // strip off the padding div and the <Icons />
  // // tree structure is: <div><Icons />{story()}</div>
  // const storyJSON = tree.children[1]

  expect(tree).toMatchSnapshot()
}

/*
 * Mock any addons that would add cruft to the snapshots. This keeps the
 * snapshots cleaner.
 */
export function mockAddons () {
  jest.mock('storybook-host', () => ({
    // ignore host options, just render the inner story
    // @ts-ignore any
    host: (_options) => (story) => story()
  }))
  jest.mock('@storybook/addon-info', () => ({
    // without info, muahahahaha! 😈
    // @ts-ignore any
    withInfo: (_info) => (story) => story
  }))
}

/* Matches any story name that does not include "(no test)" */
export const notNoTestRegex = /^((?!.*?\(no test\)).)*$/
