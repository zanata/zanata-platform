jest.disableAutomock()

import React from 'react'
import ReactDOMServer from 'react-dom/server'
import TestUtils from 'react-addons-test-utils'
import ToggleSwitch from '../../app/components/ToggleSwitch'

describe('ToggleSwitchTest', () => {
  it('ToggleSwitch markup', () => {
    const switchTheBlade = () => {}
    const actual = ReactDOMServer.renderToStaticMarkup(
      <ToggleSwitch id="switchblade"
                    className="bayonet"
                    isChecked={true}
                    onChange={switchTheBlade}
                    label="Switchington"/>
    )

    const expected = ReactDOMServer.renderToStaticMarkup(
      <span className="Switch bayonet">
      <input className="Switch-checkbox"
             type="checkbox"
             id="switchblade"
             checked={true}
             onChange={switchTheBlade}/>
      <label className="Switch-label" htmlFor="switchblade">
        <span className="Switch-labelText">Switchington</span>
      </label>
    </span>
    )
    expect(actual).toEqual(expected)
  })

  it('ToggleSwitch markup (unchecked)', () => {
    const switchTheBlade = () => {}
    const actual = ReactDOMServer.renderToStaticMarkup(
      <ToggleSwitch id="switchblade"
                    className="bayonet"
                    isChecked={false}
                    onChange={switchTheBlade}
                    label="Switchington"/>
    )

    const expected = ReactDOMServer.renderToStaticMarkup(
      <span className="Switch bayonet">
      <input className="Switch-checkbox"
             type="checkbox"
             id="switchblade"
             checked={false}
             onChange={switchTheBlade}/>
      <label className="Switch-label" htmlFor="switchblade">
        <span className="Switch-labelText">Switchington</span>
      </label>
    </span>
    )
    expect(actual).toEqual(expected)
  })

  it('ToggleSwitch onchange', () => {
    let blade = 'retracted'
    const switchTheBlade = () => {
      blade = 'extended'
    }

    const switchComponent = TestUtils.renderIntoDocument(
      <ToggleSwitch id="switchblade"
                    className="bayonet"
                    isChecked={false}
                    onChange={switchTheBlade}
                    label="Switchington"/>
    )
    switchComponent.props.onChange()
    expect(blade).toEqual('extended',
      'should call onChange action when input is changed')
  })
})
