import React, {Component} from 'react'

/**
 * Root component for Explore page
 */
class Sidebar extends Component {
  /* eslint-disable react/jsx-no-bind, no-return-assign */
  render () {
    return (
      <div className='page scroll-view-theme'>
        <div className='sidebar accordion'>
          <div className='sidebar-wrapper'>
            <div className='sidebar-container'>
              <div className='sidebar-content'>
                <a className='accordion-section-title' href='#accordion-1'
                  style='color:#54667a; padding-top: 20px;padding-bottom:10px;
                font-size: 2.25em;line-height: 1.08333em;
                letter-spacing: -0.05em;'>
                  <svg style='fill:#54667a;height: 30px;width: 30px;
                  margin-bottom: 2px;'>
                    <use xlinkHref='#Icon-project'></use>
                  </svg>
                  <span style='vertical-align: text-top;'>Zanata Server</span>
                  <svg className='hide-desktop'
                    style='fill: rgb(84, 102, 122);
                  height: 30px; width: 30px;
                  right: 10px; position: absolute; transform: rotate(0deg);
                  transform-origin: 50% 50% 0px;'>
                    <use xlinkHref='#Icon-chevron-down'></use></svg>
                </a>
                <div id='accordion-1' className='accordion-section-content'>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div className='flextab'>
        </div>
      </div>
    )
    /* eslint-enable react/jsx-no-bind, no-return-assign */
  }
}
export default Sidebar
