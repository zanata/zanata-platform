/**
 * Panel to search the glossary and display glossary terms.
 */

import React from 'react'
import PropTypes from 'prop-types'
import { connect } from 'react-redux'
import { Tab, Table } from 'react-bootstrap'
import GlossarySearchInput from '../components/GlossarySearchInput'
import GlossaryTerm from '../components/GlossaryTerm'
import GlossaryTermModal from '../components/GlossaryTermModal'
import {
  copyGlossaryTerm,
  glossarySearchTextEntered,
  showGlossaryTermDetails
} from '../actions/glossary-actions'
import { isEmpty } from 'lodash'
import { Icon, LoaderText } from '../../components'

class GlossaryTab extends React.Component {
  static propTypes = {
    copyGlossaryTerm: PropTypes.func.isRequired,
    details: PropTypes.shape({
      show: PropTypes.bool.isRequired
    }).isRequired,
    // eventKey prop to use for the bootstrap Tab
    eventKey: PropTypes.number.isRequired,
    searchText: PropTypes.string.isRequired,
    searching: PropTypes.bool.isRequired,
    showDetails: PropTypes.func.isRequired,
    results: PropTypes.arrayOf(PropTypes.shape({
      source: PropTypes.string.isRequired,
      target: PropTypes.string.isRequired
    })).isRequired,
    onGlossaryTextChange: PropTypes.func.isRequired,
    isRTLSource: PropTypes.bool.isRequired,
    isRTLTarget: PropTypes.bool.isRequired
  }

  renderResultsPanel = () => {
    const {
      copyGlossaryTerm,
      results,
      searching,
      searchText,
      showDetails,
      isRTLSource,
      isRTLTarget
    } = this.props

    if (searching) {
      return <div className="GlossarySearch">
        <LoaderText loading loadingText='Searching' />
      </div>
    }

    if (isEmpty(searchText)) {
      return <div className="Search-enterText">Enter text to search <br />
        <span>
          <Icon name='search' className="s6" />
        </span>
      </div>
    }

    if (isEmpty(results)) {
      return <div className="Glossary-noResults">
        No results<br />
        <span>
          <Icon name='glossary' className="s6" />
        </span>
      </div>
    }

    const directionClassSource = isRTLSource ? 'rtl' : 'ltr'
    const directionClassTarget = isRTLTarget ? 'rtl' : 'ltr'

    const resultsDisplay = results.map((term, index) => {
      return (
        <GlossaryTerm {...{
          key: index,
          index,
          term,
          copyGlossaryTerm,
          showDetails,
          directionClassSource,
          directionClassTarget
        }} />
      )
    })

    return (
      <Table>
        <thead className="hide-small">
          <tr>
            <th>Source</th>
            <th>Target</th>
            <th></th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {resultsDisplay}
        </tbody>
      </Table>
    )
  }

  render () {
    const glossaryModal = this.props.details.show
      ? <GlossaryTermModal /> : undefined

    const {eventKey, searchText, onGlossaryTextChange, isRTLSource} = this.props

    const directionClass = isRTLSource ? 'rtl' : 'ltr'
    return (
      <Tab eventKey={eventKey} title="">
        <div className="SidebarEditor-wrapper" id="tab1">
          <GlossarySearchInput
            text={searchText}
            onTextChange={onGlossaryTextChange}
            directionClass={directionClass} />
        </div>
        {this.renderResultsPanel()}
        {glossaryModal}
      </Tab>
    )
  }
}

function mapStateToProps (state) {
  const {glossary, context, ui} = state
  // FIXME move this to storybook for this component
  // const dummyData = [
  //   {
  //     source: 'dog',
  //     target: 'Hund'
  //   },
  //   {
  //     source: 'sausage',
  //     target: 'Wurst'
  //   },
  //   {
  //     source: 'incomprehensibilities',
  //     target: 'Unverständlichkeiten'
  //   },
  //   {
  //     source: 'tree',
  //     target: 'Baum'
  //   },
  //   {
  //     source: 'head district chimney sweep',
  //     target: 'Bezirksschornsteinfegermeister'
  //   },
  //   {
  //     source: 'German',
  //     target: 'Deutsche'
  //   }
  // ]

  const results = glossary.results.get(glossary.searchText) || []
  const targetLocaleDetails = ui.uiLocales[context.lang]
  return {
    ...glossary,
    results,
    isRTLSource: context.sourceLocale.isRTL,
    isRTLTarget: targetLocaleDetails ? targetLocaleDetails.isRTL || false
        : false
  }
}

function mapDispatchToProps (dispatch) {
  return {
    copyGlossaryTerm: index => dispatch(copyGlossaryTerm(index)),
    onGlossaryTextChange: event =>
        dispatch(glossarySearchTextEntered(event.target.value)),
    showDetails: term => dispatch(showGlossaryTermDetails(term))
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(GlossaryTab)
