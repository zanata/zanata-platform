import { configure } from '@kadira/storybook'
import './storybook.css'

// fonts are included in index.html for the app, but storybook does not use that
var fontLink = document.createElement('link')
fontLink.setAttribute('href', 'http://fonts.googleapis.com/css?family=Source+Sans+Pro:300,400,600,700,400italic') // eslint-disable-line max-len
fontLink.setAttribute('rel', 'stylesheet')
fontLink.setAttribute('type', 'text/css')
fontLink.setAttribute('async', '')
document.getElementsByTagName('head')[0].appendChild(fontLink)

// icon spritesheet is included inline in the app index.html
// this simulates including icons inline, without needing access to the index
// (don't care for storybook if it takes a moment for icons to load)
var iconXHR = new XMLHttpRequest()
iconXHR.open('GET', 'icons.svg', true)
iconXHR.send()
iconXHR.onload = function (e) {
  var div = document.createElement('div')
  div.setAttribute('style', 'width:0;height:0;overflow:hidden')
  div.innerHTML = iconXHR.responseText
  document.body.insertBefore(div, document.body.childNodes[0])
}

function loadStories () {
  require('../app/components/stories.js')
}

configure(loadStories, module)
