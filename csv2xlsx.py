print("PeachConverter v1 by WillUHD is starting up...")

import pandas as pd
import os
import sys
import time

csv_file_name = sys.argv[1] + ".csv"
xlsx_file_name = sys.argv[2] + ".xlsx"

script_dir = os.path.dirname(os.path.abspath(__file__))
csv_file_path = os.path.join(script_dir, csv_file_name)
xlsx_file_path = os.path.join(script_dir, xlsx_file_name)

print(f"Ready to convert. ")
print(f"Peach detected the input file as: {csv_file_path}")

try:
    startTime = time.time()
    df = pd.read_csv(csv_file_path, encoding='utf-8')
    print("CSV file read successfully.")
    df.to_excel(xlsx_file_path, index=False)
    thisTime = time.time() - startTime
    print(f"Successfully converted to {xlsx_file_name} in " + str(thisTime) + "s. ")

except FileNotFoundError:
    print(f"Error: The file '{csv_file_name}' was not found in the same directory as the script.")
    print("Please make sure the CSV file is in the correct location and the filename is correct.")

except Exception as e:
    print(f"An unexpected error occurred: {e}")