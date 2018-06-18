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

interface ConcurrentModalProps {
  show: boolean,
  selectedPhrase: any,
  closeConcurrentModal: () => void
}

class ConcurrentModal extends React.Component<ConcurrentModalProps, {}> {
  public static propTypes = {
    show: PropTypes.bool,
    selectedPhrase: PropTypes.any,
    closeConcurrentModal: PropTypes.func
  }
  public render () {
    const { show, selectedPhrase, closeConcurrentModal } = this.props
    if (!selectedPhrase.conflict) {
      return null
    }
    const original = selectedPhrase.conflict.response.content
    const latest = selectedPhrase.conflict.saveInfo.translations[0]
    const lastModifiedTime = new Date(2016, 12, 4, 2, 19)
    const onCancel = () => closeConcurrentModal()
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
                value={latest}
                placeholder={latest} />
            </span>
            <span className='u-floatLeft'>
              <DateAndTimeDisplay dateTime={lastModifiedTime}
                className='u-block small u-sMT-1-2 u-sPB-1-4 u-textMuted u-textSecondary' />
            </span>
            <span className='u-floatRight'>
              <Button
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
                value={original}
                placeholder={original} />
            </span>
            <span className='u-sizeHeight-1_1-2'>
              <span className='u-floatLeft'>
                <DateAndTimeDisplay dateTime={lastModifiedTime}
                  className='u-block small u-sMT-1-2 u-sPB-1-4 u-textMuted u-textSecondary' />
              </span>
              <span className='u-floatRight'>
                <Button
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
