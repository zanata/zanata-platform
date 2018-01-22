import * as React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import { Link, Icon } from '../../components'

class AboutPage extends Component {
  static propTypes = {
    aboutText: PropTypes.string,
    aboutLink: PropTypes.string,
    linkName: PropTypes.string
  }

  render() {
    const { aboutText } = this.props
    return (
        <div className='flexTab wideView'>
          <h2>About</h2>
          <p>{aboutText}</p>

          <Link link={this.props.aboutLink} useHref>
            <Icon name='link' className='n1' />
            {this.props.linkName}
          </Link>
        </div>
    )
  }
}

export default AboutPage
