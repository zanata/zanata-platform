import React from 'react'
import { storiesOf } from '@storybook/react'
import { action } from '@storybook/addon-actions'
import {
  Button,
  Row,
  Table,
  OverlayTrigger,
  Tooltip,
  Badge
} from 'react-bootstrap'
import {Icon, Modal} from '../../components'
import Lorem from 'react-lorem-component'

const tooltipAssam = (
  <Tooltip id='tooltipassam'>
    Assamese
    <br />
    অসমীয়া
  </Tooltip>
)

const tooltipGerman = (
  <Tooltip id='tooltipassam'>
    German
    <br />
    Deutsch
  </Tooltip>
)

const tooltipJapan = (
  <Tooltip id='tooltipassam'>
    Japanese
    <br />
    日本語
  </Tooltip>
)

const tooltipTMX = (
  <Tooltip id='tooltipTMXnote'>
    Some systems can't import TMX with srclang=*all*
  </Tooltip>
)

const docCountAss = (
  <Tooltip id='docall'>12 documents</Tooltip>
)

const docCountGerman = (
  <Tooltip id='docall'>12 documents</Tooltip>
)

const docCountJapan = (
  <Tooltip id='docall'>12 documents</Tooltip>
)

const docCountAll = (
  <Tooltip id='docall'>36 documents</Tooltip>
)

storiesOf('Modal', module)
  .addDecorator((story) => (
    <div className='static-modal'>
      {story()}
    </div>
  ))
  .add('default', () => (
    <Modal
      show
      onHide={action('onHide')}>
      <Modal.Header>
        <Modal.Title>Modal heading</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Lorem />
      </Modal.Body>
      <Modal.Footer>
        <span className='bootstrap u-pullRight'>
          <Row>
            <Button bsStyle='link'
              className='btn-left'
              onClick={action('onClick')}>
              Close
            </Button>
            <Button
              bsStyle='primary'
              onClick={action('onClick')}>
              Save
            </Button>
          </Row>
        </span>
      </Modal.Footer>
    </Modal>
  ))
  .add('TMX export - one language', () => (
    <Modal
      show
      onHide={action('onHide')}>
      <Modal.Header>
        <Modal.Title>Export Project to TMX</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <span className='tmxExport'>
          <p>Are you sure you want to export this project to TMX?<br />
            <strong>All documents in this project have the source language
              &nbsp;<a href="">en-US</a>.</strong></p>
          <br />
          <p>
            <Button
              bsStyle='primary'
              onClick={action('onClick')}>
              Download
            </Button>
          </p>
        </span>
      </Modal.Body>
    </Modal>
  ))
  .add('TMX export - multiple languages', () => (
    <Modal
      show
      onHide={action('onHide')}>
      <Modal.Header>
        <Modal.Title>Export Project to TMX</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <span className='tmxExport'>
          <p>Are you sure you want to export this project to TMX?</p>
          <p className='lead'>Source languages</p>
          <Table className='tmxTable'>
            <tbody>
              <tr>
                <td>
                  <OverlayTrigger placement='left' overlay={tooltipAssam}>
                    <Button bsStyle='link'>as</Button>
                  </OverlayTrigger>
                </td>
                <td>
                  <OverlayTrigger placement='top' overlay={docCountAss}>
                    <Badge>
                      12 <Icon name='document' className='n1' />
                    </Badge>
                  </OverlayTrigger>
                </td>
                <td>
                  <span className='tmxDownload'>
                    <Button
                      bsStyle='primary'
                      bsSize='small'
                      onClick={action('onClick')}>
                      Download
                    </Button>
                    <span className='asterix'>*</span>
                  </span>
                </td>
              </tr>
              <tr>
                <td>
                  <OverlayTrigger placement='left'
                    overlay={tooltipGerman}>
                    <Button bsStyle='link'>de</Button>
                  </OverlayTrigger>
                </td>
                <td>
                  <OverlayTrigger placement='top' overlay={docCountGerman}>
                    <Badge>
                      12 <Icon name='document' className='n1' />
                    </Badge>
                  </OverlayTrigger>
                </td>
                <td>
                  <span className='tmxDownload'>
                    <Button
                      bsStyle='primary'
                      bsSize='small'
                      onClick={action('onClick')}>
                    Download
                    </Button>
                    <span className='asterix'>*</span>
                  </span>
                </td>
              </tr>
              <tr>
                <td>
                  <OverlayTrigger placement='left'
                    overlay={tooltipJapan}>
                    <Button bsStyle='link'>ja</Button>
                  </OverlayTrigger>
                </td>
                <td>
                  <OverlayTrigger placement='top' overlay={docCountJapan}>
                    <Badge>
                      12 <Icon name='document' className='n1' />
                    </Badge>
                  </OverlayTrigger>
                </td>
                <td>
                  <span className='tmxDownload'>
                    <Button
                      bsStyle='primary'
                      bsSize='small'
                      onClick={action('onClick')}>
                    Download
                    </Button>
                    <span className='asterix'>*</span>
                  </span>
                </td>
              </tr>
              <tr>
                <td><strong>ALL</strong></td>
                <td>
                  <OverlayTrigger placement='top' overlay={docCountAll}>
                    <Badge>
                      36 <Icon name='document' className='n1' />
                    </Badge>
                  </OverlayTrigger>
                </td>
                <td>
                  <Button
                    bsStyle='primary'
                    bsSize='small'
                    onClick={action('onClick')}>
                    Download
                  </Button>
                </td>
              </tr>
            </tbody>
          </Table>
          <p className='allWarnings'>
            <OverlayTrigger placement='top' overlay={tooltipTMX}>
              <Button bsStyle='link'>
                <Icon name='warning' className='n1' />
                &nbsp;Produces a TMX file which<br />some systems can't import.
              </Button>
            </OverlayTrigger>
          </p>
          <p className='u-textWarning'>* All translations of documents for
            the selected source language will be included.
          </p>
        </span>
      </Modal.Body>
    </Modal>
  ))
  .add('TMX export - preparing files', () => (
    <Modal
      show
      onHide={action('onHide')}>
      <Modal.Header>
        <Modal.Title>Export Project to TMX</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <span className='tmxExport'>
          <p><strong>Preparing files</strong></p>
          <p>
            1 of 445
          </p>
        </span>
      </Modal.Body>
    </Modal>
  ))
  .add('TMX export - file prep finished', () => (
    <Modal
      show
      onHide={action('onHide')}>
      <Modal.Header>
        <Modal.Title>Export Project to TMX</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <span className='tmxExport'>
          <p><strong>Files are ready for download</strong></p>
          <p>
            <Button
              bsStyle='primary'
              onClick={action('onClick')}>
              Download
            </Button>
          </p>
        </span>
      </Modal.Body>
    </Modal>
  ))
