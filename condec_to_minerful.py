#!/usr/bin/env python3
"""
Convert a ConDec-BPMN-Modeler diagram JSON to MINERful process specification JSON.

Usage:
    python3 condec_to_minerful.py input.json output.json [--name "My Process"]
    python3 condec_to_minerful.py input.json           # prints to stdout

ConDec source: https://github.com/piliotov/ConDec-BPMN-Modeler
"""

import json
import sys
import argparse

# ---------------------------------------------------------------------------
# Relation type mapping: ConDec -> MINERful template name
# ---------------------------------------------------------------------------
RELATION_MAP = {
    "resp_existence":     "RespondedExistence",
    "coexistence":        "CoExistence",
    "response":           "Response",
    "precedence":         "Precedence",
    "succession":         "Succession",
    "alt_response":       "AlternateResponse",
    "alt_precedence":     "AlternatePrecedence",
    "alt_succession":     "AlternateSuccession",
    "chain_response":     "ChainResponse",
    "chain_precedence":   "ChainPrecedence",
    "chain_succession":   "ChainSuccession",
    "resp_absence":       "NotRespondedExistence",
    "not_coexistence":    "NotCoExistence",
    "neg_response":       "NotResponse",
    "neg_precedence":     "NotPrecedence",
    "neg_succession":     "NotSuccession",
    "neg_chain_response": "NotChainResponse",
    "neg_chain_precedence": "NotChainPrecedence",
    "neg_chain_succession": "NotChainSuccession",
    # neg_alt_* have no MINERful equivalent — skipped with warning
}

# ---------------------------------------------------------------------------
# Node constraint mapping: ConDec constraint + constraintValue -> MINERful template
# constraintValue is an integer (1, 2, 3) for _n variants; None/ignored otherwise
# ---------------------------------------------------------------------------
def node_constraint_to_template(constraint, value):
    """Return MINERful template name for a node-level constraint, or None if unsupported."""
    if constraint == "init":
        return "Init"

    if constraint == "absence":
        return "Absence"

    if constraint == "absence_n":
        n = int(value) if value is not None else 1
        mapping = {1: "AtMost1", 2: "AtMost2", 3: "AtMost3"}
        t = mapping.get(n)
        if t is None:
            print(f"  [warn] absence_n with value {n}: no AtMost{n} in MINERful, skipping", file=sys.stderr)
        return t

    if constraint == "existence_n":
        n = int(value) if value is not None else 1
        mapping = {1: "AtLeast1", 2: "AtLeast2", 3: "AtLeast3"}
        t = mapping.get(n)
        if t is None:
            print(f"  [warn] existence_n with value {n}: no AtLeast{n} in MINERful, skipping", file=sys.stderr)
        return t

    if constraint == "exactly_n":
        n = int(value) if value is not None else 1
        mapping = {1: "Exactly1", 2: "Exactly2", 3: "Exactly3"}
        t = mapping.get(n)
        if t is None:
            print(f"  [warn] exactly_n with value {n}: no Exactly{n} in MINERful, skipping", file=sys.stderr)
        return t

    return None


def make_constraint(template, params):
    return {
        "template": template,
        "parameters": params,
        "support": 1.0,
        "confidence": 1.0,
        "coverage": 1.0,
    }


def convert(diagram, name=None):
    nodes = diagram.get("nodes", [])
    relations = diagram.get("relations", [])

    # Only keep activity nodes
    activities = [n for n in nodes if n.get("type") == "activity"]

    # id -> name lookup
    id_to_name = {n["id"]: n["name"] for n in activities}

    tasks = [n["name"] for n in activities]

    constraints = []

    # --- node-level (unary) constraints ---
    for node in activities:
        constraint = node.get("constraint")
        if not constraint:
            continue
        value = node.get("constraintValue")
        template = node_constraint_to_template(constraint, value)
        if template is None:
            continue
        constraints.append(make_constraint(template, [[node["name"]]]))

    # --- relation (binary) constraints ---
    for rel in relations:
        # n-ary relations use an `activities` list instead of sourceId/targetId
        if "activities" in rel and isinstance(rel["activities"], list):
            print(f"  [warn] n-ary relation {rel.get('id', '?')} ({rel.get('type')}) skipped — not supported in MINERful", file=sys.stderr)
            continue

        rel_type = rel.get("type", "")
        source_id = rel.get("sourceId")
        target_id = rel.get("targetId")

        source_name = id_to_name.get(source_id)
        target_name = id_to_name.get(target_id)

        if source_name is None or target_name is None:
            print(f"  [warn] relation {rel.get('id', '?')}: unknown node id (source={source_id}, target={target_id}), skipping", file=sys.stderr)
            continue

        template = RELATION_MAP.get(rel_type)
        if template is None:
            print(f"  [warn] relation type '{rel_type}' has no MINERful equivalent, skipping", file=sys.stderr)
            continue

        constraints.append(make_constraint(template, [[source_name], [target_name]]))

    spec_name = name or diagram.get("name", "Converted ConDec specification")

    return {
        "name": spec_name,
        "tasks": tasks,
        "constraints": constraints,
    }


def main():
    parser = argparse.ArgumentParser(description="Convert ConDec diagram JSON to MINERful spec JSON")
    parser.add_argument("input", help="ConDec diagram JSON file")
    parser.add_argument("output", nargs="?", help="Output MINERful JSON file (omit to print to stdout)")
    parser.add_argument("--name", help="Override process name in output spec")
    args = parser.parse_args()

    with open(args.input) as f:
        diagram = json.load(f)

    spec = convert(diagram, name=args.name)

    out = json.dumps(spec, indent=2, ensure_ascii=False)

    if args.output:
        with open(args.output, "w") as f:
            f.write(out + "\n")
        print(f"Written to {args.output}", file=sys.stderr)
    else:
        print(out)


if __name__ == "__main__":
    main()
