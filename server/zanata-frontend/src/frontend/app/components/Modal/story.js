import React from 'react'
import { storiesOf } from '@kadira/storybook'
import { action, decorateAction } from '@kadira/storybook-addon-actions'
import { Modal } from '../Modal'
import { Modal as OverlayModal } from 'react-overlays'
import { Button } from 'react-bootstrap'
import { Icon } from '../../components'

storiesOf('Modal', module)
    .addDecorator((story) => (
        <OverlayModal
            containerClassName='has-modal'
            className='modal'
        >
          <div className='container'>
            <div className='modal-content'>
              {closeButton && (
                  <Button aria-label={closeLabel}
                          className='close s0'
                          onClick={onHide}>
                    <Icon name='cross' className='s2 closeIcon' />
                  </Button>
              )}
              {children}
            </div>
          </div>
        </OverlayModal>
    ))
