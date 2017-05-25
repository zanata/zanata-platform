/* Responsible for loading config to present to other modules */

// assumption: config will always be attached to the window object if present
const config = window.config || {}

/* The part of the path that is just the server deployment path. e.g. if the
 * server is deployed at example.com/zanata then this will be /zanata */
config.baseUrl = config.baseUrl || ''

export default config
