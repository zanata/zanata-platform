// @ts-nocheck
import React from 'react'
import * as PropTypes from 'prop-types'
import Col from 'antd/lib/col'
import 'antd/lib/col/style/css'
import Row from 'antd/lib/row'
import 'antd/lib/row/style/css'
import { FormattedDate, FormattedTime } from 'react-intl'
import { Icon, LoaderText } from '../../../components'
import { isEmpty } from 'lodash'
import cx from 'classnames'
import Modal from 'antd/lib/modal'
import 'antd/lib/modal/style/css'

/**
 * Modal to show detail for a single glossary term
 */
class GlossaryTermModal extends React.Component {
  static propTypes = {
    show: PropTypes.bool.isRequired,
    close: PropTypes.func.isRequired,
    sourceLocale: PropTypes.string.isRequired,
    targetLocale: PropTypes.string.isRequired,
    term: PropTypes.shape({
      source: PropTypes.string.isRequired,
      target: PropTypes.string.isRequired
    }).isRequired,
    details: PropTypes.arrayOf(
      PropTypes.shape({
        description: PropTypes.string,
        lastModifiedDate: PropTypes.number.isRequired,
        pos: PropTypes.string,
        targetComment: PropTypes.string
      })
    ).isRequired,
    // No '.Required' for below since their usage depends on whether source or
    // target text
    directionClassSource: PropTypes.string,
    directionClassTarget: PropTypes.string
  }

  render () {
    const {
      close,
      details,
      show,
      sourceLocale,
      targetLocale,
      term,
      directionClassSource,
      directionClassTarget
    } = this.props

    const selectedDetail = 0
    const detail = details[selectedDetail]

    const lastModifiedTime = detail && detail.lastModifiedDate
      ? new Date(detail.lastModifiedDate) : undefined

    const lastModifiedRow = lastModifiedTime ? (<Row>
      <Icon name="history" className="s0" parentClassName="history-icon" />
      <span className="u-sML-1-4">
      Last modified on&nbsp;
        <FormattedDate value={lastModifiedTime} format="medium" />&nbsp;
        <Icon name="clock" className="s0"
          parentClassName=" history-icon" />&nbsp;
        <FormattedTime value={lastModifiedTime} />
      </span>
    </Row>) : undefined
    const detailsDisplay = details.map(
      (detail, index) => {
        if (!detail) {
          return (
            <tr key={index}>
              <td colSpan="3" className=" u-textCenter">
                <LoaderText loading loadingText='Searching...' />
              </td>
            </tr>
          )
        }
        const { description, pos, targetComment } = detail
        return (
          <tr key={index}>
            <td>{description}</td>
            <td>{pos}</td>
            <td>
              <Icon name="comment" className="n1"
                parentClassName="comment-icon " /> {targetComment}
            </td>
          </tr>
        )
      })

    return (
      <Modal
        visible={show}
        onCancel={close}
        title={'Glossary Details'}
        key='glosssary-term-modal'
        id='GlossaryTermModal'
        width={'90%'}
        footer={null}>
        <Row className='mb4'>
          <Col span={12} className={directionClassSource}>
            <h3>Source Term : {sourceLocale}</h3>
            <span className="modal-term">{term.source}</span>
          </Col>
          <Col span={12} className={directionClassTarget}>
            <h3 className='txt-info'>Translation : {targetLocale}</h3>
            <span className={
              cx('modal-term', {'u-textMuted': isEmpty(term.target)})}>
                {isEmpty(term.target) ? '-none-' : term.target}
            </span>
          </Col>
        </Row>
        <Row>
          <Col span={24} className="mb2">
            <table className={directionClassTarget + ' GlossaryDetails-table'}>
              <thead>
                <tr>
                  <th>Description</th>
                  <th>Part of speech</th>
                  <th>Target comment</th>
                </tr>
              </thead>
              <tbody>
                {detailsDisplay}
              </tbody>
            </table>
          </Col>
        </Row>
        <span className="u-pullRight txt-neutral">
        {lastModifiedRow}
        </span>
      </Modal>
    )
  }
}

export default GlossaryTermModal
