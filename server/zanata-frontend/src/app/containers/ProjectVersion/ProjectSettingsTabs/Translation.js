/* eslint-disable */
// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import {Icon} from '../../../components'
import Checkbox from 'antd/lib/checkbox'
import 'antd/lib/checkbox/style/css'
import Radio from 'antd/lib/radio'
import 'antd/lib/radio/style/css'
import Alert from 'antd/lib/alert'
import 'antd/lib/alert/style/css'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import Card from 'antd/lib/card'
import 'antd/lib/card/style/css'

class Translation extends Component {
  render() {
    return (
      /* eslint-disable max-len */
      <div className='flexTab wideView'>
        <h1 className='txt-info'>
          <Icon name='translate' className='s5 v-sub mr2' />
          Translation
        </h1>
        <Checkbox><span className='b'>Invite only</span></Checkbox>
        <p>Only allow translation by translators who have been explicitly added
          to this project. When this is turned off, translators from any of the
          global translation teams will be able to join translation of this
          project.</p>
        <h2>Validation</h2>
        <p>Use these validations to keep translations consistent with the
          source text.</p>
        <h3>HTML/XML tags</h3>
        <p>Check that XML/HTML tags are consistent. More…</p>
        <Radio.Group>
          <Radio.Button>Off</Radio.Button>
          <Radio.Button className='btn-warn'>Warning</Radio.Button>
          <Radio.Button className='btn-danger'>Error</Radio.Button>
        </Radio.Group>
        <h3>Java variables</h3>
        <p>Check that java style variables are consistent. More…</p>
        <Radio.Group>
          <Radio.Button>Off</Radio.Button>
          <Radio.Button className='btn-warn'>Warning</Radio.Button>
          <Radio.Button className='btn-danger'>Error</Radio.Button>
        </Radio.Group>
        <h3>Leading/trailing newline</h3>
        <p>Check for consistent leading and trailing newline. More…</p>
        <Radio.Group>
          <Radio.Button>Off</Radio.Button>
          <Radio.Button className='btn-warn'>Warning</Radio.Button>
          <Radio.Button className='btn-danger'>Error</Radio.Button>
        </Radio.Group>
        <h3>Positional printf (XSI extension)</h3>
        <p>Check that positional printf style (%n$x) variables are consistent.
          More…</p>
        <Radio.Group>
          <Radio.Button>Off</Radio.Button>
          <Radio.Button className='btn-warn'>Warning</Radio.Button>
          <Radio.Button className='btn-danger'>Error</Radio.Button>
        </Radio.Group>
        <h3>Printf variables</h3>
        <p>Check that printf style (%x) variables are consistent. More…</p>
        <Radio.Group>
          <Radio.Button>Off</Radio.Button>
          <Radio.Button className='btn-warn'>Warning</Radio.Button>
          <Radio.Button className='btn-danger'>Error</Radio.Button>
        </Radio.Group>
        <h3>Tab characters</h3>
        <p>Check whether source and target have the same number of tabs. More …
        </p>
        <Radio.Group>
          <Radio.Button>Off</Radio.Button>
          <Radio.Button className='btn-warn'>Warning</Radio.Button>
          <Radio.Button className='btn-danger'>Error</Radio.Button>
        </Radio.Group>
        <h3>XML entity reference</h3>
        <p>Check that XML entities are complete. More…</p>
        <Radio.Group>
          <Radio.Button>Off</Radio.Button>
          <Radio.Button className='btn-warn'>Warning</Radio.Button>
          <Radio.Button className='btn-danger'>Error</Radio.Button>
        </Radio.Group>
        <h2>Copy translations</h2>
        <p>Copy Translations" attempts to reuse translations that have been
          entered in Zanata by matching them with untranslated strings in your
          project/version. Consequently, "Copy Translations" is best used
          before translation and review work is initiated on a project.
        </p>
        <Alert message="A translation has to pass through each of these checks
        before it will be copied." type="info" showIcon />
        <h3>On content mismatch</h3>
        <p>If the translations are not identical</p>
        <Button className='btn-danger'>Don't copy</Button>
        <h3>On context mismatch</h3>
        <p>If the context (resId, msgctxt) of the translations are not identical
        </p>
        <Radio.Group>
          <Radio.Button>Continue</Radio.Button>
          <Radio.Button className='btn-warn'>Continue as Fuzzy</Radio.Button>
          <Radio.Button className='btn-danger'>Don't continue</Radio.Button>
        </Radio.Group>
        <h3>On project mismatch</h3>
        <p>If the translations are not both from this project
        </p>
        <Radio.Group>
          <Radio.Button>Continue</Radio.Button>
          <Radio.Button className='btn-warn'>Continue as Fuzzy</Radio.Button>
          <Radio.Button className='btn-danger'>Don't continue</Radio.Button>
        </Radio.Group>
        <h3>On document mismatch</h3>
        <p>If the translations are not both from the same document & document path
        </p>
        <Radio.Group>
          <Radio.Button>Continue</Radio.Button>
          <Radio.Button className='btn-warn'>Continue as Fuzzy</Radio.Button>
          <Radio.Button className='btn-danger'>Don't continue</Radio.Button>
        </Radio.Group>
        <Card>
          <h3>If all the previous steps have passed,
            <span className='txt-success'>
            copy as translated
          </span></h3>
          <Alert message="Unless previously copied as fuzzy" type="warning" showIcon />
        </Card>
      </div>
      /* eslint-enable max-len */
    )
  }
}

export default Translation
