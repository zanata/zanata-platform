/* eslint-disable */
// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import {Icon} from '../../../components'
import Form from 'antd/lib/form'
import 'antd/lib/form/style/css'
import Input from 'antd/lib/input'
import 'antd/lib/input/style/css'
import Row from 'antd/lib/row'
import 'antd/lib/row/style/css'
import Col from 'antd/lib/col'
import 'antd/lib/col/style/css'
import Radio from 'antd/lib/radio'
import 'antd/lib/radio/style/css'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'

const FormItem = Form.Item
const { TextArea } = Input
const RadioGroup = Radio.Group

class General extends Component {

  render() {

    return (
      /* eslint-disable max-len */
      <div className='flexTab wideView'>
        <h1 className='txt-info'><Icon name='settings' className='s5 v-sub mr2' />
          <span className='fw4'>General Settings</span></h1>
        <Form layout='horizontal'>
          <Row>
            <Col span={14}>
              <FormItem label='Project Name'>
                <Input placeholder='Name of project' />
              </FormItem>
            </Col>
            <Col span={8} offset={2}>
              <FormItem label='Project ID'>
                <Input placeholder='Project ID' />
              </FormItem>
            </Col>
          </Row>
          <Row>
            <Col span={24}>
              <FormItem label='Project description (optional)'>
                <TextArea rows={4} placeholder='Description of project' />
              </FormItem>
            </Col>
          </Row>
          <Row>
            <Col span={24}>
              <h3>Project type</h3>
              <p>Determines how the project is treated for upload and download by
                clients or through the website. </p>
              <RadioGroup>
                <Radio className='w-100' value="a">File
                  <span className='ml2 txt-muted'>
                  For plain text, Libre Office, InDesign, HTML, Subtitles etc.
                </span></Radio>
                <Radio className='w-100' value="b">Gettext
                  <span className='ml2 txt-muted'>
                  For Gettext software strings</span></Radio>
                <Radio className='w-100' value="c">Podir
                  <span className='ml2 txt-muted'>
                  For Publican/Docbook strings</span></Radio>
                <Radio className='w-100' value="d">Properties
                  <span className='ml2 txt-muted'>
                  For Java properties files</span></Radio>
                <Radio className='w-100' value="e">Utf8 Properties
                  <span className='ml2 txt-muted'>
                  For UTF8-encoded Java properties</span></Radio>
                <Radio className='w-100' value="f">Xliff
                  <span className='ml2 txt-muted'>
                  For supported XLIFF files</span></Radio>
                <Radio className='w-100' value="g">XML
                  <span className='ml2 txt-muted'>
                  For XML from the Zanata REST API</span></Radio>
                <Radio className='w-100' value="h">Unspecified
                  <span className='ml2 txt-muted'>
                  A setting for older projects</span></Radio>
              </RadioGroup>
            </Col>
          </Row>
          <Row>
            <h3 className='mt4'>Source code</h3>
            <Col span={11}>
              <FormItem label='Home page (optional)'>
                <Input placeholder='Name of project' />
                <p className='f7'>e.g. https://github.com/zanata/zanata-platform</p>
              </FormItem>
            </Col>
            <Col span={11} offset={2}>
              <FormItem label='Project ID'>
                <Input placeholder='Project ID' />
                <p className='f7'>A clone-able url for your source code
                (usually uses SSH)<br />
                  e.g. <span className='code txt-error'>
                    git@github.com:zanata/zanata-server.git</span></p>
              </FormItem>
            </Col>
          </Row>
          <Row>
            <Button type='primary'>Update general settings</Button>
          </Row>
          <hr />
          <Row>
            <Col span={12}>
              <Button className='btn-warn'>
                <Icon name='locked' className='s0 v-sub mr2' />
              Make this project read only</Button>
            </Col>
            <Col span={12}>
              <Button className='btn-danger'>
                <Icon name='trash' className='s0 v-sub mr2' />
                Delete this project</Button>
            </Col>
          </Row>
        </Form>
      </div>
      /* eslint-enable max-len */
    )
  }
}

export default General
