Based on [React Select](https://github.com/JedWatson/react-select), this just
adds custom styling.
See [React Select Docs](https://github.com/JedWatson/react-select) for available
option.

To adjust the size, you can change the font-size using className,
e.g. `className='Fz(ms2)'`.

    var options = [
      { value: 'one', label: 'One' },
      { value: 'two', label: 'Two' },
      { value: 'three', label: 'Three' },
      { value: 'four', label: 'Four' },
      { value: 'five', label: 'Five' },
      { value: 'six', label: 'Six' }
    ];

    function handleChange(val) {
      setState({value: val})
    }

    <Select
      name='form-field-name'
      value={state.value}
      placeholder='Select a value'
      options={options}
      onChange={handleChange}
    />
