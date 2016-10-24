import { React, Component } from 'react'
import Loading from 'react-loading'

class Loader extends Component {
  render () {
    return (
      <Loading type='balls' color='#e3e3e3' />
    )
  }
}

export default Loader
