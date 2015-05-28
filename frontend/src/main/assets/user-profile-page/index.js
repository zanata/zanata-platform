import React from 'react';
import RecentContributions from './lib/components/RecentContributions';
import Configs from './lib/constants/Configs';

var mountNode = document.getElementById('userMatrixRoot'),
  baseUrl = mountNode.getAttribute('data-base-url');

Configs.baseUrl = baseUrl;

React.render(<RecentContributions />, mountNode);
