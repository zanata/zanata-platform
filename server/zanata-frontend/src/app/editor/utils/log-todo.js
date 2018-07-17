/* Simple function to log TODO prominently in the console */

const BASE_STYLE = 'font-size: 1.5em; background-color: #fff9ce;' +
  'padding: 0.1em; text-shadow: -1px -1px rgba(0,0,0,0.2)'
const EMO_STYLE = `${BASE_STYLE}; color: #ffe100`
const LABEL_STYLE = `${BASE_STYLE}; color: #3c42e0; font-weight: bold`
const MESSAGE_STYLE = `${BASE_STYLE}; color: #3c93e0`

// @ts-ignore any
export default function TODO (message, ...data) {
  // eslint-disable-next-line no-console
  console.log('%c🚧🚧🚧%c TODO %c%s %c🚧🚧🚧',
    EMO_STYLE, LABEL_STYLE, MESSAGE_STYLE, message, EMO_STYLE,
    ...data)
}
