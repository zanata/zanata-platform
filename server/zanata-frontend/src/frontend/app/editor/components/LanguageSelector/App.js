import React, { Component } from 'react'
import { I18n } from 'react-i18next'
import Button from '../Button'

/* eslint-disable react/jsx-no-bind */
class App extends Component {
  render () {
    return (
      <I18n ns="translations">
        {
          (t, { i18n }) => (
            <div className="App">
              <div className="App-header">
                <h1>{t('title')}</h1>
                <Button
                  title="Japanese"
                  className="EditorButton
                  Button--small u-rounded Button--primary"
                  onClick={() => i18n.changeLanguage('ja')}>ja
                </Button>
                <Button
                  title="English"
                  className="EditorButton
                  Button--small u-rounded Button--primary"
                  onClick={() => i18n.changeLanguage('en')}>en
                </Button>
              </div>
              <div className="App-intro">
                <h2>{t('description.part1')}</h2>
              </div>
              <div>{t('description.part2')}</div>
            </div>
          )
        }
      </I18n>
    )
  }
}

export default App
