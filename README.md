# Op_Research_Projet_Jules_Stevenson

**Auteur :** Jules Stevenson   M1 Informatique UCA 2025-2026

**Encadrant :** Jean-Charles Régin

**Langage :** Java 21 (compatible JDK 17+), aucune dépendance externe

> **Rien à modifier dans le code pour exécuter le projet.** Tous les tests sont
> intégrés. Les graphes externes se passent par argument de ligne de commande.

---

## Lancer le projet en 30 secondes (Linux / macOS)

Pré-requis : avoir un **JDK 17 ou supérieur**. Si vous n'en avez pas, voir la
section [Installer Java](#installer-java) plus bas.

```bash
unzip Op_Research_Projet_Jules_Stevenson.zip
cd Op_Research_Projet_Jules_Stevenson
bash build.sh
```

C'est tout. Le script compile les sources et lance les 5 tests intégrés
(résultats attendus listés [plus bas](#tests-intégrés-et-résultats-attendus)).

Pour exécuter le projet sur un fichier de graphe au format prof :

```bash
bash build.sh --file data/graph_data.txt
```

Vous pouvez bien sûr passer le chemin de votre propre fichier :

```bash
bash build.sh --file /chemin/vers/mon_graphe.txt
```

## Lancer le projet en 30 secondes (Windows)

Si vous êtes sur Windows sans bash (cmd ou PowerShell), `build.sh` ne marchera
pas. Utilisez les deux commandes manuelles équivalentes :

```powershell
unzip Op_Research_Projet_Jules_Stevenson.zip
cd Op_Research_Projet_Jules_Stevenson
mkdir build
javac -d build src\opresearch\*.java
java -Dfile.encoding=UTF-8 -cp build opresearch.Main
```

Et pour lancer sur un fichier de graphe :

```powershell
java -Dfile.encoding=UTF-8 -cp build opresearch.Main --file data\graph_data.txt
```

(Les mêmes commandes Java fonctionnent aussi sur Linux/macOS si vous préférez
ne pas utiliser le script `build.sh`.)

---

## Installer Java

### Ubuntu / Debian

```bash
sudo apt update
sudo apt install -y openjdk-21-jdk-headless
javac -version    # doit afficher "javac 21" (ou 17, 18, 19, 20)
```

### macOS (Homebrew)

```bash
brew install openjdk@21
javac -version
```

### Windows

Téléchargez le JDK depuis [adoptium.net](https://adoptium.net/) (Eclipse
Temurin), installez, puis ouvrez **un nouveau** PowerShell et vérifiez :

```powershell
javac -version
```

Si la commande n'est pas trouvée, ajoutez le chemin du JDK à la variable
d'environnement `Path` (typiquement `C:\Program Files\Eclipse Adoptium\jdk-21\bin`).

---

## Structure du projet

```
Op_Research_Projet_Jules_Stevenson/
├── README.md                                   ← ce fichier
├── Op_Research_Projet_JULES_Stevenson.pdf      ← rapport (focus : structure de données du résiduel)
├── build.sh                                    ← script de compilation + exécution (Linux / macOS)
├── src/opresearch/
│   ├── Edge.java                               ← arc + arc inverse lié
│   ├── Graph.java                              ← liste d'adjacence + parser format prof
│   ├── FordFulkerson.java                      ← max flow + min cut (Edmonds-Karp)
│   ├── BellmanFord.java                        ← plus courts chemins + détection cycle négatif
│   ├── MinCostFlow.java                        ← SSP via Bellman-Ford OU Dijkstra+potentiels
│   └── Main.java                               ← point d'entrée + tests
└── data/
    ├── graph_data.txt                          ← graphe d'exemple format prof (6 nœuds, 10 arcs)
    ├── min_cost_example.txt                    ← graphe d'exemple min-cost flow du DearStudents.docx
    └── digraph2.gv                             ← fichier graphviz fourni par le prof
```

---

## Format d'entrée des graphes (format prof)

```
N M s t
u1 v1 c1 k1
u2 v2 c2 k2
...
uM vM cM kM
```

- **Première ligne** : `N` sommets, `M` arcs, source `s`, puits `t`.
- **`M` lignes suivantes** : pour chaque arc, extrémité initiale, extrémité
  terminale, capacité maximale, coût unitaire.

Exemple — `data/graph_data.txt` (6 nœuds, 10 arcs, source = 5, puits = 4) :

```
6 10 5 4
5 0 10 2
5 1 8 4
0 1 5 5
...
```

L'arc `5 0 10 2` signifie l'arc (5,0) de capacité maximale 10 et coût unitaire 2.

---

## Tests intégrés et résultats attendus

`bash build.sh` (sans argument) exécute **5 tests** issus du document
`DearStudents.docx` et du fichier `graph_data.txt` :

| # | Description | Résultat attendu |
|---|---|---|
| 1 | Max flow + min cut, exemple 1 du `.docx` (5 nœuds, 9 arcs) | flot max = **60**, min cut = `{(0→1), (0→2), (0→3)}` |
| 2 | Min-cost max-flow, exemple 3 du `.docx` (s/t ajoutés)      | flot = **20**, coût = **150** (Bellman-Ford et Dijkstra donnent le même résultat) |
| 3 | Assignment 10 personnes × 8 tâches, exemple 2 du `.docx`   | sans capa : coût **370** ; avec capa = 2 : coût **404** |
| 4 | Graphe `data/graph_data.txt` (format prof, 6 nœuds, 10 arcs) | max flow = **15**, min cut = `{(0→2), (1→3)}` ; min-cost max flow = **149** |
| 5 | Détection de cycle négatif (graphe fabriqué)                | détecte `0→1→2→0` de coût `−3` ; ne détecte rien sur un graphe sain |

À la fin de l'exécution, vous devez voir :

```
=== Tous les tests sont passés. ===
```

L'invariant *"jamais de cycle négatif dans le résiduel"* est vérifié
**après chaque augmentation** dans les deux variantes de min-cost flow ;
si jamais un cycle négatif apparaissait, le programme lèverait une
`AssertionError` immédiatement.

---

## Tester sur votre propre graphe

Créez un fichier texte au format prof, par exemple `mon_test.txt` :

```
4 5 0 3
0 1 10 1
0 2 5 2
1 2 15 1
1 3 10 3
2 3 10 2
```

Puis lancez :

```bash
bash build.sh --file mon_test.txt
```

Ce qui sera exécuté :
1. Ford-Fulkerson → max flow + min cut + détail flot par arc
2. Min-cost max-flow par Dijkstra + potentiels → flot, coût, détail par arc

---

## Rapport

Le rapport complet dans le fichier [`Op_Research_Projet_JULES_Stevenson.pdf`](Op_Research_Projet_JULES_Stevenson.pdf)
à la racine du dépôt.
