import React, { PropTypes } from 'react'
import { Modal, Panel, Row, Table } from 'react-bootstrap'
import { FormattedDate, FormattedTime } from 'react-intl'
import { Icon } from '../../../components'

/**
 * Modal to show detail for a single glossary term
 */
const GlossaryTermModal = React.createClass({
  propTypes: {
    show: PropTypes.bool.isRequired,
    close: PropTypes.func.isRequired,
    sourceLocale: PropTypes.string.isRequired,
    term: PropTypes.shape({
      source: PropTypes.string.isRequired,
      target: PropTypes.string.isRequired
    }).isRequired,
    details: PropTypes.arrayOf(
      PropTypes.shape({
        description: PropTypes.string,
        lastModifiedTime: PropTypes.instanceOf(Date).isRequired,
        pos: PropTypes.string,
        transComment: PropTypes.string
      })
    ).isRequired
  },

  render: function () {
    const { close, details, sourceLocale, targetLocale, term } = this.props

    const selectedDetail = 0;
    const detail = details[selectedDetail]

    const { description, pos, transComment, lastModifiedTime } = detail

    const detailsDisplay = details.map(
      ({description, lastModifiedTime, pos, transComment}, index) => (
        <tr key={index}>
          {/* Do we need this column?
          <td>#{index}</td>
          */}
          <td>{description}</td>
          <td>{pos}</td>
          <td>
            <Icon name="comment" className="comment-icon n1"/> {transComment}
          </td>
        </tr>
      ))

    return (
      <Modal show
        onHide={close}>
        <Modal.Header closeButton>
          <Modal.Title>Glossary details</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Panel className="split-panel">
            <h3>Source Term [{sourceLocale}]:</h3>
            <span className="modal-term">{term.source}</span>
          </Panel>
          <Panel className="split-panel">
            <h3>Target Term [{targetLocale}]:</h3>
            <span className="modal-term">{term.target}</span>
          </Panel>
          <br />
          <Panel className="gloss-details-panel">
            <Table className="gloss-details-table">
              <thead>
                <tr>
            {/* Do we need this column?
                  <th></th>
            */}
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

          <span className="pull-right u-textMeta">
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
})

export default GlossaryTermModal
