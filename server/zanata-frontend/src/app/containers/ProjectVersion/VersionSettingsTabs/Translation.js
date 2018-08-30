/* eslint-disable */
// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import {Icon} from '../../../components'
import Radio from 'antd/lib/radio'
import 'antd/lib/radio/style/css'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import Collapse from 'antd/lib/collapse'
import 'antd/lib/collapse/style/css'
import Row from 'antd/lib/row'
import 'antd/lib/row/style/css'
import Col from 'antd/lib/col'
import 'antd/lib/col/style/css'
import Highlight from 'react-highlight'

const Panel = Collapse.Panel

class Translation extends Component {
  render() {
    return (
      /* eslint-disable max-len */
      <div className='flexTab wideView'>
        <h1 className='txt-info'>
          <Icon name='translate' className='s5 v-sub mr2' />
          Translation
        </h1>
        <h2>Validation</h2>
        <p>Use these validations to keep translations consistent with the
          source text.</p>
        <Button size='small' type='primary'>Copy translation validation settings
          from project</Button>
        <h3>HTML/XML tags</h3>
        <p className='w-100'>Check that XML/HTML tags are consistent.</p>
        <Radio.Group>
          <Radio.Button>Off</Radio.Button>
          <Radio.Button className='btn-warn'>Warning</Radio.Button>
          <Radio.Button className='btn-danger'>Error</Radio.Button>
        </Radio.Group>
        <Collapse bordered={false}>
          <Panel showArrow={false}  header="Show example..." key="1">
            <Row>
              <Col span={11}>
                <span className='fw5 txt-newblue'>SOURCE</span>
                <Highlight innerHTML={false}>
                  {'<p><strong>Hello world</strong></p>'}
                </Highlight>
              </Col>
              <Col span={13}>
                <span className='fw5 txt-newblue'>TARGET</span>
                <Highlight innerHTML={false}>
                  {'<p><strong>Hello world</stong></p>'}
                </Highlight>
              </Col>
            </Row>
          </Panel>
        </Collapse>
        <h3>Java variables</h3>
        <p>Check that java style variables are consistent.</p>
        <Radio.Group>
          <Radio.Button>Off</Radio.Button>
          <Radio.Button className='btn-warn'>Warning</Radio.Button>
          <Radio.Button className='btn-danger'>Error</Radio.Button>
        </Radio.Group>
        <Collapse bordered={false}>
          <Panel showArrow={false}  header="Show example..." key="2">
            <Row>
              <Col span={11}>
                <span className='fw5 txt-newblue'>SOURCE</span>
                <Highlight innerHTML={false}>
                  {'value must be between {0} and {1}'}
                </Highlight>
              </Col>
              <Col span={13}>
                <span className='fw5 txt-newblue'>TARGET</span>
                <Highlight innerHTML={false}>
                  {'value must be between {0} and {2}'}
                </Highlight>
              </Col>
            </Row>
          </Panel>
        </Collapse>
        <h3>Leading/trailing newline</h3>
        <p>Check for consistent leading and trailing newline.</p>
        <Radio.Group>
          <Radio.Button>Off</Radio.Button>
          <Radio.Button className='btn-warn'>Warning</Radio.Button>
          <Radio.Button className='btn-danger'>Error</Radio.Button>
        </Radio.Group>
        <Collapse bordered={false}>
          <Panel showArrow={false}  header="Show example..." key="3">
            <Row>
              <Col span={11}>
                <span className='fw5 txt-newblue'>SOURCE</span>
                <Highlight innerHTML={false}>
                  {'\\n hello world with lead new line'}
                </Highlight>
              </Col>
              <Col span={13}>
                <span className='fw5 txt-newblue'>TARGET</span>
                <Highlight innerHTML={false}>
                  {'missing \\n hello world with lead new line'}
                </Highlight>
              </Col>
            </Row>
          </Panel>
        </Collapse>
        <h3>Positional printf (XSI extension)</h3>
        <p>Check that positional printf style (%n$x) variables are consistent.</p>
        <Radio.Group>
          <Radio.Button>Off</Radio.Button>
          <Radio.Button className='btn-warn'>Warning</Radio.Button>
          <Radio.Button className='btn-danger'>Error</Radio.Button>
        </Radio.Group>
        <Collapse bordered={false}>
          <Panel showArrow={false}  header="Show example..." key="4">
            <Row>
              <Col span={11}>
                <span className='fw5 txt-newblue'>SOURCE</span>
                <Highlight innerHTML={false}>
                  {'value must be between %x$1 and %y$2'}
                </Highlight>
              </Col>
              <Col span={13}>
                <span className='fw5 txt-newblue'>TARGET</span>
                <Highlight innerHTML={false}>
                  {'value must be between %x$1 and %y$3'}
                </Highlight>
              </Col>
            </Row>
          </Panel>
        </Collapse>
        <h3>Printf variables</h3>
        <p>Check that printf style (%x) variables are consistent.</p>
        <Radio.Group>
          <Radio.Button>Off</Radio.Button>
          <Radio.Button className='btn-warn'>Warning</Radio.Button>
          <Radio.Button className='btn-danger'>Error</Radio.Button>
        </Radio.Group>
        <Collapse bordered={false}>
          <Panel showArrow={false}  header="Show example..." key="5">
            <Row>
              <Col span={11}>
                <span className='fw5 txt-newblue'>SOURCE</span>
                <Highlight innerHTML={false}>
                  {'value must be between %x and %y'}
                </Highlight>
              </Col>
              <Col span={13}>
                <span className='fw5 txt-newblue'>TARGET</span>
                <Highlight innerHTML={false}>
                  {'value must be between %x and %z'}
                </Highlight>
              </Col>
            </Row>
          </Panel>
        </Collapse>
        <h3>Tab characters</h3>
        <p>Check whether source and target have the same number of tabs.
        </p>
        <Radio.Group>
          <Radio.Button>Off</Radio.Button>
          <Radio.Button className='btn-warn'>Warning</Radio.Button>
          <Radio.Button className='btn-danger'>Error</Radio.Button>
        </Radio.Group>
        <Collapse bordered={false}>
          <Panel showArrow={false}  header="Show example..." key="6">
            <Row>
              <Col span={11}>
                <span className='fw5 txt-newblue'>SOURCE</span>
                <Highlight innerHTML={false}>
                  {'\\t hello world'}
                </Highlight>
              </Col>
              <Col span={13}>
                <span className='fw5 txt-newblue'>TARGET</span>
                <Highlight innerHTML={false}>
                  {'missing tab char (\\t) hello world'}
                </Highlight>
              </Col>
            </Row>
          </Panel>
        </Collapse>
        <h3>XML entity reference</h3>
        <p>Check that XML entities are complete.</p>
        <Radio.Group>
          <Radio.Button>Off</Radio.Button>
          <Radio.Button className='btn-warn'>Warning</Radio.Button>
          <Radio.Button className='btn-danger'>Error</Radio.Button>
        </Radio.Group>
        <Collapse bordered={false}>
          <Panel showArrow={false}  header="Show example..." key="7">
            <Row>
              <Col span={11}>
                <span className='fw5 txt-newblue'>SOURCE</span>
                <Highlight innerHTML={false}>
                  {'Pepper &amp; salt'}
                </Highlight>
              </Col>
              <Col span={13}>
                <span className='fw5 txt-newblue'>TARGET</span>
                <Highlight innerHTML={false}>
                  {'Pepper amp incomplete entity, missing \'& and ;\' salt'}
                </Highlight>
              </Col>
            </Row>
          </Panel>
        </Collapse>
      </div>
      /* eslint-enable max-len */
    )
  }
}

export default Translation
