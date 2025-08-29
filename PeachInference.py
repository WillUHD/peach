print(
    "\n  ██████╗ ███████╗ █████╗  ██████╗██╗  ██╗ \n " +
    " ██╔══██╗██╔════╝██╔══██╗██╔════╝██║  ██║ \n " +
    " ██████╔╝PeachInference ║██║ v7  ███████║ \n " +
    " ██╔═══╝ ██╔══╝  ██╔══██║██║     ██╔══██║ \n " +
    " by║     WillUHD╗██║  ██║╚██████╗██║  ██║ \n " +
    " ╚═╝     ╚══════╝╚═╝  ╚═╝ ╚═════╝╚═╝  ╚═╝ \n " +
    "   Peach is starting up... Please wait \n"
)

import os
import sys
import time
import logging

inputFolder, outputFolder = sys.argv[1], sys.argv[2]
det_model_dir = "models/ch_PP-OCRv4_det_infer"
rec_model_dir = "models/ch_PP-OCRv4_rec_infer"

logging.disable(logging.CRITICAL) 

print("Reading images...")
try:
    valid_extensions = ('.png', '.jpg', '.jpeg')
    images = [f for f in os.listdir(inputFolder) if not f.startswith('.') and f.lower().endswith(valid_extensions)]
    
    if not images:
        print(f"No valid image files detected in '{inputFolder}'. Self destructing.")
        sys.exit(0)
    print(f"{len(images)} image(s) detected. Peach is ready to analyze.")
except FileNotFoundError:
    print(f"Input folder '{inputFolder}' not found. Self destructing.")
    sys.exit(1)

print("Getting OCR library...")
from paddleocr import PaddleOCR

print("Setting up OCR...")
ocr = PaddleOCR(use_textline_orientation=False,
                lang='ch',   
                det_model_dir=det_model_dir,
                rec_model_dir=rec_model_dir 
                )

print("Peach is ready to analyze. ")
processingTime = 0.0
for idx, fname in enumerate(images, start=1):
    print(f"\nCurrent PeachTask: [{idx}/{len(images)}] Processing {fname}")
    img_path = os.path.join(inputFolder, fname)

    if not os.path.exists(img_path):
        print(f"Warning: Image file '{img_path}' not found. Skipping.")
        continue 

    start = time.time()
    result = ocr.ocr(img_path) 
    elapsed = time.time() - start
    processingTime += elapsed

    base_fname, _ = os.path.splitext(fname)
    out_txt_path = os.path.join(outputFolder, base_fname + '_ocr.txt')
    
    os.makedirs(outputFolder, exist_ok=True)

    extracted_text_lines = []
    if result and result[0]: 
        for line_info in result[0]:
            text = line_info[1][0] 
            extracted_text_lines.append(text)
    
    with open(out_txt_path, 'w', encoding='utf-8') as f:
        f.write(' '.join(extracted_text_lines)) 

    print(f"Finished in {elapsed:.2f}s")

avg = processingTime / len(images) if images else 0
print(f"\nAverage inference time per image: {avg:.2f}s")
print("PeachInference complete.")