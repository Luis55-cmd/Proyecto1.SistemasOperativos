#!/usr/bin/env python3
"""Comprueba la trazabilidad documental; no valida código ni dominio del equipo."""

import hashlib
import json
import re
from collections import Counter
from pathlib import Path
from urllib.parse import unquote


def main():
    root = Path(__file__).resolve().parent
    data = json.loads((root / "mapa.json").read_text())
    source = root / data["source"]
    original = source.read_bytes()
    assert hashlib.sha256(original).hexdigest() == data["source_sha256"], (
        "Cambió el checklist respecto de la versión indexada; revisar antes de actualizar el hash."
    )
    expected = re.findall(r"^- \[ \] \*\*([A-Z]+-\d+)", original.decode(), re.M)
    assert len(expected) == len(set(expected)), "IDs repetidos en la fuente"
    packages = data["packages"]
    transversals = data["transversals"]
    all_items = packages + transversals
    item_ids = [item["id"] for item in all_items]
    assert len(item_ids) == len(set(item_ids)), "Paquetes o transversales repetidos"
    assigned = [rid for item in all_items for rid in item["requirements"]]
    counts = Counter(assigned)
    assert set(assigned) == set(expected), {
        "faltantes": sorted(set(expected) - set(assigned)),
        "desconocidos": sorted(set(assigned) - set(expected)),
    }
    assert all(n == 1 for n in counts.values()), "Asignación principal duplicada"

    index = {item["id"]: item for item in packages}
    visiting, visited = set(), set()

    def visit(pid):
        assert pid in index, f"Dependencia desconocida: {pid}"
        assert pid not in visiting, f"Ciclo en dependencias: {pid}"
        if pid in visited:
            return
        visiting.add(pid)
        for dependency in index[pid]["dependencies"]:
            visit(dependency)
        visiting.remove(pid)
        visited.add(pid)

    for package in packages:
        visit(package["id"])
        detail = root / "paquetes" / f"{package['id']}.md"
        assert detail.exists(), f"Falta detalle: {detail.name}"
        table_ids = re.findall(r"^\| \[([A-Z]+-\d+)\]", detail.read_text(), re.M)
        assert table_ids == package["requirements"], f"Detalle desactualizado: {detail.name}"
    transversal_ids = re.findall(
        r"^- \*\*\[([A-Z]+-\d+)\]", (root / "transversales.md").read_text(), re.M
    )
    assert transversal_ids == [rid for item in transversals for rid in item["requirements"]]
    trace = re.findall(r"^\| ([A-Z]+-\d+) \| \[([^]]+)\]", (root / "trazabilidad.md").read_text(), re.M)
    owners = {rid: item["id"] for item in all_items for rid in item["requirements"]}
    assert len(trace) == len(expected) and dict(trace) == owners, "Tabla inversa desactualizada"

    main_map = (root / "README.md").read_text()
    rendered_edges = set(re.findall(r"^    (p\d+) --> (p\d+)$", main_map, re.M))

    def reachable(start, end):
        pending, explored = [start], set()
        while pending:
            current = pending.pop()
            if current == end:
                return True
            if current in explored:
                continue
            explored.add(current)
            pending.extend(b for a, b in rendered_edges if a == current)
        return False

    for package in packages:
        assert f'{package["id"].lower()}["' in main_map, "Falta nodo principal"
        for dependency in package["dependencies"]:
            assert reachable(dependency.lower(), package["id"].lower()), "Falta dependencia visual"
    for before, after in rendered_edges:
        assert before.upper() in index and after.upper() in index, "Nodo desconocido en flecha"
        assert before.upper() in index[after.upper()]["dependencies"], "Flecha no respaldada por datos"

    link_count = 0
    for path in root.rglob("*.md"):
        for link in re.findall(r"\]\(([^)]+)\)", path.read_text()):
            if "://" in link:
                continue
            file_name, _, anchor = unquote(link).partition("#")
            target = path.parent / file_name if file_name else path
            assert target.exists(), f"Enlace roto en {path.name}: {link}"
            if anchor:
                headings = re.findall(r"^#+ (.*)$", target.read_text(), re.M)
                slugs = [re.sub(r"[^\w\- ]", "", h.lower()).replace(" ", "-") for h in headings]
                assert anchor in slugs, f"Sección inexistente: {link}"
            link_count += 1
    print(f"OK: {len(packages)} paquetes, {len(transversals)} grupos transversales.")
    print(f"OK: {len(expected)} casillas con una ubicación principal cada una.")
    print(f"OK: dependencias sin ciclos; mapa visual y detalles consistentes; {link_count} enlaces.")
    print("OK: checklist intacto. Validación documental, no de implementación ni de renderizado Mermaid.")


if __name__ == "__main__":
    main()
