#!/usr/bin/env bash

PATH=${node.install.directory}/node:${node.install.directory}/node/npm/bin:${env.PATH}

echo "=== local node: $(node -v) ==="
echo "=== local npm: $(node ${node.install.directory}/node/npm/bin/npm-cli.js -v)"

# we have to invoke npm in such a way
node ${node.install.directory}/node/npm/bin/npm-cli.js $@
