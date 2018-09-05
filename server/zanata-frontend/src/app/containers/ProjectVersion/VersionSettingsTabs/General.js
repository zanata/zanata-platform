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
import Tooltip from "antd/lib/tooltip";

const FormItem = Form.Item
const { TextArea } = Input
const RadioGroup = Radio.Group

class General extends Component {

  render() {
    const info = 'Example: my-project. This will become part of your project\'s URL. Changing it may break other people\'s bookmarks to the project.'
    const versionIdLabel = (
      <span className='di'>
        Project ID
        <Tooltip title={info}
                 className='tc fr' placement='top' arrowPointAtCenter>
          <Button icon='info-circle-o' className='btn-link' />
        </Tooltip>
      </span>
    )
    const versionType = 'Help: Creating a version and project type'

    return (
      /* eslint-disable max-len */
      <div className='flexTab wideView'>
        <h1 className='txt-info'><Icon name='settings' className='s5 v-sub mr2' />
          <span className='fw4'>General Settings</span></h1>
        <Form layout='horizontal'>
          <Row>
            <Col span={24}>
              <FormItem label={versionIdLabel}>
                <Input placeholder='Version ID' />
              </FormItem>
            </Col>
          </Row>
          <Row>
            <Col span={24}>
              <h3>Project type</h3>
              <p>Determines how the project is treated for upload and download by
                clients or through the website.
              <Tooltip title={versionType}
                         className='tc fr' placement='top' arrowPointAtCenter>
                  <Button icon='question-circle-o' target='_blank'
                          href='http://docs.zanata.org/en/release/user-guide/versions/create-version/'
                          className='btn-link' />
                </Tooltip></p>
              <Button disabled size='small' className='mb4' >
                Copy project type from project</Button>
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
            <Button className='mb2 mt4' type='primary'>Update general settings</Button>
          </Row>
          <hr />
          <Row>
            <Col span={12} className='mt2'>
              <Button className='btn-warn'>
                <Icon name='locked' className='s0 v-sub mr2' />
              Make this version read only</Button>
            </Col>
            <Col span={12} className='mt2'>
              <Button className='btn-danger'>
                <Icon name='trash' className='s0 v-sub mr2' />
                Delete this version</Button>
            </Col>
          </Row>
        </Form>
      </div>
      /* eslint-enable max-len */
    )
  }
}

export default General
