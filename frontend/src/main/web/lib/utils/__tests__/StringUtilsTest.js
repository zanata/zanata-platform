jest.dontMock('../StringUtils')
  .dontMock('lodash');

describe('StringUtilsTest', function() {
  var StringUtils;

  beforeEach(function() {
    StringUtils = require('../StringUtils');
  });

  it('test empty, null and undefined value', function() {
    var value = '';
    expect(StringUtils.isEmptyOrNull(value)).toEqual(true);

    value = null;
    expect(StringUtils.isEmptyOrNull(value)).toEqual(true);

    value = undefined;
    expect(StringUtils.isEmptyOrNull(value)).toEqual(true);
  });

  it('test not empty value', function() {
    var value = '123';
    expect(StringUtils.isEmptyOrNull(value)).toEqual(false);
  });

  it('test trim leading space', function() {
    var value = '   123';
    expect(StringUtils.trimLeadingSpace(value)).toEqual('123');

    value = '123';
    expect(StringUtils.trimLeadingSpace(value)).toEqual(value);

    value = null;
    expect(StringUtils.trimLeadingSpace(value)).toEqual(value);
  });

  it('test trim white space', function() {
    var value = ' 123 ';
    expect(StringUtils.trim(value)).toEqual('123');

    value = '123';
    expect(StringUtils.trim(value)).toEqual(value);

    value = null;
    expect(StringUtils.trim(value)).toEqual(value);
  });
});
