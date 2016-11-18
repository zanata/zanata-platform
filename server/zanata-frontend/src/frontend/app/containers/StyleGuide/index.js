import React, { Component } from 'react'
import { Modal, EditableText, Icon, Link,
  LoaderText, TextInput } from '../../components'
import { Button, Row, Table, ButtonToolbar,
  OverlayTrigger, Tooltip, Grid, Col } from 'react-bootstrap'

const tooltip = (
  <Tooltip id='tooltip'><strong>Tooltip ahoy!</strong> Check this info.
  </Tooltip>
)

const wellStyles = {maxWidth: '400', margin: '0 auto 10px'}

class StyleGuide extends Component {

  constructor () {
    super()
    this.state = {
      show: false
    }
  }

  hideModal () {
    this.setState({show: false})
  }

  showModal () {
    this.setState({show: true})
  }
  /* eslint-disable react/jsx-no-bind */
  render () {
    return (

      <div className='container'>
        <h1>STYLES</h1>
        <span>
          <h2>Grid</h2>
          <Grid>
            <Row className='show-grid'>
              <Col className='show-grid' xs={12} md={8}><code>
                &lt;{'Col xs={12} md={8}'} /&gt;</code></Col>
              <Col className='show-grid' xs={6} md={4}><code>
                &lt;{'Col xs={6} md={4}'} /&gt;</code></Col>
            </Row>
            <Row className='show-grid'>
              <Col className='show-grid' xs={6} md={4}><code>
                &lt;{'Col xs={6} md={4}'} /&gt;</code></Col>
              <Col className='show-grid' xs={6} md={4}><code>
                &lt;{'Col xs={6} md={4}'} /&gt;</code></Col>
              <Col className='show-grid' xsHidden md={4}><code>
                &lt;{'Col xsHidden md={4}'} /&gt;</code></Col>
            </Row>
            <Row className='show-grid'>
              <Col className='show-grid' xs={6} xsOffset={6}><code>
                &lt;{'Col xs={6} xsOffset={6}'} /&gt;</code></Col>
            </Row>
            <Row className='show-grid'>
              <Col className='show-grid' md={6} mdPush={6}><code>
                &lt;{'Col md={6} mdPush={6}'} /&gt;</code></Col>
              <Col className='show-grid' md={6} mdPull={6}><code>
                &lt;{'Col md={6} mdPull={6}'} /&gt;</code></Col>
            </Row>
          </Grid>
        </span>
        <span>
          <h2>Main Colors</h2>
          <div className='sg-color sg-brand-primary sg-lg'>
            <span className='sg-color-swatch'>
              <span className='sg-animated'>#03A6D7</span></span></div>
          <div className='sg-color sg-gray-lighter sg-lg'>
            <span className='sg-color-swatch'>
              <span className='sg-animated'>#546677</span></span></div>
          <div className='sg-color sg-gray-light sg-lg'>
            <span className='sg-color-swatch'>
              <span className='sg-animated'>#629BAC</span></span></div>
          <div className='sg-color sg-gray sg-lg'>
            <span className='sg-color-swatch'>
              <span className='sg-animated'>#FCFCFC</span></span></div>
          <div className='sg-color sg-gray-dark sg-lg'>
            <span className='sg-color-swatch'>
              <span className='sg-animated'>#333333</span></span></div>
          <div className='sg-color sg-gray-darker sg-lg'>
            <span className='sg-color-swatch'>
              <span className='sg-animated'>#DDDDDD</span></span></div>
          <h2>Status colours</h2>
          <div className='sg-color sg-brand-success'>
            <span className='sg-color-swatch'>
              <span className='sg-animated'>#62C876</span></span></div>
          <div className='sg-color sg-brand-unsure'>
            <span className='sg-color-swatch'>
              <span className='sg-animated'>#E9DD00</span></span></div>
          <div className='sg-color sg-brand-warning'>
            <span className='sg-color-swatch'>
              <span className='sg-animated'>#FFA800</span></span></div>
          <div className='sg-color sg-brand-danger'>
            <span className='sg-color-swatch'>
              <span className='sg-animated'>#FF3B3D</span></span></div>
          <div className='sg-color sg-brand-info'>
            <span className='sg-color-swatch'>
              <span className='sg-animated'>#4E9FDD</span></span></div>
        </span>
        <span>
          <h2>Font stack</h2>
          <p>'Source Sans Pro', 'Helvetica Neue',
          Helvetica, Arial, sans-serif;</p>
        </span>
        <h1>COMPONENTS</h1>
        <span className='list-inline'>
          <h2>Buttons</h2>
          <Button bsStyle='default'>Default</Button>
          <Button bsStyle='primary'>Primary</Button>
          <Button bsStyle='success'>Success</Button>
          <Button bsStyle='warning'>Warning</Button>
          <Button bsStyle='danger'>Danger</Button>
          <Button bsStyle='info'>Info</Button>
          <Button bsStyle='link'>Link</Button>

          <ButtonToolbar>
            <Button bsStyle='primary' bsSize='large'>
            Large button</Button>
            <Button bsSize='large'>Large button</Button>
          </ButtonToolbar>
          <ButtonToolbar>
            <Button bsStyle='primary'>Default button</Button>
            <Button>Default button</Button>
          </ButtonToolbar>
          <ButtonToolbar>
            <Button bsStyle='primary' bsSize='small'>
            Small button</Button>
            <Button bsSize='small'>Small button</Button>
          </ButtonToolbar>
          <ButtonToolbar>
            <Button bsStyle='primary' bsSize='xsmall'>
            Extra small button</Button>
            <Button bsSize='xsmall'>Extra small button</Button>
          </ButtonToolbar>
          <span className='well' style={wellStyles}>
            <Button bsStyle='primary' bsSize='large' block>
            Block level button</Button>
            <Button bsSize='large' block>Block level button</Button>
          </span>
        </span>
        <br />
        <span>
          <h2>EditableText</h2>
          <EditableText
            className='editable'
            maxLength={255}
            placeholder='Add a description…'
            emptyReadOnlyText='No description' >
            'sting'
          </EditableText>
        </span>
        <span>
          <h2>Headings</h2>
          <span className='form-inline'>
            <h1>Heading 1 - H1</h1>
            <h2>Heading 2 - H2</h2>
            <h3>Heading 3 - H3</h3>
            <h4>Heading 4 - H4</h4>
            <h5>Heading 5 - H5</h5>
            <h6>Heading 6 - H6</h6>
          </span>
        </span>
        <span>
          <h2>Icons</h2>
          <ul>
            <li><Icon name='admin' className='s0' />admin</li>
            <li><Icon name='all' className='s0' />all</li>
            <li><Icon name='assign' className='s0' />assign</li>
            <li><Icon name='attach' className='s0' />attach</li>
            <li><Icon name='block' className='s0' />block</li>
            <li><Icon name='chevron-down-double'
              className='s0' />chevron-double-down</li>
            <li><Icon name='chevron-down' className='s0' />chevron-down</li>
            <li><Icon name='chevron-left' className='s0' />chevron-left</li>
            <li><Icon name='chevron-right' className='s0' />
            chevron-right</li>
            <li><Icon name='chevron-up-double'
              className='s0' />chevron-up-double</li>
            <li><Icon name='chevron-up' className='s0' />chevron-up</li>
            <li><Icon name='circle' className='s0' />circle</li>
            <li><Icon name='clock' className='s0' />clock</li>
            <li><Icon name='code' className='s0' />code</li>
            <li><Icon name='comment' className='s0' />comment</li>
            <li><Icon name='copy' className='s0' />copy</li>
            <li><Icon name='cross-circle' className='s0' />cross-circle</li>
            <li><Icon name='cross' className='s0' />cross</li>
            <li><Icon name='dashboard' className='s0' />dashboard</li>
            <li><Icon name='document' className='s0' />document</li>
            <li><Icon name='dot' className='s0' />dot</li>
            <li><Icon name='download' className='s0' />download</li>
            <li><Icon name='edit' className='s0' />edit</li>
            <li><Icon name='ellipsis' className='s0' />ellipsis</li>
            <li><Icon name='export' className='s0' />export</li>
            <li><Icon name='external-link'
              className='s0' />external-link</li>
            <li><Icon name='filter' className='s0' />filter</li>
            <li><Icon name='folder' className='s0' />folder</li>
            <li><Icon name='glossary' className='s0' />glossary</li>
            <li><Icon name='help' className='s0' />help</li>
            <li><Icon name='history' className='s0' />history</li>
            <li><Icon name='import' className='s0' />import</li>
            <li><Icon name='inbox' className='s0' />inbox</li>
            <li><Icon name='info' className='s0' />info</li>
            <li><Icon name='keyboard' className='s0' />keyboard</li>
            <li><Icon name='language' className='s0' />language</li>
            <li><Icon name='link' className='s0' />link</li>
            <li><Icon name='location' className='s0' />location</li>
            <li><Icon name='locked' className='s0' />locked</li>
            <li><Icon name='logout' className='s0' />logout</li>
            <li><Icon name='mail' className='s0' />mail</li>
            <li><Icon name='maintain' className='s0' />maintain</li>
            <li><Icon name='menu' className='s0' />menu</li>
            <li><Icon name='minus' className='s0' />minus</li>
            <li><Icon name='next' className='s0' />next</li>
            <li><Icon name='notification' className='s0' />notification</li>
            <li><Icon name='piestats' className='s0' />piestats</li>
            <li><Icon name='plus' className='s0' />plus</li>
            <li><Icon name='previous' className='s0' />previous</li>
            <li><Icon name='project' className='s0' />project</li>
            <li><Icon name='refresh' className='s0' />refresh</li>
            <li><Icon name='review' className='s0' />review</li>
            <li><Icon name='search' className='s0' />search</li>
            <li><Icon name='servmon' className='s0' />servmon</li>
            <li><Icon name='settings' className='s0' />settings</li>
            <li><Icon name='star-outline' className='s0' />star-outline</li>
            <li><Icon name='star' className='s0' />star</li>
            <li><Icon name='statistics' className='s0' />statistics</li>
            <li><Icon name='suggestions' className='s0' />suggestions</li>
            <li><Icon name='tick-circle' className='s0' />tick-circle</li>
            <li><Icon name='tick' className='s0' />tick</li>
            <li><Icon name='tm' className='s0' />tm</li>
            <li><Icon name='translate' className='s0' />translate</li>
            <li><Icon name='trash' className='s0' />trash</li>
            <li><Icon name='undo' className='s0' />undo</li>
            <li><Icon name='unlocked' className='s0' />unlocked</li>
            <li><Icon name='upload' className='s0' />upload</li>
            <li><Icon name='user' className='s0' />user</li>
            <li><Icon name='users' className='s0' />users</li>
            <li><Icon name='version' className='s0' />version</li>
            <li><Icon name='warning' className='s0' />warning</li>
            <li><Icon name='zanata' className='s0' />zanata</li>
          </ul>
        </span>
        <span>
          <h2>Link</h2>
          <Link link='www.google.com'>link</Link>
        </span>
        <span>
          <h2>Loader</h2>
          <h3>LoaderText</h3>
          <LoaderText loading loadingText='Loading' />
        </span>
        <span>
          <h2>Modal</h2>
          <Button bsStyle='default'
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
        </span>
        <span>
          <h2>Row</h2>
          <Row>
            <Icon name='star' className='s1' />This is a row
          </Row>
        </span>
        <span>
          <h2>Table</h2>
          <Table striped bordered condensed hover>
            <thead>
              <tr>
                <th>#</th>
                <th>First Name</th>
                <th>Last Name</th>
                <th>Username</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>1</td>
                <td>Mark</td>
                <td>Otto</td>
                <td>@mdo</td>
              </tr>
              <tr>
                <td>2</td>
                <td>Jacob</td>
                <td>Thornton</td>
                <td>@fat</td>
              </tr>
              <tr>
                <td>3</td>
                <td colSpan='2'>Larry the Bird</td>
                <td>@twitter</td>
              </tr>
            </tbody>
          </Table>
        </span>
        <span>
          <h2>TextInput</h2>
          <TextInput
            maxLength={100}
            id='demo'
            className='textInput'
            placeholder='TextInput…'
            accessibilityLabel='TextInput'
            defaultValue='Default text'
            onKeyDown={(e) => { this.handleKeyDown(e) }}
          />
        </span>
        <span>
          <h2>Tooltip</h2>
          <ButtonToolbar>
            <OverlayTrigger placement='left' overlay={tooltip}>
              <Button bsStyle='default'>Holy guacamole!</Button>
            </OverlayTrigger>
            <OverlayTrigger placement='top' overlay={tooltip}>
              <Button bsStyle='default'>Holy guacamole!</Button>
            </OverlayTrigger>
            <OverlayTrigger placement='bottom' overlay={tooltip}>
              <Button bsStyle='default'>Holy guacamole!</Button>
            </OverlayTrigger>
            <OverlayTrigger placement='right' overlay={tooltip}>
              <Button bsStyle='default'>Holy guacamole!</Button>
            </OverlayTrigger>
          </ButtonToolbar>
        </span>
        <span>
          <h2>Removed components</h2>
          <ul>
            <li>Base</li>
            <li>Flex</li>
            <li>Page</li>
            <li>Scrollview</li>
            <li>View</li>
          </ul>
        </span>
      </div>)
  }
  /* eslint-enable react/jsx-no-bind */
}

export default StyleGuide
