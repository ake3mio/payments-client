SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

docker build -f "$SCRIPT_DIR/../payments-client.dockerfile" --build-arg VERSION=1.0.0 -t ake3m/payments-client:latest "$SCRIPT_DIR/.."
