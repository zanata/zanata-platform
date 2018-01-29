## Default

             <div>
              <Button bsStyle='info'
               onClick={() => this.showModal()}>Launch Modal</Button>
             <Modal
               show={this.state.show}
               onHide={() => this.hideModal()}>
               <Modal.Header>
                 <Modal.Title>Example Modal</Modal.Title>
               </Modal.Header>
               <Modal.Body>Hi There</Modal.Body>
               <Modal.Footer>
                 <Button bsStyle='link'
                   onClick={() => this.hideModal()}>Cancel</Button>
                 <Button bsStyle='primary' onClick={() => this.hideModal()}>
                 Submit
                 </Button>
               </Modal.Footer>
             </Modal>
             </div>
