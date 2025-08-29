#!/bin/zsh

inputFolder="inference"
detectionFolder="output"
csvName="output"
xlsxName="output_xlsx"
combinedFolder="combined"
venvName="./.venv"

./.venv/bin/python ./renamer.py $inputFolder

if [[ -d "$inputFolder" ]]; then
  chmod -R +rwx "$inputFolder"
fi

$venvName/bin/python ./PeachInference.py $inputFolder $detectionFolder
java PeachExport $detectionFolder $csvName
$venvName/bin/python ./csv2xlsx.py $csvName $xlsxName
$venvName/bin/python ./combiner.py $detectionFolder $combinedFolder
