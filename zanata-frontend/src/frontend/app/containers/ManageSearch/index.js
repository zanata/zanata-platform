import React, {Component} from 'react'
import {Button} from 'react-bootstrap'
import Helmet from 'react-helmet'
import {Page, ScrollView, View} from 'zanata-ui'

const contentViewContainerTheme = {
  base: {
    w: 'W(100%)'
  }
}

class ManageSearch extends Component {

  render () {
    return (
      <Page>
        <Helmet title='ManageSearch' />
        <ScrollView>
          <View theme={contentViewContainerTheme}>
            <div className='row clearfix' id='admin-lang'>
              <div className='row-height'>
                <div className='contentx clearfix center-block'>
                  <h2>Manage Search
                  </h2>
                  <div className='panel panel-primary'>
                    <div className='panel-heading'>
                      <h3 className='panel-title'>Current progress</h3>
                    </div>
                    <div className='panel-body'>
                      No operations are running
                    </div>
                  </div>
                  <div className='left-form tablepadding col-xs-12'>
                    <table className='table'>
                      <thead>
                        <tr>
                          <th>Table</th>
                          <th>Purge index</th>
                          <th>Reindex</th>
                          <th>Optimise</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr>
                          <td></td>
                          <td></td>
                          <td></td>
                          <td></td>
                        </tr>
                        <tr>
                          <td></td>
                          <td></td>
                          <td></td>
                          <td></td>
                        </tr>
                        <tr>
                          <td></td>
                          <td></td>
                          <td></td>
                          <td></td>
                        </tr>
                        <tr>
                          <td></td>
                          <td></td>
                          <td></td>
                          <td></td>
                        </tr>
                        <tr>
                          <td></td>
                          <td></td>
                          <td></td>
                          <td></td>
                        </tr>
                        <tr>
                          <td></td>
                          <td></td>
                          <td></td>
                          <td></td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
              <Button bsStyle='primary'>
              Perform selected actions</Button>
            </div>
          </View>
        </ScrollView>
      </Page>
    )
  }
}
export default ManageSearch
