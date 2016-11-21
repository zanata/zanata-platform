import React, {PropTypes, Component} from 'react'
import {
  Modal, EditableText, Icon, Link,
  LoaderText, TextInput
} from '../../components'
import {
  Alert, Button, Row, Table, ButtonToolbar, Tabs,
  Tab, OverlayTrigger, Tooltip, Grid, Col,
  Badge, Nav, NavItem, ControlLabel, Pagination,
  FormGroup, FormControl, Form, InputGroup,
  Checkbox, Radio, Label, ListGroup, Panel,
  ListGroupItem, ProgressBar, Well, Breadcrumb,
  ButtonGroup, MenuItem, DropdownButton
} from 'react-bootstrap'

const tooltip = (
  <Tooltip id='tooltip'><strong>Tooltip ahoy!</strong> Check this info.
  </Tooltip>
)

const now = 60

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

  handleSelect (eventKey) {
    this.setState({
      activePage: eventKey
    })
  }
  /* eslint-disable react/jsx-no-bind */
  render () {
    const {
      page
    } = this.props
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
        <span>
          <h2>Alerts</h2>
          <Alert bsStyle='success'>
            <strong>Holy guacamole!</strong> Best check yo self,
            you're not looking too good.
          </Alert>
          <Alert bsStyle='warning'>
            <strong>Holy guacamole!</strong> Best check yo self,
            you're not looking too good.
          </Alert>
          <Alert bsStyle='danger'>
            <strong>Holy guacamole!</strong> Best check yo self,
            you're not looking too good.
          </Alert>
          <Alert bsStyle='info'>
            <strong>Holy guacamole!</strong> Best check yo self,
            you're not looking too good.
          </Alert>
        </span>
        <span>
          <h2>Badges</h2>
          <p>Badge <Badge>23</Badge></p>
          <Nav bsStyle='pills' stacked className='sg-nav-pills'>
            <NavItem className='active'>Home
              &nbsp;<Badge>42</Badge></NavItem>
            <NavItem>Profile</NavItem>
            <NavItem>Messages <Badge>3</Badge></NavItem>
          </Nav>
          <Nav bsStyle='pills' stacked className='sg-nav-pills'>
            <NavItem className='active'>
              <Badge className='pull-right'>42</Badge>
                Home
            </NavItem>
            <NavItem>Profile</NavItem>
            <NavItem>
              <Badge className='pull-right'>3</Badge>
                Messages
            </NavItem>
          </Nav>
          <Button bsStyle='primary' type='button'>
            Messages <Badge>4</Badge>
          </Button>
        </span>
        <span>
          <h2>Breadcrumbs</h2>
          <Breadcrumb>
            <Breadcrumb.Item href='#'>
              Home
            </Breadcrumb.Item>
            <Breadcrumb.Item href='#'>
              Library
            </Breadcrumb.Item>
            <Breadcrumb.Item active>
              Data
            </Breadcrumb.Item>
          </Breadcrumb>
        </span>
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
          <span>
            <Button bsStyle='primary' bsSize='large' block>
            Block level button</Button>
            <Button bsSize='large' block>Block level button</Button>
          </span>
          <h3>Button groups</h3>
          <ButtonGroup>
            <Button>Left</Button>
            <Button>Middle</Button>
            <Button>Right</Button>
          </ButtonGroup>
          <h3>Button toolbar</h3>
          <ButtonToolbar>
            <ButtonGroup>
              <Button>1</Button>
              <Button>2</Button>
              <Button>3</Button>
              <Button>4</Button>
            </ButtonGroup>
            <ButtonGroup>
              <Button>5</Button>
              <Button>6</Button>
              <Button>7</Button>
            </ButtonGroup>
            <ButtonGroup>
              <Button>8</Button>
            </ButtonGroup>
          </ButtonToolbar>
        </span>
        <br />
        <span>
          <h2>Code</h2>
          <pre>&lt;p&gt;A block of code is wrapped in pre tags&lt;/p&gt;</pre>
          <p>For example, <code>&lt;section&gt;</code>
          should be wrapped as inline.</p>
          <p>To switch directories, type <kbd>cd</kbd>
          followed by the name of the directory.</p>
        </span>
        <span>
          <h2>Dropdowns</h2>
          <ButtonToolbar>
            <DropdownButton title='Default button' className='dropdown-toggle'
              id='dropdown-size-medium'>
              <MenuItem eventKey='1'>Action</MenuItem>
              <MenuItem eventKey='2'>Another action</MenuItem>
              <MenuItem eventKey='3'>Something else here</MenuItem>
              <MenuItem divider />
              <MenuItem eventKey='4'>Separated link</MenuItem>
            </DropdownButton>
          </ButtonToolbar>
        </span>
        <span>
          <h2>Forms</h2>
          <Form>
            <FormGroup>
              <FormControl type='text' placeholder='Text' />
              <FormControl type='text' disabled placeholder='disabled' />
            </FormGroup>
            <FormGroup bsSize='lg'>
              <FormControl type='text' placeholder='large' />
            </FormGroup>
            <FormGroup bsSize='sm'>
              <FormControl type='text' placeholder='small' />
            </FormGroup>
          </Form>
          <Form>
            <FormGroup controlId='formValidationSuccess1'
              validationState='success'>
              <ControlLabel>Input with success</ControlLabel>
              <FormControl type='text' />
            </FormGroup>
            <FormGroup controlId='formValidationWarning1'
              validationState='warning'>
              <ControlLabel>Input with warning</ControlLabel>
              <FormControl type='text' />
            </FormGroup>
            <FormGroup controlId='formValidationError1' validationState='error'>
              <ControlLabel>Input with error</ControlLabel>
              <FormControl type='text' />
            </FormGroup>
            <FormGroup controlId='formValidationWarning3'
              validationState='warning'>
              <ControlLabel>Input group with warning</ControlLabel>
              <InputGroup>
                <InputGroup.Addon>@</InputGroup.Addon>
                <FormControl type='text' />
              </InputGroup>
              <FormControl.Feedback />
            </FormGroup>
          </Form>
          <Form componentClass='fieldset' horizontal>
            <FormGroup controlId='formValidationError3' validationState='error'>
              <Col componentClass={ControlLabel} xs={3}>
                Input with error
              </Col>
              <Col xs={9}>
                <FormControl type='text' />
                <FormControl.Feedback />
              </Col>
            </FormGroup>
            <FormGroup controlId='formValidationSuccess4'
              validationState='success'>
              <Col componentClass={ControlLabel} xs={3}>
                Input group with success
              </Col>
              <Col xs={9}>
                <InputGroup>
                  <InputGroup.Addon>@</InputGroup.Addon>
                  <FormControl type='text' />
                </InputGroup>
                <FormControl.Feedback />
              </Col>
            </FormGroup>
          </Form>
          <Form componentClass='fieldset' inline>
            <FormGroup controlId='formValidationWarning4'
              validationState='warning'>
              <ControlLabel>Input with warning</ControlLabel>
               {' '}
              <FormControl type='text' />
              <FormControl.Feedback />
            </FormGroup>
          {' '}
            <FormGroup controlId='formValidationError4' validationState='error'>
              <ControlLabel>Input group with error</ControlLabel>
          {' '}
              <InputGroup>
                <InputGroup.Addon>@</InputGroup.Addon>
                <FormControl type='text' />
              </InputGroup>
              <FormControl.Feedback />
            </FormGroup>
          </Form>
          <Checkbox validationState='success'>
            Checkbox with success
          </Checkbox>
          <Radio validationState='warning'>
            Radio with warning
          </Radio>
          <Checkbox validationState='error'>
            Checkbox with error
          </Checkbox>
        </span>
        <span>
          <h2>EditableText</h2>
          <EditableText
            className='editable'
            maxLength={255}
            placeholder='Add a description…'
            emptyReadOnlyText='No description'>
            'sting'
          </EditableText>
        </span>
        <span>
          <h2>Headings</h2>
          <h1 className='page-header'>Page Header <small>
          With Small Text</small></h1>
          <h1>h1. Bootstrap heading <small>Secondary text</small></h1>
          <h2>h2. Bootstrap heading <small>Secondary text</small></h2>
          <h3>h3. Bootstrap heading <small>Secondary text</small></h3>
          <h4>h4. Bootstrap heading <small>Secondary text</small></h4>
          <h5>h5. Bootstrap heading <small>Secondary text</small></h5>
          <h6>h6. Bootstrap heading <small>Secondary text</small></h6>
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
            <li><Icon name='notification' className='s0' />
            notification</li>
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
          <h2>Labels</h2>
          <h1>Label <Label>New</Label></h1>
          <h2>Label <Label>New</Label></h2>
          <h3>Label <Label>New</Label></h3>
          <h4>Label <Label>New</Label></h4>
          <h5>Label <Label>New</Label></h5>
          <p>Label <Label>New</Label></p>
          <Label bsStyle='default'>Default</Label>&nbsp;
          <Label bsStyle='primary'>Primary</Label>&nbsp;
          <Label bsStyle='success'>Success</Label>&nbsp;
          <Label bsStyle='info'>Info</Label>&nbsp;
          <Label bsStyle='warning'>Warning</Label>&nbsp;
          <Label bsStyle='danger'>Danger</Label>
        </span>
        <span>
          <h2>Link</h2>
          <Link link='www.google.com'>link</Link>
        </span>
        <span>
          <h2>Lists</h2>
          <ListGroup>
            <ListGroupItem>Item 1</ListGroupItem>
            <ListGroupItem>Item 2</ListGroupItem>
            <ListGroupItem>...</ListGroupItem>
          </ListGroup>
          <ListGroup>
            <ListGroupItem href='#' active>Active</ListGroupItem>
            <ListGroupItem href='#'>Link</ListGroupItem>
            <ListGroupItem href='#' disabled>Disabled</ListGroupItem>
          </ListGroup>
          <ListGroup>
            <ListGroupItem header='Heading 1'>
            Some body text</ListGroupItem>
            <ListGroupItem header='Heading 2' href='#'>
            Linked item</ListGroupItem>
          </ListGroup>
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
          <h2>Pagination</h2>
          <Pagination
            bsSize='large'
            items={10}
            activePage={page}
            onSelect={this.handleSelect} />
          <br />
          <Pagination
            bsSize='medium'
            items={10}
            activePage={page}
            onSelect={this.handleSelect} />
          <br />
          <Pagination
            bsSize='small'
            items={10}
            activePage={page}
            onSelect={this.handleSelect} />
        </span>
        <span>
          <h2>Panels</h2>
          <Panel>
          Basic panel example
          </Panel>
          <Panel header='Panel header'>
          Panel content
          </Panel>
          <Panel header='Panel header' bsStyle='primary'>
          Panel content
          </Panel>
          <Panel header='Panel header' bsStyle='success'>
          Panel content
          </Panel>
          <Panel header='Panel header' bsStyle='info'>
          Panel content
          </Panel>
          <Panel header='Panel header' bsStyle='warning'>
          Panel content
          </Panel>
          <Panel header='Panel header' bsStyle='danger'>
          Panel content
          </Panel>
        </span>
        <span>
          <h2>Progress bars</h2>
          <h3>Basic</h3>
          <ProgressBar now={now} label={`${now}%`} />
          <ProgressBar now={60} />
          <h3>Contextual alternatives</h3>
          <ProgressBar bsStyle='success' now={40} />
          <ProgressBar bsStyle='info' now={20} />
          <ProgressBar bsStyle='warning' now={60} />
          <ProgressBar bsStyle='danger' now={80} />
          <h3>Stacked</h3>
          <ProgressBar>
            <ProgressBar striped bsStyle='success' now={35} key={1} />
            <ProgressBar bsStyle='warning' now={20} key={2} />
            <ProgressBar active bsStyle='danger' now={10} key={3} />
          </ProgressBar>
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
          <h2>Tabs</h2>
          <Tabs defaultActiveKey={2} id='uncontrolled-tab-example'>
            <Tab eventKey={1} title='Tab 1'>Tab 1 content</Tab>
            <Tab eventKey={2} title='Tab 2'>Tab 2 content</Tab>
            <Tab eventKey={3} title='Tab 3' disabled>Tab 3 content</Tab>
          </Tabs>
          <Tab.Container id='left-tabs-example' defaultActiveKey='first'>
            <Row className='clearfix'>
              <Col sm={4}>
                <Nav bsStyle='pills' stacked>
                  <NavItem eventKey='first'>
                    Tab 1
                  </NavItem>
                  <NavItem eventKey='second'>
                    Tab 2
                  </NavItem>
                </Nav>
              </Col>
              <Col sm={8}>
                <Tab.Content animation>
                  <Tab.Pane eventKey='first'>
                    Tab 1 content
                  </Tab.Pane>
                  <Tab.Pane eventKey='second'>
                    Tab 2 content
                  </Tab.Pane>
                </Tab.Content>
              </Col>
            </Row>
          </Tab.Container>
        </span>
        <span>
          <h2>Text Styles</h2>
          <h3>Example body text</h3>
          <p className='lead'>Lead paragraph: vivamus sagittis lacus
            vel augue laoreet rutrum faucibus dolor auctor. Duis
          mollis, est non commodo luctus.</p>
          <p>Nullam quis risus eget <a href='#'>urna mollis ornare</a>
            vel eu leo. Cum sociis natoque penatibus et magnis dis
            parturient montes, nascetur ridiculus mus. Nullam
          id dolor id nibh ultricies vehicula.</p>
          <p><small>This line of text is meant to be treated as fine print.
          </small></p>
          <p>The following snippet of text is <strong>rendered as
          bold text</strong>.</p>
          <p>The following snippet of text is <em>rendered as
          italicized text</em>.</p>
          <p>An abbreviation of the word attribute is
            <abbr title='attribute'>attr</abbr>.</p>
          <p className='text-left'>Left aligned text.</p>
          <p className='text-center'>Center aligned text.</p>
          <p className='text-right'>Right aligned text.</p>
          <p className='text-justify'>Justified text.</p>
          <p className='text-muted'>Muted: Fusce dapibus,
            tellus ac cursus commodo,
          tortor mauris nibh.</p>
          <p className='text-primary'>Primary: Nullam id dolor id nibh
          ultricies vehicula ut id elit.</p>
          <p className='text-warning'>Warning: Etiam porta sem malesuada
          magna mollis euismod.</p>
          <p className='text-danger'>Danger: Donec ullamcorper nulla non
          metus auctor fringilla.</p>
          <p className='text-success'>Success: Duis mollis, est non commodo
          luctus, nisi erat porttitor ligula.</p>
          <p className='text-info'>Info: Maecenas sed diam eget risus varius
          blandit sit amet non magna.</p>
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
            onKeyDown={(e) => {
              this.handleKeyDown(e)
            }}
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
          <h2>Wells</h2>
          <Well>Look I'm in a well!</Well>
          <Well bsSize='large'>Look I'm in a large well!</Well>
          <Well bsSize='small'>Look I'm in a small well!</Well>
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

StyleGuide.propTypes = {
  page: PropTypes.number
}

export default StyleGuide
