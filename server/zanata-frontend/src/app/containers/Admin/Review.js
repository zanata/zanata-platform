import React from 'react'
import { Component } from 'react'
import * as PropType from 'prop-types'
import {connect} from 'react-redux'
import RejectionsForm, {MAJOR, MINOR, CRITICAL}
  from '../../components/RejectionsForm'
import Icon from '../../components/Icon'
import {Button, Accordion, Panel, Alert} from 'react-bootstrap'
import {
  fetchAllCriteria, addNewCriterion, editCriterion, removeCriterion
} from '../../actions/review-actions'
import {selectors} from '../../reducers/admin-reducer'

const DO_NOT_RENDER = undefined

const exampleHeader = <span>Example criteria:
  <span className="text-muted"> click to expand</span></span>
/* eslint-disable max-len */
const exampleCriteria = <Accordion expanded={false} defaultExpanded={false}>
  <Panel header={exampleHeader} eventKey="1">
    <RejectionsForm
      editable={false}
      criteriaPlaceholder='Translation Errors (terminology, mistranslated, addition, omission, un-localized, do not translate, etc)'
      priority={CRITICAL} />
    <RejectionsForm
      editable
      className='active'
      criteriaPlaceholder='Language Quality (grammar, spelling, punctuation, typo, ambiguous wording, product name, sentence structuring, readability, word choice, not natural, too literal, style and tone, etc)'
      priority={MAJOR} />
    <RejectionsForm
      editable={false}
      criteriaPlaceholder='Consistency (inconsistent style or vocabulary, brand inconsistency, etc.)'
      priority={MAJOR} />
    <RejectionsForm
      editable={false}
      criteriaPlaceholder='Style Guide & Glossary Violations'
      priority={MINOR} />
    <RejectionsForm
      editable={false}
      criteriaPlaceholder='Format (mismatches, white-spaces, tag error or missing, special character, numeric format, truncated, etc.)'
      priority={MINOR} />
    <RejectionsForm
      editable
      className='active'
      criteriaPlaceholder='Other (reason may be in comment section/history if necessary)'
      priority={CRITICAL} />
  </Panel>
</Accordion>
/* eslint-enable max-len */

class AdminReview extends Component {
  static propTypes = {
    criteria: PropType.arrayOf(PropType.shape({
      editable: PropType.bool.isRequired,
      description: PropType.string.isRequired,
      priority: PropType.oneOf([MINOR, MAJOR, CRITICAL]).isRequired
    })).isRequired,
    fetchAllCriteria: PropType.func.isRequired,
    addNewEntry: PropType.func.isRequired,
    editEntry: PropType.func.isRequired,
    deleteEntry: PropType.func.isRequired,
    notification: PropType.string
  }
  constructor (props) {
    super(props)
    this.state = {
      showNewEntryForm: false
    }
  }
  componentDidMount () {
    this.props.fetchAllCriteria()
  }
  showAddNewEntryForm = () => {
    this.setState(prevState => ({
      showNewEntryForm: true
    }))
  }
  saveNewEntry = (entry) => {
    this.props.addNewEntry(entry)
  }
  render () {
    const {criteria, deleteEntry, editEntry, notification} = this.props
    const criteriaList = criteria.map((c, i) => <RejectionsForm key={i}
      editable={c.editable} entityId={c.id} onDelete={deleteEntry}
      criteriaPlaceholder={c.description} isAdminMode displayDelete
      onSave={editEntry} description={c.description}
      priority={c.priority} />)
    const newEntryForm = this.state.showNewEntryForm ? (
      <Panel header="Add new entry">
        <RejectionsForm priority={MINOR} isAdminMode displayDelete={false}
          criteriaPlaceholder="fill in criteria"
          onSave={this.saveNewEntry} />
      </Panel>) : DO_NOT_RENDER

    const notificationBar = notification &&
      <Alert bsStyle="danger">{notification}</Alert>
    return <div className='container'>
      {notificationBar}
      {exampleCriteria}
      <h1>Reject translations settings</h1>
      {criteriaList}
      {newEntryForm}
      <div className='rejection-btns'>
        <Button bsStyle='primary' className='btn-left'
          onClick={this.showAddNewEntryForm}>
          <Icon name='plus' className='s1' /> Add review criteria
        </Button>
      </div>
    </div>
  }
}

const mapStateToProps = state => {
  return {
    criteria: selectors.getCriteria(state.admin),
    notification: selectors.getNotification(state.admin)
  }
}

const mapDispatchToProps = dispatch => {
  return {
    fetchAllCriteria: () => dispatch(fetchAllCriteria()),
    addNewEntry: (criterion) => dispatch(addNewCriterion(criterion)),
    editEntry: (criterion) => dispatch(editCriterion(criterion)),
    deleteEntry: (id) => dispatch(removeCriterion(id))
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(AdminReview)
