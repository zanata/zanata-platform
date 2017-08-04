import React, {Component} from 'react'
import { DropdownButton, MenuItem, ProgressBar,
  Nav, NavItem } from 'react-bootstrap'
import { Icon } from '../../components'
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
    this.setState(prevState => ({display: !prevState.display}))
    this.setState(prevState => ({arrow: !prevState.arrow}))
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
                <Icon name='project' className='projicon' />
                <span className='projtitle'>Zanata Server</span>
                <svg className={toggleArrow}>
                  <use xlinkHref='#Icon-chevron-down'></use></svg>
              </a>
              <div id='accordion-1' className={displayAccordion}>
                <Nav bsStyle='pills' stacked activeKey={1}>
                  <NavItem eventKey={1} href=''>
                    <Icon name='users' className='s1 sidebar-icon' />
                  People</NavItem>
                  <NavItem eventKey={2} href=''>
                    <Icon name='info' className='s1 sidebar-icon' />
                  About</NavItem>
                  <NavItem eventKey={3} href=''>
                    <Icon name='settings' className='s1 sidebar-icon' />
                  Settings </NavItem>
                </Nav>
                <DropdownButton title='Options' id='options-dropdown'
                  className='btn-sm btn-default'>
                  <MenuItem eventKey='1'>Copy translations</MenuItem>
                  <MenuItem eventKey='2'>Merge translations</MenuItem>
                  <MenuItem eventKey='3'>Copy to new version</MenuItem>
                  <MenuItem eventKey='4'>Download config file</MenuItem>
                  <MenuItem eventKey='5'>Export version to TMX</MenuItem>
                </DropdownButton>
                <div id='version'>
                  <div className='version-inline'>
                    <span className='sidebar-version-title'>
                      <Icon name='version' className='s2' />
                      <span className='v-heading'>VERSION</span>
                    </span>
                    <DropdownButton id='version-dropdown'
                      className='btn-sm btn-default'
                      title='master'>
                      <MenuItem eventKey='1'>release</MenuItem>
                      <MenuItem eventKey='2'>test-1</MenuItem>
                    </DropdownButton>
                  </div>
                  <p className='version-settings'>
                    <a href=''>Version settings</a>
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
                  <Nav className='v-links'
                    bsStyle='pills' stacked activeKey={1}>
                    <NavItem eventKey={1} href=''>
                    Languages</NavItem>
                    <NavItem eventKey={2} href=''>
                    Documents</NavItem>
                    <NavItem eventKey={3} href=''>
                    Groups </NavItem>
                  </Nav>
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
