#!/bin/bash

set -e 

PYTHON_ENV_NAME=".venv"

if ! command -v python3.11 &> /dev/null; then exit 1; fi
if ! brew list swig &> /dev/null; then exit 1; fi

if [ -d "$PYTHON_ENV_NAME" ]; then
    rm -rf "$PYTHON_ENV_NAME"
fi

python3.11 -m venv "$PYTHON_ENV_NAME"
source "$PYTHON_ENV_NAME/bin/activate"
pip install --upgrade pip

cat > constraints.txt << EOL
numpy==1.25.2
opencv-python==4.6.0.66
opencv-contrib-python==4.6.0.66
EOL

PIP_MIRROR_ARGS="-i https://pypi.tuna.tsinghua.edu.cn/simple --no-cache-dir"

pip install \
    -c constraints.txt \
    "paddleocr==2.7.0" \
    "paddlepaddle==2.5.1" \
    "pandas" \
    $PIP_MIRROR_ARGS

pip freeze > requirements.txt
echo "requirements.txt has been created."
rm constraints.txt

pip list | grep -E 'numpy|paddleocr|paddlepaddle|opencv-python|pandas'