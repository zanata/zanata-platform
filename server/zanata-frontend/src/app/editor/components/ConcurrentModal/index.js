/* eslint-disable */
// @ts-nocheck
import React from 'react'
import Alert from 'antd/lib/alert'
import 'antd/lib/alert/style/css'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import Card from 'antd/lib/card'
import 'antd/lib/card/style/css'
import Tag from 'antd/lib/tag'
import 'antd/lib/tag/style/css'
import { Modal } from '../../../components'
import DateAndTimeDisplay from '../DateAndTimeDisplay'
import Textarea from 'react-textarea-autosize'

class ConcurrentModal extends React.Component {
  render() {
    const lastModifiedTime = new Date(2016, 12, 4, 2, 19)
    const original = 'A wonderful serenity has taken possession of my entire' +
      ' soul, like these sweet mornings of spring which I enjoy with my ' +
      'whole heart.'
    const latest = 'A horrible anxiety has taken possession of my entire' +
      ' soul, like these agonising evenings of summer which I loathe with my' +
      ' whole heart.'
    return (
      /* eslint-disable max-len */
      <Modal show={true}
        onHide={close}
        closeButton
        id="ConflictsModal">
        <Modal.Header>
          <Modal.Title>
            <small><span className="u-pullLeft">Current conflicts</span></small>
          </Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Alert type="danger"><span className="alert-link"><a
            href="">Username</a></span> has saved a new version while you
            are editing. Please resolve conflicts.</Alert>
            <Card>
              <span className="u-sizeHeight-1_1-2">
                <strong>Username</strong> created a <span
                  className='u-textSuccess'>Translated</span> revision
                <Tag color='blue'>latest</Tag>
              </span>
              <span className="revisionBox">
                <Textarea
                  className='form-control'
                  value={latest}
                  placeholder={latest} />
              </span>
              <span className='u-floatLeft'>
                <DateAndTimeDisplay dateTime={lastModifiedTime}
                  className="u-block small u-sMT-1-2 u-sPB-1-4 u-textMuted u-textSecondary"/>
              </span>
              <span className='u-floatRight'>
                <Button title="Click should trigger onClick action"
                  className="EditorButton Button--secondary u-rounded">Use latest
                </Button>
              </span>
            </Card>
            <Card>
              <span className="u-sizeHeight-1_1-2"><strong>You</strong> created
                an <span className="u-textHighlight">Unsaved</span> revision.
              </span>
              <span className="revisionBox">
                <Textarea
                  className='form-control'
                  value={original}
                  placeholder={original} />
              </span>
              <span className="u-sizeHeight-1_1-2">
                <span className='u-floatLeft'>
                  <DateAndTimeDisplay dateTime={lastModifiedTime}
                    className="u-block small u-sMT-1-2 u-sPB-1-4 u-textMuted u-textSecondary"/>
                </span>
                <span className='u-floatRight'>
                  <Button title="Click should trigger onClick action"
                    className="EditorButton Button--primary u-rounded">
                    Use original
                  </Button>
                </span>
              </span>
            </Card>
          </Modal.Body>
        </Modal>
        /* eslint-enable max-len */
      )}
    }

export default ConcurrentModal
