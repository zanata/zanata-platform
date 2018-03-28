import React from 'react'
import * as ReactDOMServer from 'react-dom/server'
import * as TestUtils from 'react-dom/test-utils'
import Dropdown from '.'

describe('DropdownTest', () => {
  it('Dropdown markup (closed)', () => {
    const toggleTheDoor = () => {}
    const actual = ReactDOMServer.renderToStaticMarkup(
      <Dropdown
        onToggle={toggleTheDoor}
        isOpen={false}
        enabled={true}
        className="boom acka lacka">
        <Dropdown.Button>
          <button>Boom boom acka lacka lacka boom</button>
        </Dropdown.Button>
        <Dropdown.Content>
          <ul>
            <li>Open the door</li>
            <li>Get on the floor</li>
            <li>Everybody walk the dinosaur</li>
          </ul>
          <a href="https://youtu.be/vgiDcJi534Y">Was Not Was</a>
        </Dropdown.Content>
      </Dropdown>
    )

    const expected = ReactDOMServer.renderToStaticMarkup(
      <div className="EditorDropdown boom acka lacka">
        <div className="EditorDropdown-toggle"
             aria-haspopup={true}
             aria-expanded={false}
             onClick={toggleTheDoor}>
          <button>Boom boom acka lacka lacka boom</button>
        </div>
        <div className="EditorDropdown-content">
          <ul>
            <li>Open the door</li>
            <li>Get on the floor</li>
            <li>Everybody walk the dinosaur</li>
          </ul>
          <a href="https://youtu.be/vgiDcJi534Y">Was Not Was</a>
        </div>
      </div>
    )
    expect(actual).toEqual(expected)
  })

  it('Dropdown markup (open)', () => {
    const toggleTheDoor = () => {}
    const actual = ReactDOMServer.renderToStaticMarkup(
      <Dropdown
        onToggle={toggleTheDoor}
        isOpen={true}
        enabled={true}
        className="boom acka lacka">
        <Dropdown.Button>
          <button>Boom boom acka lacka lacka boom</button>
        </Dropdown.Button>
        <Dropdown.Content>
          <ul>
            <li>Open the door</li>
            <li>Get on the floor</li>
            <li>Everybody walk the dinosaur</li>
          </ul>
          <a href="https://youtu.be/83nFiPoSuzU">Was Not Was</a>
        </Dropdown.Content>
      </Dropdown>
    )

    const expected = ReactDOMServer.renderToStaticMarkup(
      <div className="EditorDropdown is-active boom acka lacka">
        <div className="EditorDropdown-toggle"
             aria-haspopup={true}
             aria-expanded={true}
             onClick={toggleTheDoor}>
          <button>Boom boom acka lacka lacka boom</button>
        </div>
        <div className="EditorDropdown-content">
          <ul>
            <li>Open the door</li>
            <li>Get on the floor</li>
            <li>Everybody walk the dinosaur</li>
          </ul>
          <a href="https://youtu.be/83nFiPoSuzU">Was Not Was</a>
        </div>
      </div>
    )
    expect(actual).toEqual(expected)
  })

  it('Dropdown events', () => {
    let theDoor = 'closed'
    // @ts-ignore
    const toggleTheDoor = (buttonDOMNode) => {
      theDoor = 'open'
    }

    const dinoWalkDropdown = TestUtils.renderIntoDocument(
      <Dropdown
        onToggle={toggleTheDoor}
        isOpen={true}
        enabled={true}
        className="boom acka lacka">
        <Dropdown.Button>
          <button>Boom boom acka lacka lacka boom</button>
        </Dropdown.Button>
        <Dropdown.Content>
          <ul>
            <li>Open the door</li>
            <li>Get on the floor</li>
            <li>Everybody walk the dinosaur</li>
          </ul>
          <a href="https://youtu.be/83nFiPoSuzU">Was Not Was</a>
        </Dropdown.Content>
      </Dropdown>
    )

    const list = TestUtils.scryRenderedDOMComponentsWithClass(
      // @ts-ignore
      dinoWalkDropdown, 'EditorDropdown-toggle')
    TestUtils.Simulate.click(list[0])

    // @ts-ignore
    expect(theDoor).toEqual('open',
      'click on dropdown button should trigger given toggle function')
  })

  it('Dropdown disabled', () => {
    let theDoor = 'closed'
    const toggleTheDoor = () => {
      theDoor = 'open'
    }
    const dinoWalkDropdown = TestUtils.renderIntoDocument(
      <Dropdown
        onToggle={toggleTheDoor}
        isOpen={true}
        enabled={false}
        className="boom acka lacka">
        <Dropdown.Button>
          <button>Boom boom acka lacka lacka boom</button>
        </Dropdown.Button>
        <Dropdown.Content>
          <ul>
            <li>Open the door</li>
            <li>Get on the floor</li>
            <li>Everybody walk the dinosaur</li>
          </ul>
          <a href="https://youtu.be/83nFiPoSuzU">Was Not Was</a>
        </Dropdown.Content>
      </Dropdown>
    )

    // throws if onClick is not bound
    try {
      const list = TestUtils.scryRenderedDOMComponentsWithClass(
        // @ts-ignore
        dinoWalkDropdown, 'EditorDropdown-toggle')
      TestUtils.Simulate.click(list[0])
      // dinoWalkDropdown.toggleDropdown()
    } catch (e) {
      // swallow on purpose, valid for code to not bind onClick
    }
    // @ts-ignore
    expect(theDoor).toEqual('closed',
      'click on disabled dropdown button should not ' +
      'trigger given toggle function')
  })
})
