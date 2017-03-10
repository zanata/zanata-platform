jest.disableAutomock()

import React from 'react'
import ReactDOMServer from 'react-dom/server'
import TestUtils from 'react-addons-test-utils'
import UiLanguageDropdown from '../../app/editor/components/UiLanguageDropdown'
import Dropdown from '../../app/editor/components/Dropdown'

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
        isOpen={true}/>
    )

    const expected = ReactDOMServer.renderToStaticMarkup(
      <Dropdown onToggle={nowSeeHere}
                isOpen={true}
                className="Dropdown--right u-sMV-1-2">
        <Dropdown.Button>
          <a className="Link--invert u-inlineBlock u-textNoWrap u-sPH-1-4">
            Setswana
          </a>
        </Dropdown.Button>
        <Dropdown.Content>
          <ul>
            <li key="st">
              <a className="Dropdown-item">
                Sesotho
              </a>
            </li>
            <li key="tn">
              <a className="Dropdown-item">
                Setswana
              </a>
            </li>
            <li key="sw">
              <a className="Dropdown-item">
                Swahili
              </a>
            </li>
            <li key="zu">
              <a className="Dropdown-item">
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
        isOpen={true}/>
    )
    const list = TestUtils.scryRenderedDOMComponentsWithTag(
      uiLangDropdown, 'a')
    TestUtils.Simulate.click(list[3])
    expect(myRoar).toEqual('thunderous',
      'changeUiLocale callback should run when a language is clicked')
  })
})
