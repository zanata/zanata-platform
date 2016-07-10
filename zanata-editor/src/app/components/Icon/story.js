import React from 'react'
import { storiesOf } from '@kadira/storybook'
import Icon from '.'

const iconNames = [
  'admin', 'all', 'attach', 'block', 'chevron-down', 'chevron-down-double',
  'chevron-left', 'chevron-right', 'chevron-up', 'chevron-up-double', 'circle',
  'clock', 'code', 'comment', 'copy', 'cross', 'cross-circle', 'dashboard',
  'document', 'dot', 'download', 'edit', 'ellipsis', 'export', 'external-link',
  'filter', 'folder', 'glossary', 'help', 'history', 'import', 'inbox', 'info',
  'keyboard', 'language', 'link', 'loader', 'location', 'locked', 'logout',
  'mail', 'maintain', 'menu', 'minus', 'next', 'notification', 'plus',
  'previous', 'project', 'refresh', 'review', 'search', 'settings', 'star',
  'star-outline', 'statistics', 'suggestions', 'tick', 'tick-circle',
  'translate', 'trash', 'undo', 'unlocked', 'upload', 'user', 'users', 'version'
]

/* slightly spaced-out list item with an icon and its name */
function iconLi (name, key) {
  return <li className="u-sM-1-2" key={key}><Icon name={name}/> {name}</li>
}

/*
 * See .storybook/README.md for info on the component storybook.
 */
storiesOf('Icon', module)
  // history icon from suggestion panel
  .add('types', () => (
    <ul className="u-listHorizontal">
      {iconNames.map(iconLi)}
    </ul>
  ))
  .add('sizes', () => (
    <ul className="u-listHorizontal">
      <li className="u-sM-1-2">
        <Icon name="translate" className="Icon--xsm"/> Icon--xsm
      </li>
      <li className="u-sM-1-2">
        <Icon name="translate" className="Icon--sm"/> Icon--sm
      </li>
      <li className="u-sM-1-2">
        <Icon name="translate"/> (default)
      </li>
      <li className="u-sM-1-2">
        <Icon name="translate" className="Icon--lg"/> Icon--lg
      </li>
      <li className="u-sM-1-2">
        <Icon name="translate" className="Icon--xlg"/> Icon--xlg
      </li>
    </ul>
  ))
  .add('other styles', () => (
    <ul className="u-listHorizontal">
      <li className="u-sM-1-2"><Icon name="folder"/> (default)</li>
      <li className="u-sM-1-2">
        <Icon name="folder" className="Icon--circle"/> Icon--circle</li>
      <li className="u-sM-1-2">
        <Icon name="folder" className="Icon--stroked"/> Icon--stroked</li>
      <li className="u-sM-1-2 Link--neutral">
        <Icon name="folder" className=""/> Link--neutral (icons use
            current text color)</li>
    </ul>
  ))

// TODO add more variety of styles. See if there is a stylesheet to compare to
