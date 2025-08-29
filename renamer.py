import os
import sys
from datetime import datetime

def combine_txt_files(input_folder, output_folder="."):
    if not os.path.isdir(input_folder):
        print(f"Error: Input folder '{input_folder}' not found.")
        return

    os.makedirs(output_folder, exist_ok=True)
    combined_content = []
    sorted_filenames = sorted(os.listdir(input_folder))
    
    for filename in sorted_filenames:
        if filename.lower().endswith(".txt") and os.path.isfile(os.path.join(input_folder, filename)):
            filepath = os.path.join(input_folder, filename)
            try:
                with open(filepath, 'r', encoding='utf-8') as f:
                    combined_content.append(f.read())
            except Exception as e:
                print(f"Error reading '{filename}': {e}")

    if not combined_content:
        return

    date_str = datetime.now().strftime('%m-%d')
    output_filename = os.path.join(output_folder, f"{date_str}_combined.txt")

    try:
        with open(output_filename, 'a', encoding='utf-8') as outfile:
            timestamp = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
            if outfile.tell() > 0: outfile.write("\n\n")
            outfile.write('\n'.join(combined_content))
            
        print(f"Successfully appended content to '{os.path.abspath(output_filename)}'.")
        
    except Exception as e:
        print(f"Error writing to '{output_filename}': {e}")

if __name__ == "__main__":
    if len(sys.argv) < 2 or len(sys.argv) > 3:
        print("Usage: python combine_script.py <input_folder> [output_folder]")
        sys.exit(1) 
        
    input_folder = sys.argv[1]
    output_folder = sys.argv[2] if len(sys.argv) == 3 else "."
    
    combine_txt_files(input_folder, output_folder)