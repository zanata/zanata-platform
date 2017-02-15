import React, {Component} from 'react'
import { DropdownButton, MenuItem } from 'react-bootstrap'
/**
 * Root component for Sidebar
 */
class Sidebar extends Component {

  constructor (props) {
    super(props)
    this.state = {
      display: false,
      arrow: false,
      sideshow: false
    }
  }

  toggleDisplay () {
    this.setState({display: !this.state.display})
    this.setState({arrow: !this.state.arrow})
  }
  /* eslint-disable react/jsx-no-bind, no-return-assign */
  render () {
    const displayAccordion = this.state.display ? 'accordion-section-content' +
        ' open' : 'accordion-section-content'
    const toggleArrow = this.state.arrow ? 'hide-desktop up' : 'hide-desktop' +
        ' down'
    return (
      <div className='sidebar accordion'>
        <div className='sidebar-wrapper'>
          <div className='sidebar-container'>
            <div className='sidebar-content'>
              <a className='accordion-section-title'
                onClick={::this.toggleDisplay}>
                <svg className='projicon s2'>
                  <use xlinkHref='#Icon-project'></use>
                </svg>
                <span>Zanata Server</span>
                <svg className={toggleArrow}>
                  <use xlinkHref='#Icon-chevron-down'></use></svg>
              </a>
              <div id='accordion-1' className={displayAccordion}>
                <ul>
                  <li>
                    <a href='people.html'>People</a>
                  </li>
                  <li>
                    <a href='about.html'>About</a>
                  </li>
                  <li>
                    <a href='settings.html'>Settings</a>
                  </li>
                </ul>
                <div id='version'>
                  <div>
                    <span>VERSION</span>
                  </div>
                  <DropdownButton title='master'>
                    <MenuItem eventKey='1'>release</MenuItem>
                    <MenuItem eventKey='2'>test-1</MenuItem>
                  </DropdownButton>
                  <p><a href='vsettings.html'>Version settings</a></p>
                  <p><span>10%</span> translated</p>
                  <div>
                    <div></div>
                  </div>
                  <ul id='expList'>
                    <li className='active'>
                      <a href='languages.html'>Languages</a>
                    </li>
                    <li>
                      <a href='docs.html'>Documents</a>
                    </li>
                    <li>
                      <a href='groups.html'>Groups</a>
                    </li>
                    <li className='collapsed expanded'>
                      <a href=''>Options</a>
                      <ul>
                        <li><a href=''>Copy translations</a></li>
                        <li><a href=''>Merge tranlations</a></li>
                        <li><a href=''>Copy to new version</a></li>
                        <li><a href=''>Download config file</a></li>
                        <li><a href=''>Export version to TMX</a></li>
                      </ul>
                    </li>
                  </ul>
                </div>
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
