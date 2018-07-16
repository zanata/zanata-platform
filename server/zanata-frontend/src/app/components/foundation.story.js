// @ts-nocheck
/**
 * Foundational stories, not demonstrating any component in particular.
 */

import React from 'react'
import { storiesOf } from '@storybook/react'

storiesOf('Foundation', module)
    .add('colours', () => (
        <span>
          <h2>Main colors</h2>
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
    ))
    .add('typography', () => (
        <span>
          <h2>Font stack</h2>
          <p><code>'Source Sans Pro', 'Helvetica Neue',
            Helvetica, Arial, sans-serif;</code></p>
        </span>
    ))
