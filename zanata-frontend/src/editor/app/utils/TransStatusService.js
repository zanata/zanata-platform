// TODO migrated from app/components/transStatus/TransStatusService.js
import _ from 'lodash'

const STATUSES = {
  'UNTRANSLATED': {
    'ID': 'untranslated',
    'NAME': 'Untranslated',
    'CSSCLASS': 'neutral'
  },
  'NEEDSWORK': {
    'ID': 'needswork',
    'NAME': 'Needs Work',
    'CSSCLASS': 'unsure'
  },
  'TRANSLATED': {
    'ID': 'translated',
    'NAME': 'Translated',
    'CSSCLASS': 'success'
  },
  'APPROVED': {
    'ID': 'approved',
    'NAME': 'Approved',
    'CSSCLASS': 'highlight'
  }
}

export function getAll () {
  return STATUSES
}

export function getAllAsArray () {
  return _.values(STATUSES)
}

/**
 *
 * @param {string} statusKey string representation of the status.
 * @returns {StatusInfo}
 */
exports.getStatusInfo = function (statusKey) {
  return STATUSES[conformStatus(statusKey)]
}

exports.getId = function (statusKey) {
  return STATUSES[conformStatus(statusKey)].ID
}

exports.getServerId = function (statusId) {
  return serverStatusId(statusId)
}

exports.getName = function (statusKey) {
  return STATUSES[conformStatus(statusKey)].NAME
}

exports.getCSSClass = function (statusKey) {
  return STATUSES[conformStatus(statusKey)].CSSCLASS
}

/**
 * Conform it to uppercase for lookups and
 * temporary fix for server sending "needReview"
 * instead of needswork status
 * @param  {string} status
 * @return {string}        new value to use
 */
function conformStatus (statusKey) {
  statusKey = statusKey.toUpperCase()
  if (!statusKey || statusKey === 'NEW') {
    statusKey = 'UNTRANSLATED'
  } else if (statusKey === 'NEEDREVIEW') {
    statusKey = 'NEEDSWORK'
  }
  return statusKey
}

/**
 * Conform it to PascalCase for lookups and
 * temporary fix for server receiving "needReview"
 * instead of needswork status
 * @param  {string} status
 * @return {string}        new value to use
 */
function serverStatusId (statusId) {
  statusId = statusId.toLowerCase()
  if (!statusId || statusId === 'untranslated') {
    return 'New'
  } else if (statusId === 'needswork') {
    return 'NeedReview'
  }
  return statusId.charAt(0).toUpperCase() + statusId.slice(1).toLowerCase()
}
