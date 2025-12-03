package com.idktogo.idk_to_go.service;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

public class QuizService {
    OpenAIClient client = OpenAIOkHttpClient.fromEnv();

    ResponseCreateParams params = ResponseCreateParams.builder()
            .input("What is the capital of France?")
            .model("gpt-5-nano")
            .build();

    Response response = client.responses().create(params);



}
