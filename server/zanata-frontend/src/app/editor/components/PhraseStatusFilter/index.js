import FilterToggle from '../FilterToggle'
import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import { connect } from 'react-redux'
import {
  STATUS_UNTRANSLATED,
  STATUS_NEEDS_WORK,
  STATUS_TRANSLATED,
  STATUS_APPROVED,
  STATUS_REJECTED
} from '../../utils/status-util'
import {
  resetStatusFilter,
  updateStatusFilter
} from '../../actions/phrases-filter-actions'

/**
 * Panel with controls to filter the list of trans units
 */
export class PhraseStatusFilter extends Component {
  static propTypes = {
    resetFilter: PropTypes.func.isRequired,
    onFilterChange: PropTypes.func.isRequired,

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
  }

  static defaultProps = {
    counts: {
      total: 0,
      approved: 0,
      rejected: 0,
      translated: 0,
      needswork: 0,
      untranslated: 0
    }
  }

  filterApproved = () => this.props.onFilterChange(STATUS_APPROVED)
  filterRejected = () => this.props.onFilterChange(STATUS_REJECTED)
  filterTranslated = () => this.props.onFilterChange(STATUS_TRANSLATED)
  filterNeedsWork = () => this.props.onFilterChange(STATUS_NEEDS_WORK)
  filterUntranslated = () => this.props.onFilterChange(STATUS_UNTRANSLATED)

  render () {
    const { gettextCatalog, resetFilter } = this.props

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
}

const mapStateToProps = ({
  // TODO move counts to a more appropriate place in state
  // @ts-ignore any
  headerData: { context: { selectedDoc: { counts } } },
  // @ts-ignore any
  phrases: { filter: { status } },
  // @ts-ignore any
  ui: { gettextCatalog }}) => {
  return {
    counts,
    filter: status,
    gettextCatalog
  }
}

// @ts-ignore any
function mapDispatchToProps (dispatch) {
  return {
    resetFilter: () => dispatch(resetStatusFilter()),
    // @ts-ignore any
    onFilterChange: (status) => dispatch(updateStatusFilter(status))
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(PhraseStatusFilter)
