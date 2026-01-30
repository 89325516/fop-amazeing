import os
import re

def find_chinese_in_files(directory):
    chinese_re = re.compile(r'[\u4e00-\u9fff]')
    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith('.java'):
                path = os.path.join(root, file)
                try:
                    with open(path, 'r', encoding='utf-8') as f:
                        for line_num, line in enumerate(f, 1):
                            if chinese_re.search(line):
                                print(f"{path}:{line_num}: {line.strip()}")
                except Exception as e:
                    pass

if __name__ == "__main__":
    find_chinese_in_files('/Users/papersiii/fopws2526projectfop-amazeing/core/src/de/tum/cit/fop/maze')
