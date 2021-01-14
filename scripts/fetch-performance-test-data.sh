#!/bin/bash

# This script is for fetching the input data for the full-text-search library's
# performance tests. The directories it uses are quite large, so checking them
# into this repository is impractical. Instead, we fetch them from GitHub lazily
# via this shell script.

# shellcheck disable=SC2010
if [[ $(ls -al | grep -c settings.gradle) != "1" ]]; then
  echo "You can use this script from the root of the full-text-search repository.
Current working directory: $(pwd)"
  exit 1
fi

l_systems="LSystems"
kotlin_web_site="kotlin-web-site"
kotlin="kotlin"

function print_usage_message() {
  echo "Usage: $0 [$l_systems | $kotlin_web_site | $kotlin]"
  echo "Note the size of these data sets:
 - $l_systems: small
 - $kotlin_web_site: medium
 - $kotlin: large"
}

if [[ $# != "1" ]]; then
  print_usage_message
  exit 1
fi

example_input_directories="example-input-directories"
mkdir -p $example_input_directories

function clone_input_directory() {
  output_target=$1
  repo_url=$2
  output_dir="$example_input_directories/$output_target"
  git clone "$repo_url" "$output_dir"
  rm -rf "$output_dir/.git"
}

if [[ $1 == "$l_systems" ]]; then
  clone_input_directory "$l_systems" "https://github.com/robmoore-i/LSystems"
elif [[ $1 == "$kotlin_web_site" ]]; then
  clone_input_directory "$kotlin_web_site" "https://github.com/JetBrains/kotlin-web-site"
elif [[ $1 == "$kotlin" ]]; then
  clone_input_directory "$kotlin" "https://github.com/JetBrains/kotlin"
else
  print_usage_message
  exit 1
fi
