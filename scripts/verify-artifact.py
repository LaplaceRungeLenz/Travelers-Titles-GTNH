"""Verify the normal distributable contains only this mod and Java 8 bytecode."""
import json
import struct
import sys
import zipfile
from pathlib import Path

artifact = Path(sys.argv[1] if len(sys.argv) > 1 else "build/libs/travelerstitlesgtnh-0.1.0.jar")
with zipfile.ZipFile(artifact) as archive:
    names = archive.namelist()
    classes = [name for name in names if name.endswith(".class")]
    assert classes, "No compiled classes"
    assert not any("/qa/" in name or "RuntimeProbe" in name for name in names), "QA code in normal artifact"
    assert not any(name.endswith(".jar") for name in names), "Bundled dependency JAR"
    assert all(name.startswith("io/github/laplacerungelenz/travelerstitles/") for name in classes), "Foreign class"
    for name in classes:
        header = archive.read(name)[:8]
        magic, minor, major = struct.unpack(">IHH", header)
        assert magic == 0xCAFEBABE and major == 52, (name, major)
    mods = json.loads(archive.read("mcmod.info"))
    assert len(mods) == 1 and mods[0]["modid"] == "travelerstitlesgtnh", mods
print(f"PASS: {artifact.name}; {len(classes)} classes; Java 8; no QA or bundled dependencies")
