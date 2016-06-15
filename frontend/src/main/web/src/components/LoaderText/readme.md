Allows the use of a loader that includes text. Can be toggled between loading
or not loading. Good for loading state in buttons.

## Default

    function handleClick() {
      setState({loading: !state.loading})
    }
    <LoaderText loading={state.loading} onClick={handleClick}>
      I will toggle loading when clicked
    </LoaderText>

## Size

    function handleClick() {
      setState({loading: !state.loading})
    }
    <LoaderText atomic={{fz: 'Fz(ms3)'}}
      loading={state.loading}
      onClick={handleClick} size='3'>
      I will toggle loading when clicked but am larger
    </LoaderText>

## Custom Loading Text

    function handleClick() {
      setState({loading: true})
      setTimeout(() => {
        setState({loading: false})
      }, 1000)
    }
    <ButtonRound onClick={handleClick} disabled={state.loading}>
      <LoaderText loading={state.loading}
        loadingText='Submitting'>
        Submit
      </LoaderText>
    </ButtonRound>
