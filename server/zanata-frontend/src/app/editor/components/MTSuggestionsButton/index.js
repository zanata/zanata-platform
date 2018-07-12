import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import Tooltip from 'antd/lib/tooltip'
import 'antd/lib/tooltip/style/css'
import React from 'react'
import * as PropTypes from 'prop-types'

class MTSuggestionsButton extends React.Component {
  static propTypes = {
    backendId: PropTypes.string.isRequired
  }
  render () {
    const backendId = 'Google'
    const title = this.props.backendId === 'Google' ?
        <a href='https://translate.google.com/'><img src='http://zanata.org/images/translated-by-google-white-short.png'
           alt='Translated by Google' /></a>
        : 'Translated by ' + this.props.backendId
    return (
      <span className='Button--MT'>
        <Tooltip placement='right' title={title}>
          <Button className='Button--snug u-roundish Button--neutral'>
           MT
          </Button>
        </Tooltip>
      </span>
    )
  }
}

export default MTSuggestionsButton
