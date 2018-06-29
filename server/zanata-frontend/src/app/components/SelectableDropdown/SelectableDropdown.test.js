
import React from 'react'
import * as ReactDOMServer from 'react-dom/server'
import SelectableDropdown from '.'
import {MenuItem, DropdownButton} from 'react-bootstrap'

const callback = () => {}

describe('SelectableDropdown', () => {
  it('can render SelectableDropdown markup', () => {
    // Function used in TMMergeModal for a percentage selection dropdown
    // @ts-ignore any
    const valueToDisplay = v => `The function says ${v}`
    const actual = ReactDOMServer.renderToStaticMarkup(
        <SelectableDropdown title={'TestDropdown'}
        id='dropdown-basic' classNameName='vmerge-ddown'
        onSelectDropdownItem={callback}
        selectedValue={'woof'}
        valueToDisplay={valueToDisplay}
        values={['moo', 'woof', 'meow']} />
    )
    const expected = ReactDOMServer.renderToStaticMarkup(
       <DropdownButton id={'dropdown-basic'} bsStyle={'default'}
        bsSize={'sm'} title={'TestDropdown'} className='bstrapReact'>
        <MenuItem onClick={callback} active={false}>
          The function says moo
        </MenuItem>
        <MenuItem onClick={callback} active>
          The function says woof
        </MenuItem>
        <MenuItem onClick={callback} active={false}>
          The function says meow
        </MenuItem>
      </DropdownButton>
    )
    expect(actual).toEqual(expected)
  })
})
