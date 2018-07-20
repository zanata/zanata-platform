// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import Sidebar from '../../components/Sidebar/index'

class AboutPage extends Component {
  render() {
    const content = 'Test'
    return (
      <Sidebar content={content}/>
    )
  }
}

export default AboutPage
