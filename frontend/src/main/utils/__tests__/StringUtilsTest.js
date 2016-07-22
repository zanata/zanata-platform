/* eslint-disable no-undef */
jest.unmock('../StringUtils')
import StringUtils from '../StringUtils'

describe('StringUtilsTest', function () {
  it('test trim leading space', function () {
    var value = '   123'
    expect(StringUtils.trimLeadingSpace(value)).toEqual('123')

    value = '123'
    expect(StringUtils.trimLeadingSpace(value)).toEqual(value)

    value = '123  '
    expect(StringUtils.trimLeadingSpace(value)).toEqual(value)

    value = null
    expect(StringUtils.trimLeadingSpace(value)).toEqual(value)
  })

  it('test trim white space', function () {
    var value = ' 123 '
    expect(StringUtils.trim(value)).toEqual('123')

    value = '123'
    expect(StringUtils.trim(value)).toEqual(value)

    value = null
    expect(StringUtils.trim(value)).toEqual(value)
  })
})

/* eslint-enable */
