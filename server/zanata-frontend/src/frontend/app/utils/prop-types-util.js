import PropTypes from 'prop-types'

export const ProjectType = PropTypes.shape({
  id: PropTypes.string.isRequired,
  status: PropTypes.string.isRequired,
  title: PropTypes.string.isRequired,
  versions: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.string.isRequired,
    status: PropTypes.string.isRequired
  })).isRequired
})

export const LocaleType = PropTypes.shape({
  displayName: PropTypes.string.isRequired,
  localeId: PropTypes.string.isRequired,
  nativeName: PropTypes.string.isRequired
})

export const FromProjectVersionType = PropTypes.shape({
  projectSlug: PropTypes.string.isRequired,
  version: PropTypes.shape({
    id: PropTypes.string.isRequired,
    status: PropTypes.string.isRequired
  }).isRequired
})
