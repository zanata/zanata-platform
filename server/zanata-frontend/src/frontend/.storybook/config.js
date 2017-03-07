import { configure } from '@kadira/storybook'
import './storybook.css'

// fonts are included in index.html for the app, but storybook does not use that
var fontLink = document.createElement('link')
fontLink.setAttribute('href', 'http://fonts.googleapis.com/css?family=Source+Sans+Pro:300,400,600,700,400italic') // eslint-disable-line max-len
fontLink.setAttribute('rel', 'stylesheet')
fontLink.setAttribute('type', 'text/css')
fontLink.setAttribute('async', '')
document.getElementsByTagName('head')[0].appendChild(fontLink)

function loadStories () {
  require('../app/components/stories.js')
}

configure(loadStories, module)
