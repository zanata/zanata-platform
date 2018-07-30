// @ts-nocheck
import React from 'react'
import DatePicker from 'antd/lib/date-picker'
import 'antd/lib/date-picker/style/css'

/**
 * User profile statistics root page
 */
class RecentContributions extends React.Component {

  render () {
    const { RangePicker } = DatePicker

    /* eslint-disable react/jsx-no-bind */
    return (
      <div className='matrixHeading' id='userProfile-matrix'>
        <div className='u-flexCenter'>
          <h2 className='userProfile-recentContributions'>
          Recent Contributions</h2>
          <div className='dateRange-container'>
            <RangePicker
              dateRender={(current) => {
                const style = {}
                if (current.date() === 1) {
                  style.border = '1px solid #1890ff'
                  style.borderRadius = '50%'
                }
                return (
                  <div className="ant-calendar-date" style={style}>
                    {current.date()}
                  </div>
                )
              }}
            />
          </div>
        </div>
      </div>
    )
    /* eslint-enable react/jsx-no-bind */
  }
}

export default RecentContributions
