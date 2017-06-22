import React from 'react'
import PropTypes from 'prop-types'
import { Tab } from 'react-bootstrap'
import ActTabActSelect from '../components/ActTabActSelect'
import ActTabLangSelect from '../components/ActTabLangSelect'

class ActivityTab extends React.Component {

  static propTypes = {
    // eventKey prop to use for the bootstrap Tab
    eventKey: PropTypes.number.isRequired
  }

  render () {
    const { eventKey } = this.props
    return (
      <Tab eventKey={eventKey} title="">
        <div className="sidebar-wrapper" id="tab2">
          <ActTabLangSelect />
          <ActTabActSelect />
        </div>
      </Tab>
    )
  }
}

export default ActivityTab
