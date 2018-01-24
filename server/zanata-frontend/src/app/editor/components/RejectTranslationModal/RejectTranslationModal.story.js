import * as React from 'react'
import { storiesOf, action } from '@storybook/react'
import RejectTranslationModal from '.'
import Lorem from 'react-lorem-component'
import { MINOR, MAJOR, CRITICAL } from './index.js'
import { Modal } from '../../../components'
import { Row, Button } from 'react-bootstrap'

/*
 * TODO add stories showing the range of states
 *      for RejectTranslationModal
 */
storiesOf('RejectTranslationModal', module)
    .addDecorator((story) => (
        <div>
          <h1>Lorem Ipsum</h1>
          <Lorem count={1} />
          <Lorem mode="list" />
          <h2>Dolor Sit Amet</h2>
          <Lorem />
          <Lorem mode="list" />
          <div className="static-modal">
            {story()}
          </div>
        </div>
    ))
    .add('Criteria chosen', () => (
        <RejectTranslationModal show isOpen
         criteria="Translation Errors: terminology, mistranslated addition, omission, un-localized, do not translate, etc"
         priority={CRITICAL}  textState="u-textDanger" />
    ))

    .add('Other - no criteria set', () => (
        <Modal show key="reject-translation-modal"
               id="RejectTranslationModal">
          <Modal.Header>
            <Modal.Title>Reject translation</Modal.Title>
          </Modal.Header>
          <Modal.Body>
        <div className="EditorRejection-input">
          <textarea ref="input"
                    type="comment"
                    placeholder="Provide a comment for why this translation has been rejected"
                    cols="50"
                    rows="10"
                    className='EditorInputGroup-input is-focused InputGroup--outlined Commenting' />
           </div>
          </Modal.Body>
          <Modal.Footer>
          <span>
            <Row>
              <Button className="EditorButton Button--large u-rounded Button--secondary">
                Cancel
              </Button>
              <Button className="EditorButton Button--large u-rounded Button--primary">
                Reject translation
              </Button>
            </Row>
          </span>
          </Modal.Footer>
        </Modal>
    ))
