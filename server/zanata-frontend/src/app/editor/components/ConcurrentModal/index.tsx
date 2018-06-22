// @ts-ignore
import React from 'react'
import PropTypes from 'prop-types'
import Alert from 'antd/lib/alert'
import 'antd/lib/alert/style/css'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import Card from 'antd/lib/card'
import 'antd/lib/card/style/css'
import Tag from 'antd/lib/tag'
import 'antd/lib/tag/style/css'
import Modal from 'antd/lib/modal'
import 'antd/lib/modal/style/index.less'
import DateAndTimeDisplay from '../DateAndTimeDisplay'
import Textarea from 'react-textarea-autosize'

export enum resolution {
  latest = 'latest',
  original = 'original',
}

interface ConcurrentModalProps {
  closeConcurrentModal: () => void
  saveResolveConflict: (latest: any, original: any, resolution: resolution) => void
  show: boolean
  selectedPhrase?: any
}

class ConcurrentModal extends React.Component<ConcurrentModalProps, {}> {
  public static propTypes = {
    closeConcurrentModal: PropTypes.func,
    saveResolveConflict: PropTypes.func.isRequired,
    selectedPhrase: PropTypes.any,
    show: PropTypes.bool.isRequired,
  }
  public render () {
    const {
      closeConcurrentModal, saveResolveConflict, show, selectedPhrase
    } = this.props
    if (!selectedPhrase.conflict) {
      return null
    }
    const original = selectedPhrase.conflict.saveInfo
    const latest = selectedPhrase.conflict.response
    const lastModifiedTime = new Date(2016, 12, 4, 2, 19)
    const onCancel = () => closeConcurrentModal()
    const saveLatest = () => {
      saveResolveConflict(latest, original, resolution.latest)
    }
    const saveOriginal = () => {
      saveResolveConflict(latest, original, resolution.original)
    }
    return (
      /* eslint-disable max-len */
      <Modal
        title={'Current conflicts'}
        visible={show}
        width={'46rem'}
        onCancel={onCancel}
        footer={null}>
          <Alert message='Username has saved a new version while you
            are editing. Please resolve conflicts.' type='error' />
          <Card>
            <p className='u-sizeHeight-1_1-2'>
              <strong>Username</strong> created a <span
                className='u-textSuccess'>Translated</span> revision <Tag
                  color='blue'>latest</Tag>
            </p>
            <span className='revisionBox'>
              <Textarea
                className='form-control'
                value={latest.content}/>
            </span>
            <span className='u-floatLeft'>
              <DateAndTimeDisplay dateTime={lastModifiedTime}
                className='u-block small u-sMT-1-2 u-sPB-1-4 u-textMuted u-textSecondary' />
            </span>
            <span className='u-floatRight'>
              <Button
                onClick={saveLatest}
                className='EditorButton Button--secondary u-rounded'>Use latest
              </Button>
            </span>
          </Card>
          <Card>
            <p className='u-sizeHeight-1_1-2'><strong>You</strong> created
              an <span className='u-textHighlight'>Unsaved</span> revision.
            </p>
            <span className='revisionBox'>
              <Textarea
                className='form-control'
                value={original.translations[0]} />
            </span>
            <span className='u-sizeHeight-1_1-2'>
              <span className='u-floatLeft'>
                <DateAndTimeDisplay dateTime={lastModifiedTime}
                  className='u-block small u-sMT-1-2 u-sPB-1-4 u-textMuted u-textSecondary' />
              </span>
              <span className='u-floatRight'>
                <Button
                  onClick={saveOriginal}
                  className='EditorButton Button--primary u-rounded'>
                  Use original
                </Button>
              </span>
            </span>
          </Card>
      </Modal>
      /* eslint-enable max-len */
    )
  }
}

export default ConcurrentModal
