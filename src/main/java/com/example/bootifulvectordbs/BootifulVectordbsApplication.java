package com.example.bootifulvectordbs;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.io.Resource;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class BootifulVectordbsApplication {

    public static void main(String[] args) {
        SpringApplication.run(BootifulVectordbsApplication.class, args);
    }

    private static void init(Resource resource, VectorStore vectorStore) throws Exception {
        var csvAsString = Arrays.stream(resource.getContentAsString(Charset.defaultCharset())
                        .split(System.lineSeparator()))
                .map(String::trim)
                .toList();


        var ts = new TokenTextSplitter();

        for (var line : csvAsString) {

            var firstComma = line.indexOf(',');
            var uuid = line.substring(0, firstComma);
            var description = line.substring(firstComma).substring(2);
            description = description.substring(0, description.length() - 1);

            var docNotPollack = new Document(description, Map.of("productId", uuid));
            var docs = ts.apply(List.of(docNotPollack));
            vectorStore.add(docs);


        }
    }

    @Bean
    ApplicationRunner applicationRunner(
            @Value("classpath:/tshirts.csv") Resource resource,
            VectorStore vectorStore, ConversionService conversionService) {
        return args -> {

            var greenSilk = vectorStore
                    .similaritySearch("green silk");
            System.out.println("found "+greenSilk.size());
            for (var r : greenSilk)
                System.out.println("Similarity found: " + r);


        };
    }
}
