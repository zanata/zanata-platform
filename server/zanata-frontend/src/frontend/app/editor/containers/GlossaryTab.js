/**
 * Panel to search the glossary and display glossary terms.
 */

import React, { PropTypes } from 'react'
import { connect } from 'react-redux'
import { Button, Tab, Table, Tooltip, OverlayTrigger }
  from 'react-bootstrap'
import GlossarySearchInput from '../components/GlossarySearchInput'
import IconButton from '../components/IconButton'
import { glossarySearchTextEntered } from '../actions/glossary'
import { isEmpty } from 'lodash'
import { Icon, LoaderText } from '../../components'

// FIXME need a modal to open when this is clicked
const logDetailsClick = () => {
}

const tooltip = (
  <Tooltip>
    longwordtextthingy
  </Tooltip>
)

const GlossaryTab = React.createClass({
  propTypes: {
    // eventKey prop to use for the bootstrap Tab
    eventKey: PropTypes.number.isRequired,
    searchText: PropTypes.string.isRequired,
    searching: PropTypes.bool.isRequired,
    results: PropTypes.arrayOf(PropTypes.shape({
      source: PropTypes.string.isRequired,
      target: PropTypes.string.isRequired
    })).isRequired,
    onGlossaryTextChange: PropTypes.func.isRequired
  },

  renderResultsPanel () {
    const { results, searching, searchText } = this.props

    if (searching) {
      return <div className="search-glos">
        <LoaderText loading loadingText='Searching...' />
      </div>
    }

    if (isEmpty(searchText)) {
      return <div className="enter-search-text">Enter text to search <br />
        <span>
          <Icon name='search' className="s6" />
        </span>
      </div>
    }

    if (isEmpty(results)) {
      return <div className="no-gloss-results">
        No results<br />
        <span>
          <Icon name='glossary' className="s6" />
        </span>
      </div>
    }

    const resultsDisplay = results.map((term, index) => {
      return (
        <tr key={index}>
          <td id="long-string" data-filetype="text" className="gloss-text">
            <OverlayTrigger placement="top" overlay={tooltip}>
              <Button bStyle="link">
                <span>
                  <span className="hide-mdplus text-blue">
                    Source
                  </span>
                  {term.source}
                </span>
              </Button>
            </OverlayTrigger>
          </td>
          <td id="long-string" data-filetype="text" className="gloss-text">
            <OverlayTrigger placement="top" overlay={tooltip}>
              <Button bStyle="link">
                <span>
                  <span className="hide-mdplus text-blue">
                  Target
                  </span>
                    {term.target}
                </span>
              </Button>
            </OverlayTrigger>
          </td>
          <td>
            <Button title="Copy"
              className="Button Button--small u-rounded Button--primary">
              Copy
            </Button>
          </td>
          <td className="info-icon">
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
      <Table>
        <thead className="hide-small">
          <tr>
            <th>Source</th>
            <th>Target</th>
            <th></th>
            <th className="hide-md align-center">Details</th>
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

function mapStateToProps ({ glossary }) {
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
    ...glossary
  }
}

function mapDispatchToProps (dispatch) {
  return {
    onGlossaryTextChange: event =>
        dispatch(glossarySearchTextEntered(event.target.value))
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(GlossaryTab)
