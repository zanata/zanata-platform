// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import * as PropType from 'prop-types'
import {connect} from 'react-redux'
import RejectionsForm, {MAJOR, MINOR, CRITICAL}
  from '../../components/RejectionsForm'
import {
  fetchAllCriteria, addNewCriterion, editCriterion, removeCriterion
} from '../../actions/review-actions'
import { Link } from '../../components'
import {selectors} from '../../reducers/admin-reducer'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import Layout from 'antd/lib/layout'
import 'antd/lib/layout/style/css'
import Breadcrumb from 'antd/lib/breadcrumb'
import 'antd/lib/breadcrumb/style/css'
import Card from 'antd/lib/card'
import 'antd/lib/card/style/css'
import Notification from 'antd/lib/notification'
import 'antd/lib/notification/style/css'

const DO_NOT_RENDER = undefined
 /* eslint-disable max-len */

class AdminReview extends Component {
  static propTypes = {
    criteria: PropType.arrayOf(PropType.shape({
      commentRequired: PropType.bool.isRequired,
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
  componentDidUpdate (prevProps, prevState) {
    const { notification } = this.props
    if (notification && prevProps.notification !== notification) {
      Notification[notification.severity]({
        message: notification.message,
        description: notification.description,
        duration: notification.duration
      })
    }
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
    const {criteria, deleteEntry, editEntry} = this.props
    const criteriaList = (criteria.length > 0)
      ? criteria.map((c, i) => <RejectionsForm key={i}
        commentRequired={c.commentRequired} entityId={c.id} onDelete={deleteEntry}
        criteriaPlaceholder={c.description} isAdminMode displayDelete
        onSave={editEntry} description={c.description}
        priority={c.priority} />)
      : null
    const newEntryForm = this.state.showNewEntryForm ? (
      <span className='mb2'>
        <Card title='Add new entry'>
          <RejectionsForm priority={MINOR} isAdminMode displayDelete={false}
            criteriaPlaceholder='fill in criteria'
            onSave={this.saveNewEntry} />
        </Card>
      </span>) : DO_NOT_RENDER
    return <div className='container centerWrapper' id='admin-review'>
      <Layout>
        <Breadcrumb>
          <Breadcrumb.Item href='home'>
            <Link link='/admin/home'>Administration</Link>
          </Breadcrumb.Item>
        </Breadcrumb>
        <h1>Reject translations settings</h1>
        <p className='lead'>Set the translation rejection criteria to be used
          in the editor. Start by adding your first 'new rejection criteria
        entry' and add as many criteria as you require.</p>
        <span className='mb2'>
          <Card type='inner' title='Example criteria'>
            <hr />
            <ul>
              <li><strong>Translation Errors</strong>: terminology, mistranslated,
              addition, omission, un-localized, do not translate, etc</li>
              <li><strong>Language Quality</strong>: grammar, spelling,
                punctuation, typo, ambiguous wording, product name,
                sentence structuring, readability, word choice, not natural,
              too literal, style and tone, etc</li>
              <li><strong>Style Guide and Glossary Violations</strong></li>
              <li><strong>Consistency</strong>: inconsistent style or vocabulary,
              brand inconsistency, etc.</li>
              <li><strong>Format</strong>: mismatches, white-spaces, tag error
                or missing, special character, numeric format, truncated,
              etc.</li>
            </ul>
          </Card>
        </span>
        {criteriaList}
        {newEntryForm}
        <div className='mb3'>
          <Button type="primary" icon="plus"
            aria-label="button"
            onClick={this.showAddNewEntryForm}>
          New review criteria entry</Button>
        </div>
      </Layout>
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
