var React = require('react')
var Sidebar = require('react-sidebar')

var ESidebar = React.createClass({

  componentWillMount: function () {

  },

  componentWillUnmount: function () {
  },

  mediaQueryChanged: function () {
    this.setState({sidebarDocked: this.state.mql.matches})
  },

  render: function () {
    var sidebarContent = <b>Sidebar content</b>

    return (
      <Sidebar sidebar={sidebarContent}
        open={this.state.sidebarOpen}
        docked={this.state.sidebarDocked}
        onSetOpen={this.onSetSidebarOpen}>
        <b>Main content</b>
      </Sidebar>
    )
  }
})

module.exports = ESidebar
