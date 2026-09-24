from pathlib import Path
import subprocess

BASE = 'fb166432226f21fd432476b4dea6dccdabd8513b'

changed = subprocess.check_output(['git', 'diff', '--name-only', BASE + '..HEAD'], text=True).splitlines()
for name in changed:
    path = Path(name)
    if not path.is_file():
        continue
    try:
        base = subprocess.check_output(['git', 'show', f'{BASE}:{name}'])
    except subprocess.CalledProcessError:
        continue
    current = path.read_bytes()
    base_lf = base.count(b'\n')
    base_crlf = base.count(b'\r\n')
    if base_lf and base_crlf * 10 >= base_lf * 9:
        normalized = current.replace(b'\r\n', b'\n').replace(b'\r', b'\n')
        restored = normalized.replace(b'\n', b'\r\n')
        if restored != current:
            path.write_bytes(restored)

for name in [
    '.github/scripts/patch_task4.py',
    '.github/scripts/patch_task5.py',
    '.github/scripts/patch_play_task7.py',
    '.github/workflows/patch-play-task4.yml',
    '.github/workflows/patch-play-task5.yml',
    '.github/workflows/patch-play-task7.yml',
]:
    path = Path(name)
    if path.exists():
        path.unlink()
