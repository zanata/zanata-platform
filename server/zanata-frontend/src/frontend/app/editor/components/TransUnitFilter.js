import FilterToggle from './FilterToggle'
import React, { PropTypes } from 'react'
import {
  STATUS_UNTRANSLATED,
  STATUS_NEEDS_WORK,
  STATUS_TRANSLATED,
  STATUS_APPROVED,
  STATUS_REJECTED
} from '../utils/status'

/**
 * Panel with controls to filter the list of trans units
 */
const TransUnitFilter = React.createClass({
  propTypes: {
    actions: PropTypes.shape({
      resetFilter: PropTypes.func.isRequired,
      onFilterChange: PropTypes.func.isRequired
    }).isRequired,

    filter: PropTypes.shape({
      all: PropTypes.bool.isRequired,
      approved: PropTypes.bool.isRequired,
      rejected: PropTypes.bool.isRequired,
      translated: PropTypes.bool.isRequired,
      needswork: PropTypes.bool.isRequired,
      untranslated: PropTypes.bool.isRequired
    }).isRequired,

    counts: PropTypes.shape({
      // TODO better to derive total from the others rather than duplicate
      total: PropTypes.number,
      approved: PropTypes.number,
      rejected: PropTypes.number,
      translated: PropTypes.number,
      needswork: PropTypes.number,
      untranslated: PropTypes.number
    }).isRequired,

    // DO NOT RENAME, the translation string extractor looks specifically
    // for gettextCatalog.getString when generating the translation template.
    gettextCatalog: PropTypes.shape({
      getString: PropTypes.func.isRequired
    }).isRequired
  },

  getDefaultProps: () => {
    return {
      counts: {
        total: 0,
        approved: 0,
        rejected: 0,
        translated: 0,
        needswork: 0,
        untranslated: 0
      }
    }
  },

  componentWillMount: function () {
    const { onFilterChange } = this.props.actions
    this.filterApproved = onFilterChange.bind(undefined, STATUS_APPROVED)
    this.filterRejected = onFilterChange.bind(undefined, STATUS_REJECTED)
    this.filterTranslated = onFilterChange.bind(undefined, STATUS_TRANSLATED)
    this.filterNeedsWork = onFilterChange.bind(undefined, STATUS_NEEDS_WORK)
    this.filterUntranslated =
      onFilterChange.bind(undefined, STATUS_UNTRANSLATED)
  },

  render: function () {
    const { resetFilter } = this.props.actions
    const gettextCatalog = this.props.gettextCatalog

    return (
      <ul className="u-listHorizontal u-sizeHeight-1">
        <li className="u-sm-hidden u-sMV-1-4">
          <FilterToggle
            id="filter-phrases-total"
            className="u-textSecondary"
            isChecked={this.props.filter.all}
            onChange={resetFilter}
            title={gettextCatalog.getString('Total Phrases')}
            count={this.props.counts.total}
            withDot={false} />
        </li>
        <li className="u-ltemd-hidden u-sMV-1-4">
          <FilterToggle
            id="filter-phrases-approved"
            className="u-textHighlight"
            isChecked={this.props.filter.approved}
            onChange={this.filterApproved}
            title={gettextCatalog.getString('Approved')}
            count={this.props.counts.approved} />
        </li>
        <li className="u-ltemd-hidden u-sMV-1-4">
          <FilterToggle
            id="filter-phrases-translated"
            className="u-textSuccess"
            isChecked={this.props.filter.translated}
            onChange={this.filterTranslated}
            title={gettextCatalog.getString('Translated')}
            count={this.props.counts.translated} />
        </li>
        <li className="u-ltemd-hidden u-sMV-1-4">
          <FilterToggle
            id="filter-phrases-needs-work"
            className="u-textUnsure"
            isChecked={this.props.filter.needswork}
            onChange={this.filterNeedsWork}
            title={gettextCatalog.getString('Needs Work')}
            count={this.props.counts.needswork} />
        </li>
        <li className="u-ltemd-hidden u-sMV-1-4">
          <FilterToggle
            id="filter-phrases-rejected"
            className="u-textWarning"
            isChecked={this.props.filter.rejected}
            onChange={this.filterRejected}
            title={gettextCatalog.getString('Rejected')}
            count={this.props.counts.rejected} />
        </li>
        <li className="u-ltemd-hidden u-sMV-1-4">
          <FilterToggle
            id="filter-phrases-untranslated"
            className="u-textNeutral"
            isChecked={this.props.filter.untranslated}
            onChange={this.filterUntranslated}
            title={gettextCatalog.getString('Untranslated')}
            count={this.props.counts.untranslated} />
        </li>
  {/* A couple of parts of the Angular template that were not being used yet
        <li ng-show="appCtrl.PRODUCTION" class="u-sML-1-4">
          <button class="Link--neutral u-sizeHeight-1_1-2"
            title="{{::'Filters'|translate}}">
            <icon name="filter" title="{{::'Filters'|translate}}"
              class="u-sizeWidth-1_1-2"></icon>
          </button>
        </li>
        <li ng-show="appCtrl.PRODUCTION">
          <button class="Link--neutral u-sizeHeight-1_1-2"
            title="{{::'Search'|translate}}">
            <icon name="search" title="{{::'Search'|translate}}"
              class="u-sizeWidth-1_1-2"></icon>
          </button>
        </li>
  */}

      </ul>
    )
  }
})

export default TransUnitFilter
