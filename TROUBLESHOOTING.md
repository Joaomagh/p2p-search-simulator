# Guia de Solução de Problemas

## Erro: "UnsupportedClassVersionError" ou "class file version 61.0"

**Problema:** Você está tentando executar o programa com Java 8, mas ele foi compilado para Java 17+.

**Solução:**

### Opção 1: Instalar Java 17+ (Recomendado)

1. Baixe e instale o Java 17 ou superior:
   - Oracle JDK: https://www.oracle.com/java/technologies/downloads/
   - OpenJDK: https://adoptium.net/

2. Configure o `JAVA_HOME`:
   - Método rápido: execute `scripts\setup-java17.ps1 "C:\Program Files\Java\jdk-21.0.6"`
   - Manual:
     ```cmd
     # Windows
     setx JAVA_HOME "C:\Program Files\Java\jdk-21.0.6"
     setx PATH "%PATH%;%JAVA_HOME%\bin"
     ```

3. Verifique a instalação:
   ```cmd
   java -version
   ```
   Deve mostrar versão 17 ou superior.

### Opção 2: Usar o Java 17+ já instalado

Se você já tem Java 17+ instalado mas está usando Java 8 no PATH:

#### Windows:
```cmd
# Execute diretamente com o caminho completo
"C:\Program Files\Java\jdk-21.0.6\bin\java.exe" -jar target/p2p-simulator.jar

# Ou use o script que detecta automaticamente
run-auto.bat
```

#### Linux/Mac:
```bash
# Execute com o caminho completo
/usr/lib/jvm/java-17-openjdk/bin/java -jar target/p2p-simulator.jar

# Ou configure JAVA_HOME temporariamente
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
java -jar target/p2p-simulator.jar
```

## Erro: "NoClassDefFoundError" ou "ClassNotFoundException"

**Problema:** As dependências não foram incluídas no JAR.

**Solução:**

1. Recompile o projeto com o maven-shade-plugin:
   ```bash
   mvn clean package
   ```

2. Certifique-se de usar o JAR correto:
   - ✅ **USAR:** `target/p2p-simulator.jar` (com todas as dependências)
   - ❌ **NÃO USAR:** `target/p2p-search-simulator-1.0-SNAPSHOT.jar` (JAR normal sem dependências)

## Erro de Compilação no Maven

**Problema:** Maven não consegue compilar o projeto.

**Solução:**

1. Verifique se o Maven está usando o Java 17+:
   ```bash
   mvn -version
   ```

2. Configure o Maven para usar o Java correto:
   ```bash
   # Windows
   set JAVA_HOME=C:\Program Files\Java\jdk-21.0.6
   mvn clean package
   
   # Linux/Mac
   export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
   mvn clean package
   ```

## Visualização não aparece

**Problema:** A janela de visualização não abre ou fecha imediatamente.

**Solução:**

1. Certifique-se de ter respondido "s" quando perguntado sobre visualização gráfica.

2. Verifique se há erros no console relacionados a GraphStream.

3. Tente executar sem visualização primeiro para testar o algoritmo:
   - Responda "n" quando perguntado sobre visualização.

## Problema com encoding (caracteres estranhos no terminal)

**Problema:** Caracteres acentuados aparecem incorretamente (�, Ã§Ã£, etc.).

**Solução:**

### Windows:
```cmd
# Execute o terminal com UTF-8
chcp 65001

# Depois execute o programa
java -jar target/p2p-simulator.jar
```

### PowerShell:
```powershell
# Configure o PowerShell para UTF-8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8"

# Execute o programa
java -jar target/p2p-simulator.jar
```

## Erro: "Error: Could not find or load main class"

**Problema:** O manifest do JAR não está configurado corretamente.

**Solução:**

1. Verifique se usou o maven-shade-plugin (já configurado no `pom.xml`).

2. Recompile:
   ```bash
   mvn clean package
   ```

3. Verifique o conteúdo do JAR:
   ```bash
   # Windows
   jar tf target/p2p-simulator.jar | findstr Main.class
   
   # Linux/Mac
   jar tf target/p2p-simulator.jar | grep Main.class
   ```
   Deve mostrar: `p2p/search/simulator/Main.class`

## Testes falhando

**Problema:** `mvn test` reporta falhas.

**Solução:**

1. Execute os testes com mais detalhes:
   ```bash
   mvn test -X
   ```

2. Execute um teste específico:
   ```bash
   mvn test -Dtest=TopologyTest
   mvn test -Dtest=SimulationTest
   ```

3. Limpe e recompile:
   ```bash
   mvn clean test
   ```

## Verificar Versões

```bash
# Verificar Java
java -version

# Verificar Maven
mvn -version

# Verificar JAVA_HOME
echo %JAVA_HOME%     # Windows CMD
echo $env:JAVA_HOME # Windows PowerShell
echo $JAVA_HOME     # Linux/Mac

# Listar instalações do Java no Windows
dir "C:\Program Files\Java"

# Listar instalações do Java no Linux/Mac
ls /usr/lib/jvm
```

## Ainda com Problemas?

1. Verifique o log completo do Maven durante a compilação.
2. Certifique-se de que todas as dependências foram baixadas corretamente.
3. Tente limpar o cache do Maven:
   ```bash
   mvn clean
   mvn dependency:purge-local-repository
   mvn package
   ```

4. Verifique se há firewalls bloqueando o download das dependências do Maven Central.
