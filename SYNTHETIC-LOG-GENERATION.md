# Synthetic Log Generation with MINERful

## What this tool does

MINERful's log maker generates synthetic XES event logs from **Declare process specifications** (JSON or Declare Map format). Structure level is entirely determined by how many constraints — and how tight — you put in the spec.

---

## Java requirement

The pre-built `MINERful.jar` was compiled for Java 18+. System default may be Java 17. Use Java 21:

```bash
/usr/lib/jvm/java-21-openjdk/bin/java
```

---

## Base command

```bash
JAVA=/usr/lib/jvm/java-21-openjdk/bin/java
CP="MINERful.jar:bin:$(ls lib/*.jar | tr '\n' ':')"

$JAVA -Xmx4G -cp "$CP" minerful.MinerFulLogMakerStarter \
  -iSF <spec.json> \
  -oLF <output.xes> \
  -oLL <conforming trace count> \
  -oLV <violating trace count> \
  -iVC <violating-constraints.json> \
  -oLm <min events per trace> \
  -oLM <max events per trace>
```

### All flags

| Flag | Long name | Meaning | Default |
|------|-----------|---------|---------|
| `-iSF` | `--input-specification-file` | Path to process spec JSON | (required) |
| `-iSE` | `--input-specification-encoding` | `json` or `declare-map` | `json` |
| `-oLF` | `--out-log-file` | Output XES path | (required) |
| `-oLE` | `--out-log-encoding` | `xes`, `mxml`, or `strings` | `xes` |
| `-oLL` | `--size` | Number of conforming traces | 100 |
| `-oLV` | `--sizeviol` | Number of violating (deviant) traces | 0 |
| `-iVC` | `--viol-const-file` | Constraints to violate in deviant traces | (none) |
| `-oLm` | `--minlen` | Min events per trace | 0 |
| `-oLM` | `--maxlen` | Max events per trace | 100 |
| `-d`   | `--debug` | Debug level: `none/info/debug/trace/all` | `info` |

---

## Structure levels — the core concept

Structure level = how many constraints + how strict they are. More constraints and stricter templates → more structured behavior.

### Constraint templates by strictness

**Existence (unary — apply to one activity):**
| Template | Meaning |
|----------|---------|
| `Init` | Activity must be first in trace |
| `End` | Activity must be last in trace |
| `AtLeast1` / `AtLeast2` / `AtLeast3` | Activity occurs ≥ N times |
| `AtMost1` / `AtMost2` / `AtMost3` | Activity occurs ≤ N times |
| `Exactly1` / `Exactly2` / `Exactly3` | Activity occurs exactly N times |
| `Absence` | Activity never occurs |

**Relation (binary — apply to pairs):**
| Template | Strictness | Meaning |
|----------|-----------|---------|
| `RespondedExistence` | loose | if A occurs, B must occur somewhere (or vice versa) |
| `CoExistence` | loose | A and B either both occur or both absent |
| `Choice` | loose | at least one of A or B occurs |
| `ExclusiveChoice` | loose | exactly one of A or B occurs |
| `Response` | medium | every A is eventually followed by B |
| `Precedence` | medium | B can only occur if A occurred before it |
| `Succession` | medium | Response + Precedence combined |
| `AlternateResponse` | stricter | every A followed by B before next A |
| `AlternatePrecedence` | stricter | every B preceded by A with no other B between |
| `AlternateSuccession` | stricter | AlternateResponse + AlternatePrecedence |
| `ChainResponse` | strict | A must be immediately followed by B |
| `ChainPrecedence` | strict | B must be immediately preceded by A |
| `ChainSuccession` | strict | A immediately followed by B AND B immediately preceded by A |

**Negative relations:**
| Template | Meaning |
|----------|---------|
| `NotCoExistence` | A and B cannot both appear in same trace |
| `NotResponse` | A is never followed by B |
| `NotPrecedence` | B is never preceded by A |
| `NotChainResponse` | A is never immediately followed by B |
| `NotChainPrecedence` | B is never immediately preceded by A |
| `NotSuccession` | NotResponse + NotPrecedence |
| `NotChainSuccession` | NotChainResponse + NotChainPrecedence |
| `NotRespondedExistence` | if A occurs, B must not occur |

---

## Structure levels in practice

### Level 1 — Structured
Many constraints, strict templates (`ChainResponse`, `ChainPrecedence`). Activities fire in a near-fixed sequence.

**Spec:** `specifications/loan-structured.json`
```bash
$JAVA -Xmx4G -cp "$CP" minerful.MinerFulLogMakerStarter \
  -iSF specifications/loan-structured.json \
  -oLF output-structured.xes \
  -oLL 100 -oLm 5 -oLM 10
```

### Level 2 — Semi-structured
Moderate constraints, looser templates (`Precedence`, `Response`, `RespondedExistence`). Order partially enforced, some freedom in between.

**Spec:** `specifications/loan-semi-structured.json`
```bash
$JAVA -Xmx4G -cp "$CP" minerful.MinerFulLogMakerStarter \
  -iSF specifications/loan-semi-structured.json \
  -oLF output-semi.xes \
  -oLL 100 -oLm 3 -oLM 15
```

### Level 3 — Loosely structured
Few constraints, only the non-negotiable ones (Init, a few Precedence, ExclusiveChoice). Activities appear in almost any order.

**Spec:** `specifications/loan-loosely-structured.json`
```bash
$JAVA -Xmx4G -cp "$CP" minerful.MinerFulLogMakerStarter \
  -iSF specifications/loan-loosely-structured.json \
  -oLF output-loose.xes \
  -oLL 100 -oLm 2 -oLM 20
```

### Level 4 — Unstructured
Near-empty spec — just activity alphabet + minimal constraints to prevent obvious nonsense (e.g. both Approve and Reject in same trace). Traces are essentially random walks over the activity set.

**Spec:** `specifications/loan-unstructured.json`
```bash
$JAVA -Xmx4G -cp "$CP" minerful.MinerFulLogMakerStarter \
  -iSF specifications/loan-unstructured.json \
  -oLF output-unstructured.xes \
  -oLL 100 -oLm 1 -oLM 25
```

---

## Adding noise — violating (deviant) traces

Use `-oLV` to mix in traces that deliberately break specific constraints. The `-iVC` file lists the constraints to violate. These deviant traces are labelled in the XES output.

**Example:** 80 conforming + 20 deviant traces, where deviant traces break disbursement-before-approval and approve+reject coexistence:

```bash
$JAVA -Xmx4G -cp "$CP" minerful.MinerFulLogMakerStarter \
  -iSF specifications/loan-semi-structured.json \
  -iVC specifications/loan-violating-constraints.json \
  -oLF output-mixed.xes \
  -oLL 80 -oLV 20 -oLm 3 -oLM 15
```

**Violating constraints file:** `specifications/loan-violating-constraints.json`

Note: `-oLV` ≤ `-oLL` (can't have more violating traces than total traces).

---

## Spec JSON format

```json
{
  "name": "Human-readable name",
  "tasks": ["Activity A", "Activity B", "Activity C"],
  "constraints": [
    {
      "template": "Precedence",
      "parameters": [["Activity A"], ["Activity B"]],
      "support": 1.0,
      "confidence": 1.0,
      "coverage": 1.0
    }
  ]
}
```

- `parameters`: array of arrays — `[["source"], ["target"]]` for binary; `[["activity"]]` for unary
- `support` / `confidence` / `coverage`: set to `1.0` for hard constraints; lower values are metadata from mining, not enforced during generation

---

## Example specs included

| File | Level | Description |
|------|-------|-------------|
| `specifications/loan-structured.json` | Structured | Loan process with strict chain ordering |
| `specifications/loan-semi-structured.json` | Semi-structured | Loan process with eventual ordering, some freedom |
| `specifications/loan-loosely-structured.json` | Loosely structured | Only critical constraints kept |
| `specifications/loan-unstructured.json` | Unstructured | Activity alphabet only, near-random traces |
| `specifications/loan-violating-constraints.json` | Violations | Constraints to break for deviant traces |
| `specifications/uni-declarative-specification.json` | Mixed | University admission process (original example) |

---

## Adapting for your own domain

1. List your activity alphabet in `tasks`
2. Start with no constraints — this is your unstructured baseline
3. Add `Init` / `End` for mandatory start/end activities
4. Add `Precedence` / `Response` pairs for the ordering rules you care about
5. Add `ChainResponse` / `ChainPrecedence` for strict immediate-next rules
6. Add `NotCoExistence` for mutually exclusive outcomes
7. Add `AtMost1` for activities that should not repeat
8. Test: run with `-oLL 10` and inspect the XES output to verify behavior
9. Create a separate violating-constraints JSON with the rules you want to stress-test

---

## Output format

XES (default) is the standard for process mining tools (ProM, PM4Py, Disco, Celonis). Each trace gets a `concept:name` label and each event gets activity name + lifecycle + timestamp.

To use with PM4Py:
```python
import pm4py
log = pm4py.read_xes("output.xes")
```
