import { hashHistory } from 'react-router'

const isChromium = window.chrome
const vendorName = window.navigator.vendor
const isOpera = window.navigator.userAgent.indexOf('OPR') > -1
const isIEedge = window.navigator.userAgent.indexOf('Edge') > -1
const isChrome = (isChromium !== null && isChromium !== undefined &&
  vendorName === 'Google Inc.' && isOpera === false && isIEedge === false)

export const canGoBack = ((isChrome && window.history.length > 2) ||
  (!isChrome && window.history.length > 1))

export const replaceRouteQuery = (location, paramsToReplace) => {
  const newLocation = {
    ...location,
    query: {
      ...location.query,
      ...paramsToReplace
    }
  }
  Object.keys(newLocation.query).forEach(key => {
    if (!newLocation.query[key]) {
      delete newLocation.query[key]
    }
  })
  hashHistory.replace({
    ...newLocation
  })
}
