import React from 'react'
import PropTypes from 'prop-types'
import { Panel, Row, Table } from 'react-bootstrap'
import { FormattedDate, FormattedTime } from 'react-intl'
import { Icon, LoaderText, Modal } from '../../../components'

import { createAction } from 'redux-actions'
import { LOCALE_SELECTED } from '../../actions/header-action-types'

export const localeDetails = createAction(LOCALE_SELECTED)

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
    directionClassSource: PropTypes.object,
    isLtrSource: PropTypes.bool,
    directionClassTarget: PropTypes.object,
    isLtrTarget: PropTypes.bool
  }

  constructor (props) {
    super(props)
    this.state = {
      // TODO location detection so default of isLtr = false can be removed
      isLtrSource: false,
      isLtrTarget: false
    }
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

    const lastModifiedTime = detail
      ? new Date(detail.lastModifiedDate) : new Date()

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

    const directionClassSource = localeDetails.isLtr ? 'rtl' : 'ltr'
    const directionClassTarget = localeDetails.isLtr ? 'ltr' : 'rtl'

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
          <Panel className={directionClassSource + ' split-panel'}>
            <h3>Source Term : {sourceLocale}</h3>
            <span className="modal-term">{term.source}</span>
          </Panel>
          <Panel className={directionClassTarget + ' split-panel'}>
            <h3>Target Term : {targetLocale}</h3>
            <span className="modal-term">{term.target}</span>
          </Panel>
          <br />
          <Panel className="gloss-details-panel">
            <Table className={directionClassTarget + ' GlossaryDetails-table'}>
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
            <Row>
              <Icon name="history" className="s0 history-icon" />
              <span className="u-sML-1-4">
                Last modified on&nbsp;
                <FormattedDate value={lastModifiedTime} format="medium" />&nbsp;
                <Icon name="clock" className="s0 history-icon" />&nbsp;
                <FormattedTime value={lastModifiedTime} />
              </span>
            </Row>
          </span>
        </Modal.Body>
      </Modal>
    )
  }
}

export default GlossaryTermModal
