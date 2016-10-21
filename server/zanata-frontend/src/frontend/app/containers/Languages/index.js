import React, { Component, PropTypes } from 'react'
import { ButtonRound } from 'zanata-ui'
import Helmet from 'react-helmet'
import {
  Page,
  ScrollView,
  View,
} from 'zanata-ui'

const contentViewContainerTheme = {
  base: {
    maw: 'Maw(r32)',
    m: 'Mx(a)',
    w: 'W(100%)'
  }
}

class Languages extends Component {
  render () {
    return (
      <Page>
        <Helmet title='Languages' />
        <ScrollView>
          <View theme={contentViewContainerTheme}>
            <ButtonRound type='primary'>testing</ButtonRound>
          </View>
        </ScrollView>
      </Page>
    )
  }
}

export default Languages