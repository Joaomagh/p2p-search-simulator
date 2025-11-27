package p2p.search.simulator.loader;

import p2p.search.simulator.model.NetworkConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * ResponsÃ¡vel por carregar a configuraÃ§Ã£o da rede a partir de um arquivo JSON.
 */
public class NetworkLoader {
    
    private final ObjectMapper objectMapper;
    
    public NetworkLoader() {
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Carrega a configuraÃ§Ã£o da rede a partir de um arquivo.
     * 
     * @param filePath caminho do arquivo JSON
     * @return objeto NetworkConfig com os dados carregados
     * @throws IOException se houver erro na leitura ou parsing do arquivo
     */
    public NetworkConfig loadFromFile(String filePath) throws IOException {
        File file = new File(filePath);
        return objectMapper.readValue(file, NetworkConfig.class);
    }
    
    /**
     * Carrega a configuraÃ§Ã£o da rede a partir de um recurso no classpath.
     * Ãštil para carregar arquivos de src/main/resources.
     * 
     * @param resourcePath caminho do recurso (ex: "config.json")
     * @return objeto NetworkConfig com os dados carregados
     * @throws IOException se houver erro na leitura ou parsing do recurso
     */
    public NetworkConfig loadFromResource(String resourcePath) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
        
        if (inputStream == null) {
            throw new IOException("Resource not found: " + resourcePath);
        }
        
        return objectMapper.readValue(inputStream, NetworkConfig.class);
    }
    
    /**
     * Carrega a configuraÃ§Ã£o da rede a partir de uma string JSON.
     * 
     * @param jsonContent conteÃºdo JSON como string
     * @return objeto NetworkConfig com os dados carregados
     * @throws IOException se houver erro no parsing do JSON
     */
    public NetworkConfig loadFromString(String jsonContent) throws IOException {
        return objectMapper.readValue(jsonContent, NetworkConfig.class);
    }
}
