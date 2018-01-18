import React, {Component} from 'react'
import PropType from 'prop-types'
import {connect} from 'react-redux'
import RejectionsForm, {MAJOR, MINOR, CRITICAL}
  from '../../components/RejectionsForm'
import Icon from '../../components/Icon'
import {Button, Panel, Alert} from 'react-bootstrap'
import {
  fetchAllCriteria, addNewCriterion, editCriterion, removeCriterion
} from '../../actions/review-actions'
import {selectors} from '../../reducers/admin-reducer'

const DO_NOT_RENDER = undefined
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
