# 🌳 Helix Internship Assignment - Tree Structure Expansion

## How to Run
This project is built with Gradle and Java.

### Prerequisites
- Java 17+
- Gradle (or use the included Gradle wrapper)

### Running the Program
1. Clone this repository and place your `input.json` file at the project root.
2. In the terminal, run:

```bash
./gradlew run
```

### Running the test cases
```bash
./gradlew test
```

### Key Design Ideas
1. **Jackson Library**: Used for JSON parsing and tree manipulation.
2. **Recursive Expansion**: The core logic uses a recursive function to expand each node type (atom, sequence, reference, array, hierarchy).

### AI Assistance
Using Chatgpt to help understand the concept of Ancestor and Paths:
- Ask ai for get example to help better understand the logic behind it
