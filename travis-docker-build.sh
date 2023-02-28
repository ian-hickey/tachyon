#!/bin/bash

# get tachyon version
LUCEE_VERSION=$(<buildVersionNumber.txt)

# build the travis request body
function build_request {
cat <<EOF
{
  "request": {
    "message": "Automated build for version ${LUCEE_VERSION}",
    "branch":"travis-build-matrix",
    "config": {
      "merge_mode": "deep_merge",
      "env": {
        "global": {
          "LUCEE_VERSION": "${LUCEE_VERSION}"
        }
      }
    }
  }
}
EOF
}
REQUEST_BODY=$(build_request)

# trigger the tachyon-dockerfiles travis job for this tachyon version
curl --no-progress-meter -S -m 30 -X POST \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -H "Travis-API-Version: 3" \
  -H "Authorization: token ${TRAVIS_TOKEN}" \
  -d "${REQUEST_BODY}" \
  https://api.travis-ci.com/repo/tachyon%2Ftachyon-dockerfiles/requests
