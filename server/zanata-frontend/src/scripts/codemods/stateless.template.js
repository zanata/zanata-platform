import React, { PropTypes } from 'react'

/**
 * TODO add a concise description of this component
 */
const COMPONENT_NAME_HERE = ({
  fancy,
  noise = 'moo',
  onClick
}) => {
  return (
    <div style={fancy
      ? { color: 'rebeccapurple', fontWeight: 'bold' }
      : {}}>
      <p>TODO make this component</p>
      <button onClick={onClick}>Make Click Event!</button>
      <p>The cow says {noise}</p>
    </div>
  )
}

COMPONENT_NAME_HERE.propTypes = {
  /* Whether it should look fancy */
  fancy: PropTypes.bool.isRequired,
  /* What the cow says */
  noise: PropTypes.string,
  /* arguments: clickCount, sound */
  onClick: PropTypes.func.isRequired
}

export default COMPONENT_NAME_HERE
