# P2P Search Simulator

Simulador de busca distribuída em redes P2P não-estruturadas com 4 estratégias, cache inteligente e visualização em tempo real.

## Stack

- **Java 17+** (LTS)
- **Maven 3.6+**
- **JUnit 5** (98 testes)
- **JGraphT** (grafos)
- **GraphStream** (visualização)

## Quick Start

```bash
# Build
mvn clean package

# Run GUI
java -jar target/p2p-simulator.jar

# Run tests (98 tests)
mvn test
```

### Requirements

```bash
java -version  # Must be 17.x or higher
mvn -version   # Maven 3.6+
```

> ⚠️ **Java 17+ required**. Download: [Adoptium OpenJDK](https://adoptium.net/)

## Search Strategies

| Strategy | Messages | Success Rate | Cache | Best For |
|----------|----------|--------------|-------|----------|
| **Flooding** | ~20 | 100% ✅ | No | Guaranteed find |
| **Random Walk** | ~12 | 50% ⚠️ | No | Low traffic |
| **Informed Flooding** | 20→1* | 100% ✅ | Yes | Repeated searches |
| **Informed Random Walk** | 12→1* | 60% ⚠️ | Yes | Balanced |

\* With cache hit (95% reduction)

### How It Works

**Flooding**: Broadcasts to all neighbors (except sender). Guarantees 100% success but high traffic.

**Random Walk**: Randomly picks one neighbor. 80% less messages but ~50% success rate (stochastic).

**Informed Strategies**: Check cache first. If hit → direct send (1 message). If miss → run base strategy.

**Cache Propagation**: When resource found, RESPONSE travels back updating cache on all nodes in path.

## Configuration

Edit `src/main/resources/config.json`:

```json
{
  "num_nodes": 12,
  "min_neighbors": 2,
  "max_neighbors": 4,
  "resources": {
    "n1": ["fileA", "fileB"],
    "n2": ["fileC"]
  },
  "edges": [["n1", "n2"], ["n1", "n3"]]
}
```

**Validation (automatic):**
- Graph connectivity (no isolated subnetworks)
- Node degree bounds: `min_neighbors ≤ degree ≤ `max_neighbors`
- Every node has ≥1 resource
- No self-loops

## Architecture

```
src/main/java/p2p/search/simulator/
├── model/
│   ├── Message.java           # QUERY/RESPONSE with TTL
│   ├── Node.java              # Peer (resources, cache, neighbors)
│   └── NetworkConfig.java     # JSON config DTO
├── strategy/
│   ├── SearchStrategy.java    # Interface (Strategy Pattern)
│   ├── FloodingStrategy.java
│   ├── RandomWalkStrategy.java
│   ├── InformedFloodingStrategy.java
│   └── InformedRandomWalkStrategy.java
├── simulation/
│   └── SimulationManager.java # Simulation engine
├── topology/
│   └── NetworkTopology.java   # Graph + validations (JGraphT)
├── visualization/
│   └── NetworkVisualizer.java # GraphStream wrapper
└── ui/
    └── SimulatorGUI.java      # Swing GUI

src/test/java/
├── strategy/                   # Strategy tests
├── cache/                      # Cache propagation tests
├── ttl/                        # TTL behavior tests
└── metrics/                    # Metrics validation tests
```

## Features

### Core Mechanics
- **TTL (Time-To-Live)**: Decrements per hop, prevents infinite loops
- **Anti-Echo**: Nodes never send back to sender (reduces ~20% messages)
- **Cache System**: RESPONSE messages update cache in reverse path
- **Message Deduplication**: `(messageId, nodeId)` prevents duplicate processing

### GUI Features
- **Speed Control**: 0ms (instant) to 2s (slow motion)
- **Replay**: Re-run last search with same parameters
- **Real-time Stats**: Messages, nodes visited, hops, coverage, execution time
- **Visual Feedback**: 
  - 🔵 Blue = Source node
  - 🟠 Orange = Processing
  - 🟢 Green = Resource found
  - 🔴 Red = Active edge (message in transit)

### Metrics Collected
- Total messages sent
- Nodes visited (coverage %)
- Number of hops
- Messages per node ratio
- Execution time (ms)
- Complete path taken

## Testing

```bash
# All tests (98 tests)
mvn test

# Specific test
mvn -Dtest=FloodingStrategyTest test
mvn -Dtest=CacheBehaviorTest test
```

**Test Coverage:**
- ✅ Topology validation (4 rules)
- ✅ All 4 search strategies
- ✅ TTL expiration and propagation
- ✅ Cache propagation via RESPONSE
- ✅ Anti-echo mechanism
- ✅ Metrics consistency
- ✅ Network loader (JSON parsing)

## Documentation

- **[GUIA_DE_USO.md](GUIA_DE_USO.md)** - User manual (Portuguese)
- **[GUIA_ACADEMICO.md](GUIA_ACADEMICO.md)** - Academic defense guide (Portuguese)

## Troubleshooting

**Error: `UnsupportedClassVersionError`**
- **Cause**: Java < 17
- **Fix**: Install Java 17+ and set `JAVA_HOME`

**Error: `NetworkConfig not found`**
- **Cause**: Missing `config.json`
- **Fix**: Ensure `src/main/resources/config.json` exists

**GUI doesn't open**
- **Cause**: GraphStream/Swing unavailable
- **Fix**: Check console logs, try CLI mode

**Tests failing**
- **Fix**: `mvn clean install -U`

## Performance

Typical results on 12-node network (TTL=10):

| Metric | Flooding | Random Walk | Informed Flooding | Informed Random |
|--------|----------|-------------|-------------------|-----------------|
| Messages (1st) | 20 | 12 | 20 | 12 |
| Messages (2nd) | 20 | 12 | **1** 🚀 | **1** 🚀 |
| Nodes visited | 12 (100%) | 5 (40%) | 12 (100%) | 5 (40%) |
| Success rate | 100% | 50% | 100% | 60% |
| Hops | 7 | 4 | 7→1 | 4→1 |

**Key Insight**: Informed strategies reduce 2nd search by **95%** (cache hit = 1 message).

## Academic Compliance

This project fully complies with distributed systems course requirements:

✅ **Java 17** (LTS) with Maven  
✅ **Strategy Pattern** for algorithms  
✅ **JGraphT** for graph modeling  
✅ **GraphStream** with "Academic Style" (white background, sober colors)  
✅ **Topology Validation** (connectivity, degree, resources, self-loops)  
✅ **TTL + Anti-Echo** mechanisms  
✅ **Cache via RESPONSE** messages  
✅ **Comprehensive Testing** (98 tests, 100% pass rate)  
✅ **Detailed Metrics** (messages, hops, coverage, time)  

## License

MIT License - See [LICENSE](LICENSE)

## Authors

Developed for **Distributed Systems** course - Universidade Federal do Ceará (UFC)

---

**TL;DR**: P2P search simulator with 4 strategies, smart caching (95% message reduction), real-time visualization, and 98 passing tests.
