/* eslint-disable */
// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import VersionProgress from '../../components/Sidebar/VersionProgress'
import {Icon} from '../../components'
import Pagination from 'antd/lib/pagination'
import 'antd/lib/pagination/style/css'

const counts = {
  total: 20,
  approved: 3,
  translated: 8,
  needswork: 4,
  rejected: 1,
  untranslated: 4
}

const members = (<a href=''>Japanese</a>)
const memberCount = '23 members'

const lastActive =  <span className='txt-muted'>
    <Icon name='clock' className='s1 v-sub' /> Last modified today
  </span>

const tableEntry = (
  <React.Fragment>
    <tr className='row-combined mb4'>
      <td className='w-30'>{members}</td>
      <td className='w-20'>{memberCount}</td>
      <td className='fr'>{lastActive}</td>
    </tr>
  </React.Fragment>
)

class GroupsTable extends Component {
  render() {
    return (
      <React.Fragment>
        <span className='fr mb2'>
          <Pagination size='small' defaultCurrent={1} total={12} />
        </span>
        <table className='w-100'>
          <tbody>
          {tableEntry}
          {tableEntry}
          {tableEntry}
          {tableEntry}
          </tbody>
        </table>
      </React.Fragment>
    )
  }
}

export default GroupsTable
