import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import Tooltip from 'antd/lib/tooltip'
import 'antd/lib/tooltip/style/css'
import React from 'react'

class MTSuggestionsButton extends React.Component {

  render () {
    const text = (<a href='https://translate.google.com/'><img src='http://zanata.org/images/white-short.png'
     alt='Translated by Google' /></a>)
    return (
      <span className='Button--MT'>
        <Tooltip placement='right' title={text}>
          <Button className='Button--snug u-roundish Button--neutral'>
           MT
          </Button>
        </Tooltip>
      </span>
    )
  }
}

export default MTSuggestionsButton
