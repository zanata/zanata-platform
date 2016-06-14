## Default

    function showModal () {
      console.log('Clicked: ', state)
      setState({isModal: true})
    }
    function hideModal () {
      console.log('Hide: ', state)
      setState({isModal: false})
    }

    <div>
      <ButtonRound onClick={showModal}>Launch Modal</ButtonRound>
      <Modal
        show={state.isModal}
        onHide={hideModal}>
        <Modal.Header>
          <Modal.Title>Example Modal</Modal.Title>
        </Modal.Header>
        <Modal.Body>Hi There</Modal.Body>
        <Modal.Footer>
          <ButtonLink onClick={hideModal}>Cancel</ButtonLink>
          <ButtonRound onClick={hideModal} atomic={{m: 'Mstart(r1)'}}>
            Submit
          </ButtonRound>
        </Modal.Footer>
      </Modal>
    </div>
