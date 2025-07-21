print(
"\n  ██████╗ ███████╗ █████╗  ██████╗██╗  ██╗ \n " + 
" ██╔══██╗██╔════╝██╔══██╗██╔════╝██║  ██║ \n " + 
" ██████╔╝PeachInference ║██║ v1  ███████║ \n " + 
" ██╔═══╝ ██╔══╝  ██╔══██║██║     ██╔══██║ \n " + 
" by║     WillUHD╗██║  ██║╚██████╗██║  ██║ \n " + 
" ╚═╝     ╚══════╝╚═╝  ╚═╝ ╚═════╝╚═╝  ╚═╝ \n " + 
"   Peach is starting up... Please wait \n")

print("Importing libraries... ")
import os
import time
from PIL import Image
import easyocr 

print("Setting up PeachOCR...")
ocr_reader = easyocr.Reader(['ch_sim', 'en'], gpu=True, verbose=False)

print("Initiating objects...")
processingTime = 0
curImg = 1
outputCSV = "orders.csv"
inputFolder = "inference"
outputFolder = "txt"
scale = 2 # scale factor for downscaling the image resolution for faster inference 
headers = [
    "orderID",
    "username",
    "itemID",
    "orderTime",
    "orderCount",
    "orderPrice",
    "deliveryID",
    "contactID",
    "address"
]

print("Reading images...")
images = len(os.listdir(inputFolder))
print(str(images) + " image(s) detected. Peach is ready to analyze. ")

# loops for each image in /inference
for filename in os.listdir(inputFolder):
    print("\nCurrent PeachTask: [" + str(curImg) + "/" + str(images) + "]")

    # declare io paths
    inputPath = os.path.join(inputFolder, filename)
    outPath = os.path.join(outputFolder, os.path.splitext(filename)[0] + '.txt')
    startTime = time.time()

    img = Image.open(inputPath)
    img = img.resize((round(img.width / scale), round(img.height / scale))) 
    img.save(inputPath)
    
    print("Starting PeachPrediction OCR... ")
    easyocr_results = ocr_reader.readtext(inputPath)

    thisTime = (time.time() - startTime) 
    processingTime += thisTime
    
    rec_texts_list = [text for (bbox, text, prob) in easyocr_results]
    
    if rec_texts_list: 
        output_text = " ".join(rec_texts_list)
        with open(outPath, 'w', encoding='utf-8') as f:
            f.write(output_text)
    
    os.remove(inputPath) 
    print(f"PeachPrediction for {inputPath} finished in {thisTime:.2f}s.") 
    curImg += 1

print(f"\nAverage inference time per image: {(processingTime / images):.2f} seconds. \n")
print("PeachInference has analyzed all images. Converting to CSV with PeachExport... ")