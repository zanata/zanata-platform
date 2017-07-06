jest.disableAutomock()

import React from 'react'
import ReactDOMServer from 'react-dom/server'
import SelectableDropdown from './SelectableDropdown'

describe('SelectableDropdownTest', () => {
  it('can render SelectableDropdown markup', () => {
    const clickFun = () => {}
    // Function used in TMMergeModal for a percentage selection dropdown
    const percentValueToDisplay = v => `${v}%`
    const actual = ReactDOMServer.renderToStaticMarkup(
      <SelectableDropdown title={'90'}
        id='dropdown-basic' classNameName='vmerge-ddown'
        onSelectDropdownItem={clickFun}
        selectedValue={90}
        valueToDisplay={percentValueToDisplay}
        values={[80, 90, 100]} />
    )
    const expected = ReactDOMServer.renderToStaticMarkup(
      <div className='dropdown btn-group btn-group-sm btn-group-default'>
        <button id='dropdown-basic' role='button' aria-haspopup='true'
          aria-expanded='false' type='button'
          className='dropdown-toggle btn btn-sm btn-default'>
          90 <span className='caret'></span>
        </button>
        <ul role='menu' className='dropdown-menu'
          aria-labelledby='dropdown-basic'>
          <li role='presentation' className=''>
            <a role='menuitem' tabIndex='-1' href='#'>80%</a>
          </li>
          <li role='presentation' className='active'>
            <a role='menuitem' tabIndex='-1' href='#'>90%</a>
          </li>
          <li role='presentation' className=''>
            <a role='menuitem' tabIndex='-1' href='#'>100%</a>
          </li>
        </ul>
      </div>
    )
    expect(actual).toEqual(expected)
  })
})
