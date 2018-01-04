import React from 'react'
import { storiesOf } from '@storybook/react'
import { Panel, Well, Accordion } from 'react-bootstrap'

storiesOf('Panel', module)
    .add('default', () => (
        <span>
        <h2><img src="https://react-bootstrap.github.io/assets/logo.png" width="42px" />Panel</h2>

        <Well>By default, all the <code>Panel</code> does is apply some basic border and padding to contain some content.
You can pass on any additional properties you need, e.g. a custom onClick handler, as it is shown in the example code. They all will apply to the wrapper div element.
          <ul><li><a href="https://react-bootstrap.github.io/components.html#panels-props">Props for react-bootstrap Panel</a></li></ul></Well>

        <Panel>
          Basic panel example
        </Panel>
        </span>
    ))

    .add('with heading', () => (
        <span>
        <h2><img src="https://react-bootstrap.github.io/assets/logo.png" width="42px" />Panel</h2>

          <Well>Easily add a heading container to your panel with the header prop.</Well>
        <Panel header='Panel header'>
          Panel content
        </Panel>
        </span>
    ))
    .add('accordion', () => (
        <span>
          <h2><img src="https://react-bootstrap.github.io/assets/logo.png" width="42px" />Accordion</h2>

          <Well><code>Accordion</code> aliases <code>PanelGroup accordion</code>
                    <ul><li><a href="https://react-bootstrap.github.io/components.html#panels-props-group">Props for react-bootstrap PanelGroup and Accordion</a></li></ul></Well>

        <Accordion>
          <Panel header="Collapsible Group Item #1" eventKey="1">
            Anim pariatur cliche reprehenderit, enim eiusmod high life accusamus terry richardson ad squid. 3 wolf moon officia aute, non cupidatat skateboard dolor brunch. Food truck quinoa nesciunt laborum eiusmod. Brunch 3 wolf moon tempor, sunt aliqua put a bird on it squid single-origin coffee nulla assumenda shoreditch et. Nihil anim keffiyeh helvetica, craft beer labore wes anderson cred nesciunt sapiente ea proident. Ad vegan excepteur butcher vice lomo. Leggings occaecat craft beer farm-to-table, raw denim aesthetic synth nesciunt you probably haven't heard of them accusamus labore sustainable VHS.
          </Panel>
          <Panel header="Collapsible Group Item #2" eventKey="2">
            Anim pariatur cliche reprehenderit, enim eiusmod high life accusamus terry richardson ad squid. 3 wolf moon officia aute, non cupidatat skateboard dolor brunch. Food truck quinoa nesciunt laborum eiusmod. Brunch 3 wolf moon tempor, sunt aliqua put a bird on it squid single-origin coffee nulla assumenda shoreditch et. Nihil anim keffiyeh helvetica, craft beer labore wes anderson cred nesciunt sapiente ea proident. Ad vegan excepteur butcher vice lomo. Leggings occaecat craft beer farm-to-table, raw denim aesthetic synth nesciunt you probably haven't heard of them accusamus labore sustainable VHS.
          </Panel>
          <Panel header="Collapsible Group Item #3" eventKey="3">
            Anim pariatur cliche reprehenderit, enim eiusmod high life accusamus terry richardson ad squid. 3 wolf moon officia aute, non cupidatat skateboard dolor brunch. Food truck quinoa nesciunt laborum eiusmod. Brunch 3 wolf moon tempor, sunt aliqua put a bird on it squid single-origin coffee nulla assumenda shoreditch et. Nihil anim keffiyeh helvetica, craft beer labore wes anderson cred nesciunt sapiente ea proident. Ad vegan excepteur butcher vice lomo. Leggings occaecat craft beer farm-to-table, raw denim aesthetic synth nesciunt you probably haven't heard of them accusamus labore sustainable VHS.
          </Panel>
        </Accordion>
        </span>
    ))
    .add('primary', () => (
        <span>
                  <h2><img src="https://react-bootstrap.github.io/assets/logo.png" width="42px" />Panel - primary</h2>
        <Panel header='Panel header' bsStyle='primary'>
          Panel content
        </Panel>
          <hr />
        <p><code>bsStyle='primary'</code></p>
        </span>

))

    .add('success', () => (
        <span>
                            <h2><img src="https://react-bootstrap.github.io/assets/logo.png" width="42px" />Panel - success</h2>

        <Panel header='Panel header' bsStyle='success'>
          Panel content
        </Panel>
        <p><code>bsStyle='success'</code></p>
        </span>

))

    .add('info', () => (
        <span>
          <h2><img src="https://react-bootstrap.github.io/assets/logo.png" width="42px" />Panel - info</h2>

        <Panel header='Panel header' bsStyle='info'>
          Panel content
        </Panel>
        <p><code>bsStyle='info'</code></p>
        </span>
))

    .add('warning', () => (
        <span>
          <h2><img src="https://react-bootstrap.github.io/assets/logo.png" width="42px" />Panel - warning</h2>

        <Panel header='Panel header' bsStyle='warning'>
          Panel content
        </Panel>
        <p><code>bsStyle='warning'</code></p>
        </span>
    ))

    .add('danger', () => (
        <span>
          <h2><img src="https://react-bootstrap.github.io/assets/logo.png" width="42px" />Panel - danger</h2>

        <Panel header='Panel header' bsStyle='danger'>
          Panel content
        </Panel>
        <p><code>bsStyle='danger'</code></p>
        </span>
    ))
