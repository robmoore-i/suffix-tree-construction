# shellcheck disable=SC2010
if [[ $(ls -al | grep -c settings.gradle) != "1" ]]; then
  echo "You can use this script from the root of the full-text-search repository.
Current working directory: $(pwd)"
  exit 1
fi

project_dir_name=$(pwd | xargs basename)
pushd ..
zip -r "${project_dir_name}/full-text-search.zip" "$project_dir_name" \
  -x '*.git*' \
  -x '*example-input-directories*' \
  -x '*.idea*' \
  -x '*build*' \
  -x '*.gradle*'

popd || exit
