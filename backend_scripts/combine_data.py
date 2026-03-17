import pandas as pd
import os

DATA_DIR = r"c:\xampp\htdocs\probuilder_api\data"
OUTPUT_FILE = os.path.join(DATA_DIR, "material_prices.xlsx")

files = {
    "cement": "cement_chennai.xlsx",
    "steel": "steel_chennai.xlsx",
    "sand": "sand_chennai.xlsx",
    "bricks": "bricks_chennai.xlsx",
    "wood": "wood_chennai.xlsx",
    "aggregates": "aggregates_chennai.xlsx"
}

all_data = []

for material, filename in files.items():
    path = os.path.join(DATA_DIR, filename)
    if os.path.exists(path):
        df = pd.read_excel(path)
        df['material'] = material # Add material column
        # Ensure date format is consistent
        if 'date' in df.columns:
            df['date'] = pd.to_datetime(df['date'])
        all_data.append(df)
        print(f"Loaded {material}")
    else:
        print(f"Warning: {filename} not found")

if all_data:
    combined_df = pd.concat(all_data, ignore_index=True)
    combined_df.to_excel(OUTPUT_FILE, index=False)
    print(f"Successfully created {OUTPUT_FILE} with {len(combined_df)} rows.")
else:
    print("No data found to combine.")
