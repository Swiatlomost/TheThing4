def grep_logcat(pattern: str):

import subprocess
import sys
import os

def grep_logcat(pattern: str):
    adb_cmd = 'adb'
    # Spróbuj znaleźć adb.exe w platform-tools jeśli nie jest w PATH
    if not shutil.which('adb'):
        user = os.environ.get('USERNAME') or os.environ.get('USER')
        default_adb = os.path.expandvars(r'C:\Users\%USERNAME%\AppData\Local\Android\Sdk\platform-tools\adb.exe')
        if os.path.exists(default_adb):
            adb_cmd = default_adb
    try:
        result = subprocess.run([
            adb_cmd, 'logcat', '-d'
        ], capture_output=True, text=True, check=True)
        lines = result.stdout.splitlines()
        filtered = [line for line in lines if pattern.lower() in line.lower()]
        for line in filtered:
            print(line)
        if not filtered:
            print(f'Brak wyników dla wzorca: {pattern}')
    except Exception as e:
        print(f'Błąd podczas pobierania logcat: {e}')

if __name__ == '__main__':
    if len(sys.argv) < 2:
        print('Użycie: python grep_logcat.py <wzorzec>')
        sys.exit(1)
    grep_logcat(sys.argv[1])
