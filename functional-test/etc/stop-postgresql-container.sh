#!/bin/bash

# =============================================
# Parameters:
# 1. Container name (to shut down)
# =============================================

echo "Stopping Postgresql container $1"
docker stop $1