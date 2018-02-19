/* global describe it expect */
import { CALL_API } from 'redux-api-middleware'
import { getUserPermissions } from './index'

const projectSlug = 'someProject'
const localeId = 'ja'

describe('api-actions', () => {
  it('generates correct API endpoint for getUserPermissions', () => {
    const apiAction = getUserPermissions(localeId, projectSlug)
    expect(apiAction[CALL_API].endpoint).toEqual(
      '/rest/user/permission/roles/project/someProject?localeId=ja'
    )
  })
})
