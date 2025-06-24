package com.example.demo.chatbot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatCompletionChunk {
    private String id;
    private String object;
    private long created;
    private String model;
    private List<Choice> choices;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        private Delta delta;
        private int index;

        @JsonProperty("finish_reason")
        private String finishReason;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Delta {
        /** 최초 청크에만 오고 이후엔 null */
        private String role;

        /** 이후 스트리밍 되는 텍스트 델타 */
        private String content;
    }
}
