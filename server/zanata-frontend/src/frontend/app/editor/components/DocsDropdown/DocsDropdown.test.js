jest.disableAutomock()

import React from 'react'
import ReactDOMServer from 'react-dom/server'
import DocsDropdown from '.'
import Dropdown from '../Dropdown'
import { Icon } from '../../../components'
import { Row } from 'react-bootstrap'

describe('DocsDropdownTest', () => {
  it('DocsDropdown markup', () => {
    const eatLettuce = () => {}
    const actual = ReactDOMServer.renderToStaticMarkup(
      <DocsDropdown
        context={{
          projectVersion: {
            project: {
              slug: 'slimy'
            },
            version: 'slick',
            docs: [
              'snail.txt',
              'gastropod.txt',
              'cephalopod.txt'
            ]
          },
          selectedDoc: {
            id: 'gastropod.txt'
          },
          selectedLocale: 'sluggish'
        }}
        toggleDropdown={eatLettuce}
        isOpen={true}/>
    )

    const expected = ReactDOMServer.renderToStaticMarkup(
      <Dropdown onToggle={eatLettuce}
                isOpen={true}>
        <Dropdown.Button>
          <button className="Link--invert">
            <Row>
              gastropod.txt
              <div className="u-sML-1-8 EditorDropdown-toggleIcon">
                <Icon name="chevron-down" className="s1" />
              </div>
            </Row>
          </button>
        </Dropdown.Button>
        <Dropdown.Content>
          <ul>
            <li key="snail.txt">
              <a href="/project/translate/slimy/v/slick/snail.txt?lang=sluggish"
                 className="EditorDropdown-item">snail.txt</a>
            </li>
            <li key="gastropod.txt">
              <a href="/project/translate/slimy/v/slick/gastropod.txt?lang=sluggish"
                 className="EditorDropdown-item">gastropod.txt</a>
            </li>
            <li key="cephalopod.txt">
              <a href="/project/translate/slimy/v/slick/cephalopod.txt?lang=sluggish"
                 className="EditorDropdown-item">cephalopod.txt</a>
            </li>
          </ul>
        </Dropdown.Content>
      </Dropdown>
    )
    expect(actual).toEqual(expected)
  })
})
