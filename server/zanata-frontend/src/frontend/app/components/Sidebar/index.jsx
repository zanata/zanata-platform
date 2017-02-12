import React, {Component} from 'react'
// import { Accordion } from 'react-bootstrap'
/**
 * Root component for Sidebar
 */
class Sidebar extends Component {
  /* eslint-disable react/jsx-no-bind, no-return-assign */
  render () {
    return (
      <div className='sidebar accordion'>
        <div className='sidebar-wrapper'>
          <div className='sidebar-container'>
            <div className='sidebar-content'>
              <a className='accordion-section-title' href='#accordion-1'>
                <svg className='projicon'>
                  <use xlinkHref='#Icon-project'></use>
                </svg>
                <span>Zanata Server</span>
                <svg className='hide-desktop'>
                  <use xlinkHref='#Icon-chevron-down'></use></svg>
              </a>
              <div id='accordion-1' className='accordion-section-content'>
              </div>
            </div>
          </div>
        </div>
      </div>
    )
    /* eslint-enable react/jsx-no-bind, no-return-assign */
  }
}
export default Sidebar
