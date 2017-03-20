/**
 * Panel to search the glossary and display glossary terms.
 */

import React, { PropTypes } from 'react'
import { connect } from 'react-redux'
import { Button, Tab, Table } from 'react-bootstrap'
import GlossarySearchInput from '../components/GlossarySearchInput'
import IconButton from '../components/IconButton'
import { glossarySearchTextEntered } from '../actions/glossary'
import { isEmpty } from 'lodash'

// FIXME need a modal to open when this is clicked
const logDetailsClick = () => {
}

const GlossaryTab = React.createClass({
  propTypes: {
    // eventKey prop to use for the bootstrap Tab
    eventKey: PropTypes.number.isRequired,
    searchText: PropTypes.string.isRequired,
    searching: PropTypes.bool.isRequired,
    results: PropTypes.arrayOf(PropTypes.shape({
      source: PropTypes.shape({
        content: PropTypes.string.isRequired
      }).isRequired,
      target: PropTypes.shape({
        content: PropTypes.string.isRequired
      }).isRequired
    })).isRequired,
    onGlossaryTextChange: PropTypes.func.isRequired
  },

  renderResultsPanel () {
    const { results, searching, searchText } = this.props

    if (searching) {
      return 'Searching...'
    }

    if (isEmpty(searchText)) {
      return 'Enter text to search'
    }

    if (isEmpty(results)) {
      return 'No results'
    }

    const resultsDisplay = results.map((term, index) => {
      return (
        <tr key={index}>
          <td className="bold-text">{term.source.content}</td>
          <td className="bold-text">{term.target.content}</td>
          <td>
            <Button title="copy"
              className="Button Button--small u-rounded Button--primary">
              Copy
            </Button>
          </td>
          <td className="align-center">
            <IconButton
              icon="info"
              title="Details"
              className="Button--link"
              onClick={logDetailsClick}
            />
          </td>
        </tr>
      )
    })

    return (
      <Table reponsive>
        <thead>
          <tr>
            <th>Source term</th>
            <th>Target term</th>
            <th></th>
            <th className="align-center">Details</th>
          </tr>
        </thead>
        <tbody>
          {resultsDisplay}
        </tbody>
      </Table>
    )
  },

  render () {
    const { eventKey, searchText, onGlossaryTextChange } = this.props
    return (
      <Tab eventKey={eventKey} title="">
        <div className="sidebar-wrapper" id="tab2">
          <GlossarySearchInput
            text={searchText}
            onTextChange={onGlossaryTextChange} />
        </div>
        {this.renderResultsPanel()}
      </Tab>
    )
  }

})

function mapStateToProps (state) {
  const { context, glossary, headerData } = state

  const sourceLanguage = context.sourceLocale.localeId
  const targetLanguage = headerData.context.selectedLocale

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

  return {
    ...glossary,
    results: glossary.results.map(result => {
      return {
        source: result[sourceLanguage],
        target: result[targetLanguage]
      }
    })
  }
}

function mapDispatchToProps (dispatch) {
  return {
    onGlossaryTextChange: event =>
        dispatch(glossarySearchTextEntered(event.target.value))
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(GlossaryTab)
