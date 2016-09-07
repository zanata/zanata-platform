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
                    <table className='searchtable table'>
                      <thead>
                        <tr>
                          <th><label className='checkbox-inline'><input
                            type='checkbox' value='' />Table</label></th>
                          <th><label className='checkbox-inline'><input
                            type='checkbox' value='' />Purge index</label>
                          </th>
                          <th><label className='checkbox-inline'><input
                            type='checkbox' value='' />Reindex</label></th>
                          <th><label className='checkbox-inline'><input
                            type='checkbox' value='' />Optimise</label></th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr>
                          <td><label className='checkbox-inline'><input
                            type='checkbox' value='' />HAccount</label></td>
                          <td><label className='checkbox-inline'><input
                            type='checkbox' value='' />&nbsp;</label></td>
                          <td><label className='checkbox-inline'><input
                            type='checkbox' value='' />&nbsp;</label></td>
                          <td><label className='checkbox-inline'><input
                            type='checkbox' value='' />&nbsp;</label></td>
                        </tr>
                        <tr>
                          <td><label className='checkbox-inline'><input
                            type='checkbox' value='' />HGlossaryEntry</label>
                          </td>
                          <td><label className='checkbox-inline'><input
                            type='checkbox' value='' />&nbsp;</label></td>
                          <td><label className='checkbox-inline'><input
                            type='checkbox' value='' />&nbsp;</label></td>
                          <td><label className='checkbox-inline'><input
                            type='checkbox' value='' />&nbsp;</label></td>
                        </tr>
                        <tr>
                          <td><label className='checkbox-inline'><input
                            type='checkbox' value='' />HGlossaryTerms</label>
                          </td>
                          <td><label className='checkbox-inline'><input
                            type='checkbox' value='' />&nbsp;</label></td>
                          <td><label className='checkbox-inline'><input
                            type='checkbox' value='' />&nbsp;</label></td>
                          <td><label className='checkbox-inline'><input
                            type='checkbox' value='' />&nbsp;</label></td>
                        </tr>
                        <tr>
                          <td><label className='checkbox-inline'><input
                            type='checkbox' value='' />HProject</label></td>
                          <td><label className='checkbox-inline'><input
                            type='checkbox' value='' />&nbsp;</label></td>
                          <td><label className='checkbox-inline'><input
                            type='checkbox' value='' />&nbsp;</label></td>
                          <td><label className='checkbox-inline'><input
                            type='checkbox' value='' />&nbsp;</label></td>
                        </tr>
                        <tr>
                          <td><label className='checkbox-inline'><input
                            type='checkbox' value='' />HProjectIteration
                          </label></td>
                          <td><label className='checkbox-inline'><input
                            type='checkbox' value='' />&nbsp;</label></td>
                          <td><label className='checkbox-inline'><input
                            type='checkbox' value='' />&nbsp;</label></td>
                          <td><label className='checkbox-inline'><input
                            type='checkbox' value='' />&nbsp;</label></td>
                        </tr>
                        <tr>
                          <td><label className='checkbox-inline'><input
                            type='checkbox' value='' />TransMemoryUnit
                          </label></td>
                          <td><label className='checkbox-inline'><input
                            type='checkbox' value='' />&nbsp;</label></td>
                          <td><label className='checkbox-inline'><input
                            type='checkbox' value='' />&nbsp;</label></td>
                          <td><label className='checkbox-inline'><input
                            type='checkbox' value='' />&nbsp;</label></td>
                        </tr>
                        <tr>
                          <td><label className='checkbox-inline'><input
                            type='checkbox' value='' />HTextFlowTarget</label>
                          </td>
                          <td><label className='checkbox-inline'><input
                            type='checkbox' value='' />&nbsp;</label></td>
                          <td><label className='checkbox-inline'><input
                            type='checkbox' value='' />&nbsp;</label></td>
                          <td><label className='checkbox-inline'><input
                            type='checkbox' value='' />&nbsp;</label></td>
                        </tr>
                      </tbody>
                    </table>
                    <div>
                      <Button bsStyle='default'>
                      Clear selected</Button>
                      <div className='pull-right'><Button bsStyle='primary'>
                      Perform selected actions</Button></div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </View>
        </ScrollView>
      </Page>
    )
  }
}
export default ManageSearch
