import React from 'react'
import PropTypes from 'prop-types'
import { Panel, Row, Table } from 'react-bootstrap'
import { FormattedDate, FormattedTime } from 'react-intl'
import { Icon, LoaderText, Modal } from '../../../components'
import { isEmpty } from 'lodash'
import cx from 'classnames'

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
    ).isRequired
  }

  render () {
    const {
      close,
      details,
      show,
      sourceLocale,
      targetLocale,
      term
    } = this.props

    const selectedDetail = 0
    const detail = details[selectedDetail]

    const lastModifiedTime = detail && detail.lastModifiedDate
      ? new Date(detail.lastModifiedDate) : undefined

    const lastModifiedRow = lastModifiedTime ? (<Row>
      <Icon name="history" className="s0 history-icon" />
      <span className="u-sML-1-4">
      Last modified on&nbsp;
        <FormattedDate value={lastModifiedTime} format="medium" />&nbsp;
        <Icon name="clock" className="s0 history-icon" />&nbsp;
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
              <Icon name="comment"
                className="comment-icon n1" /> {targetComment}
            </td>
          </tr>
        )
      })

    // TODO className glossary-modal
    return (
      <Modal show={show}
        onHide={close}
        key="glosssary-term-modal"
        id="GlossaryTermModal">
        <Modal.Header>
          <Modal.Title><small><span className="u-pullLeft">
          Glossary details</span></small></Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Panel className="split-panel">
            <h3>Glossary Term [{sourceLocale}]:</h3>
            <span className="modal-term">{term.source}</span>
          </Panel>
          <Panel className="split-panel">
            <h3>Translation [{targetLocale}]:</h3>
            <span className={
              cx('modal-term', {'u-textMuted': isEmpty(term.target)})}>
              {isEmpty(term.target) ? '-none-' : term.target}
            </span>
          </Panel>
          <br />
          <Panel className="gloss-details-panel">
            <Table className="GlossaryDetails-table">
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
            </Table>
          </Panel>

          <span className="u-pullRight u-textMeta">
          {lastModifiedRow}
          </span>
        </Modal.Body>
      </Modal>
    )
  }
}

export default GlossaryTermModal
