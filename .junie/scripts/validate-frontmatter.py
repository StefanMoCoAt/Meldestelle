import os, re, yaml, json
from glob import glob

try:
    import jsonschema
except ImportError:
    # GitHub Actions step will install this before running; provide friendlier message if missing
    raise SystemExit("[FM] jsonschema package not installed. Please run: pip install jsonschema pyyaml")

SCHEMA_PATH = 'docs/.frontmatter.schema.json'
FM_REGEX = re.compile(r'^---\n(.*?)\n---', re.S)

with open(SCHEMA_PATH, encoding='utf-8') as f:
    schema = json.load(f)

errors = 0
for path in glob('docs/**/*.md', recursive=True):
    # ADRs und ggf. generierte Inhalte vorerst ausnehmen (separater Rollout für FM)
    if path.startswith('docs/architecture/adr/'):
        continue
    # Skip generated or non-content files if any (none by default)
    with open(path, 'r', encoding='utf-8') as fh:
        content = fh.read()
    m = FM_REGEX.search(content)
    if not m:
        print(f"[FM] fehlt: {path}")
        errors = 1
        continue
    try:
        fm = yaml.safe_load(m.group(1)) or {}
        jsonschema.validate(fm, schema)
    except Exception as e:
        print(f"[FM] invalid in {path}: {e}")
        errors = 1

exit(errors)
