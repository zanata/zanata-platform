import React, { Component } from 'react'
import { Sidebar } from '../../components'
import { Button } from 'react-bootstrap'
/**
 * Root component for About page
 */
class About extends Component {

  render () {
    /* eslint-disable react/jsx-no-bind, no-return-assign */
    return (
      <div>
        <Sidebar />
        <div className='flextab'>
          <h1>People</h1>
          <Button bsStyle='primary'>Add someone</Button>
        </div>
      </div>
    )
    /* eslint-enable react/jsx-no-bind, no-return-assign */
  }
}

export default About
