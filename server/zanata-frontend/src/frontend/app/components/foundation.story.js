/**
 * Foundational stories, not demonstrating any component in particular.
 */

import React from 'react'
import { storiesOf } from '@storybook/react'
import { Grid, Row, Col } from 'react-bootstrap'

storiesOf('Foundation', module)
    .add('grid', () => (
        <Grid>
          <Row className='showGrid'>
            <Col className='showGrid' xs={12} md={8}><code>
              &lt;{'Col xs={12} md={8}'} /&gt;</code></Col>
            <Col className='showGrid' xs={6} md={4}><code>
              &lt;{'Col xs={6} md={4}'} /&gt;</code></Col>
          </Row>
          <Row className='showGrid'>
            <Col className='showGrid' xs={6} md={4}><code>
              &lt;{'Col xs={6} md={4}'} /&gt;</code></Col>
            <Col className='showGrid' xs={6} md={4}><code>
              &lt;{'Col xs={6} md={4}'} /&gt;</code></Col>
            <Col className='showGrid' xsHidden md={4}><code>
              &lt;{'Col xsHidden md={4}'} /&gt;</code></Col>
          </Row>
          <Row className='showGrid'>
            <Col className='showGrid' xs={6} xsOffset={6}><code>
              &lt;{'Col xs={6} xsOffset={6}'} /&gt;</code></Col>
          </Row>
          <Row className='showGrid'>
            <Col className='showGrid' md={6} mdPush={6}><code>
              &lt;{'Col md={6} mdPush={6}'} /&gt;</code></Col>
            <Col className='showGrid' md={6} mdPull={6}><code>
              &lt;{'Col md={6} mdPull={6}'} /&gt;</code></Col>
          </Row>
        </Grid>
    ))
    .add('colours', () => (
        <span>
          <h3>Main colors</h3>
          <small>Hover for hexcode</small><br />
          <div className='sg-color sg-brand-primary sg-lg'>
            <span className='sg-color-swatch'>
              <span className='sg-animated'>#03A6D7</span></span></div>
          <div className='sg-color sg-gray-lighter sg-lg'>
            <span className='sg-color-swatch'>
              <span className='sg-animated'>#546677</span></span></div>
          <div className='sg-color sg-gray-light sg-lg'>
            <span className='sg-color-swatch'>
              <span className='sg-animated'>#629BAC</span></span></div>
            <div className='sg-color sg-muted sg-lg'>
            <span className='sg-color-swatch'>
              <span className='sg-animated'>#A2B3BE</span></span></div>
            <div className='sg-color sg-neutral sg-lg'>
            <span className='sg-color-swatch'>
              <span className='sg-animated'>#BDD4DC</span></span></div>
          <div className='sg-color sg-gray-dark sg-lg'>
            <span className='sg-color-swatch'>
              <span className='sg-animated'>#F5F5F5</span></span></div>
          <div className='sg-color sg-gray-darker sg-lg'>
            <span className='sg-color-swatch'>
              <span className='sg-animated'>#DDDDDD</span></span></div>
          <div className='sg-color sg-gray-darkest sg-lg'>
            <span className='sg-color-swatch'>
              <span className='sg-animated'>#555555</span></span></div>
          <h3>Status colours</h3>
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
    ))
    .add('typography', () => (
        <span>
          <h3>Font stack</h3>
          <p>'Source Sans Pro', 'Helvetica Neue',
          Helvetica, Arial, sans-serif;</p>
          <hr />
        <h3>Headings</h3>
          <h1 className='page-header'>Page Header <small>
          With Small Text</small></h1>
          <h1>h1. Bootstrap heading <small>Secondary text</small></h1>
          <h2>h2. Bootstrap heading <small>Secondary text</small></h2>
          <h3>h3. Bootstrap heading <small>Secondary text</small></h3>
          <h4>h4. Bootstrap heading <small>Secondary text</small></h4>
          <h5>h5. Bootstrap heading <small>Secondary text</small></h5>
          <h6>h6. Bootstrap heading <small>Secondary text</small></h6>
          <hr />
        <h3>Text styles</h3>
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
        <p className='u-textLeft'>Left aligned text.</p>
        <p className='u-textCenter'>Center aligned text.</p>
        <p className='u-textRight'>Right aligned text.</p>
        <p className='u-textJustify'>Justified text.</p>
        <p className='u-textMuted'>Muted: Fusce dapibus,
        tellus ac cursus commodo,
        tortor mauris nibh.</p>
        <p className='u-textPrimary'>Primary: Nullam id dolor id nibh
        ultricies vehicula ut id elit.</p>
        <p className='u-textWarning'>Warning: Etiam porta sem malesuada
        magna mollis euismod.</p>
        <p className='u-textDanger'>Danger: Donec ullamcorper nulla non
        metus auctor fringilla.</p>
        <p className='u-textSuccess'>Success: Duis mollis, est non commodo
        luctus, nisi erat porttitor ligula.</p>
        <p className='u-textInfo'>Info: Maecenas sed diam eget risus varius
        blandit sit amet non magna.</p>
      </span>
    ))


