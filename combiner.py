import os
import sys
from datetime import datetime

def generate_unique_filename(base_path, extension):
    counter = 1
    output_filename = f"{base_path}{extension}"

    while os.path.exists(output_filename):
        counter += 1
        output_filename = f"{base_path}_{counter}{extension}"
        
    return output_filename

def combine_txt_files(input_folder, output_folder):
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
        print(f"No .txt files were found or read from '{input_folder}'. No output file created.")
        return

    date_str = datetime.now().strftime('%m-%d')
    base_name = f"{date_str}_combined"
    base_path = os.path.join(output_folder, base_name)
    
    output_filename = generate_unique_filename(base_path, ".txt")

    try:
        with open(output_filename, 'w', encoding='utf-8') as outfile:
            # Join the content of each file with a newline.
            outfile.write('\n'.join(combined_content))
        # Provide the absolute path for clarity.
        print(f"All .txt files combined into '{os.path.abspath(output_filename)}' successfully.")
    except Exception as e:
        print(f"Error writing to '{output_filename}': {e}")

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python combine_script.py <input_folder> <output_folder>")
    else:
        input_folder = sys.argv[1]
        output_folder = sys.argv[2]
        combine_txt_files(input_folder, output_folder)