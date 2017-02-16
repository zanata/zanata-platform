import React, {Component} from 'react'
import { DropdownButton, MenuItem, ProgressBar } from 'react-bootstrap'
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
                <svg className='projicon'>
                  <use xlinkHref='#Icon-project'></use>
                </svg>
                <span>Zanata Server</span>
                <svg className={toggleArrow}>
                  <use xlinkHref='#Icon-chevron-down'></use></svg>
              </a>
              <div id='accordion-1' className={displayAccordion}>
                <ul>
                  <li>
                    <a href='people.html'>
                      <svg className='s1 sidebar-icon'>
                        <use xlinkHref='#Icon-users'></use>
                      </svg>
                    People</a>
                  </li>
                  <li>
                    <a href='about.html'>
                      <svg className='s1 sidebar-icon'>
                        <use xlinkHref='#Icon-info'></use>
                      </svg>
                    About</a>
                  </li>
                  <li>
                    <a href='settings.html'>
                      <svg className='s1 sidebar-icon'>
                        <use xlinkHref='#Icon-settings'></use>
                      </svg>
                    Settings</a>
                  </li>
                </ul>
                <DropdownButton title='Options'
                  className='btn-sm btn-primary'>
                  <MenuItem eventKey='1'>Copy translations</MenuItem>
                  <MenuItem eventKey='2'>Merge translations</MenuItem>
                  <MenuItem eventKey='3'>Copy to new version</MenuItem>
                  <MenuItem eventKey='4'>Download config file</MenuItem>
                  <MenuItem eventKey='5'>Export version to TMX</MenuItem>
                </DropdownButton><br />
                <div id='version'>
                  <div className='version-inline'>
                    <span className='sidebar-version-title'>
                      <svg className='s2'>
                        <use xlinkHref='#Icon-version'></use>
                      </svg>
                    VERSION</span>
                    <DropdownButton className='btn-sm' title='master'>
                      <MenuItem eventKey='1'>release</MenuItem>
                      <MenuItem eventKey='2'>test-1</MenuItem>
                    </DropdownButton>
                  </div>
                  <p className='version-settings'>
                    <a href='vsettings.html'>Version settings</a>
                  </p>
                  <div className='version-percent'>
                    <p><span className='percent'>10%</span> translated</p>
                  </div>
                  <ProgressBar>
                    <ProgressBar className='progress-bar-translated'
                      now={10} key={1} />
                    <ProgressBar className='progress-bar-fuzzy'
                      now={7} key={2} />
                    <ProgressBar className='progress-bar-rejected'
                      now={3} key={3} />
                    <ProgressBar className='progress-bar-approved'
                      now={10} key={4} />
                  </ProgressBar>
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
