/* global jest describe expect it */

import React from 'react'
import * as ReactDOMServer from 'react-dom/server'
import TestUtils from 'react-dom/test-utils'
import UiLanguageDropdown from '.'
import Dropdown from '../Dropdown'

describe('UiLanguageDropdownTest', () => {
  it('UiLanguageDropdown markup', () => {
    const workingOnMyRoar = () => {}
    const nowSeeHere = () => {}
    const actual = ReactDOMServer.renderToStaticMarkup(
      <UiLanguageDropdown
        changeUiLocale={workingOnMyRoar}
        selectedUiLocale="tn"
        uiLocales={{
          st: {
            id: 'st',
            name: 'Sesotho'
          },
          tn: {
            id: 'tn',
            name: 'Setswana'
          },
          sw: {
            id: 'sw',
            name: 'Swahili'
          },
          zu: {
            id: 'zu',
            name: 'Zulu'
          }
        }}
        toggleDropdown={nowSeeHere}
        isOpen />
    )

    const expected = ReactDOMServer.renderToStaticMarkup(
      <Dropdown onToggle={nowSeeHere}
        isOpen
        className="Dropdown--right u-sMV-1-2">
        <Dropdown.Button>
          <a className="Link--invert u-inlineBlock u-textNoWrap u-sPH-1-4">
            Setswana
          </a>
        </Dropdown.Button>
        <Dropdown.Content>
          <ul>
            <li key="st">
              <a className="EditorDropdown-item">
                Sesotho
              </a>
            </li>
            <li key="tn">
              <a className="EditorDropdown-item">
                Setswana
              </a>
            </li>
            <li key="sw">
              <a className="EditorDropdown-item">
                Swahili
              </a>
            </li>
            <li key="zu">
              <a className="EditorDropdown-item">
                Zulu
              </a>
            </li>
          </ul>
        </Dropdown.Content>
      </Dropdown>
    )
    expect(actual).toEqual(expected)
  })

  it('UiLanguageDropdown events', () => {
    let myRoar = 'puny'
    const workingOnMyRoar = (newLocale) => {
      myRoar = 'thunderous'
      expect(newLocale).toEqual(
        {localeId: 'sw', name: 'Swahili'},
        'should call changeUiLocale callback with a well-formatted locale object')
    }
    const nowSeeHere = () => {}

    const locales = {
      st: {
        id: 'st',
        name: 'Sesotho'
      },
      tn: {
        id: 'tn',
        name: 'Setswana'
      },
      sw: {
        id: 'sw',
        name: 'Swahili'
      },
      zu: {
        id: 'zu',
        name: 'Zulu'
      }
    }

    const uiLangDropdown = TestUtils.renderIntoDocument(
      <UiLanguageDropdown
        changeUiLocale={workingOnMyRoar}
        selectedUiLocale="tn"
        uiLocales={locales}
        toggleDropdown={nowSeeHere}
        isOpen />
    )
    const list = TestUtils.scryRenderedDOMComponentsWithTag(
      uiLangDropdown, 'a')
    TestUtils.Simulate.click(list[3])
    expect(myRoar).toEqual('thunderous',
      'changeUiLocale callback should run when a language is clicked')
  })
})
