import _ from 'lodash'
import {getId} from './TransStatusService'

/* convert from structure used in angular to structure used in react */
// TODO we should change the server response to save us from doing this
//      transformation
export const prepareLocales = (locales) => {
  return _.chain(locales || [])
      .map(function (locale) {
        return {
          id: locale.localeId,
          name: locale.displayName
        }
      })
      .indexBy('id')
      .value()
}

export const prepareStats = (statistics) => {
  _.forEach(statistics, statistic => {
    statistic[getId('needswork')] = statistic['needReview'] || 0
  })
  // TODO pahuang first is word stats and second is message stats
  const msgStatsStr = _.pick(statistics[1], [
    'total', 'untranslated', 'rejected',
    'needswork', 'translated', 'approved'])
  return _.mapValues(msgStatsStr, (numStr) => {
    return parseInt(numStr, 10)
  })
}

export const prepareDocs = documents => {
  return _.pluck(documents || [], 'name')
}
