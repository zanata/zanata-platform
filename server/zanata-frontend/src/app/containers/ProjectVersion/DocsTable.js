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

const doc = (<a href=''>Readme.txt</a>)
const percentage = (<span className='txt-success'><span className='f4'>60%</span> translated</span>)
const wordsleft = '200 words left'
const lastModified =  <span className='txt-muted'>
    <Icon name='clock' className='s1 v-sub' /> Last modified today
  </span>

const tableEntry = (
  <React.Fragment>
    <tr className='row1'>
      <td className='w-30 ellipsis'>{doc}</td>
      <td className='w-20'>{percentage}</td>
      <td className='w-20'>{wordsleft}</td>
      <td className='fr'>{lastModified}</td>
    </tr>
    <tr className = 'mb4 row2'>
      <td colSpan = "4" >
        <VersionProgress counts = {counts} />
      </td>
    </tr>
  </React.Fragment>
)

class LanguagesTable extends Component {
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

export default LanguagesTable
