if [[ $(pwd | xargs basename) != "LSystems" ]]; then
  echo "Not building docker image.
This script should be executed from the repository's root directory, 'LSystems'."
  exit 1
fi

docker build -f docker/Dockerfile . -t lsystems
