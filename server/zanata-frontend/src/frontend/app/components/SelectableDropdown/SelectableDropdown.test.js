jest.disableAutomock()

import React from 'react'
import ReactDOMServer from 'react-dom/server'
import SelectableDropdown from '.'
import {MenuItem, DropdownButton} from 'react-bootstrap'

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
      <DropdownButton id={'dropdown-basic'} bsStyle={'default'}
        bsSize={'sm'} title={'90'}>
        <MenuItem onClick={clickFun} active={false}>
          80%
        </MenuItem>
        <MenuItem onClick={clickFun} active>
          90%
        </MenuItem>
        <MenuItem onClick={clickFun} active={false}>
          100%
        </MenuItem>
      </DropdownButton>
    )
    expect(actual).toEqual(expected)
  })
})
