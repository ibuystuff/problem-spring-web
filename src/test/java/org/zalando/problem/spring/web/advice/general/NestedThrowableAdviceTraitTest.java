package org.zalando.problem.spring.web.advice.general;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.zalando.problem.ProblemModule;
import org.zalando.problem.spring.web.advice.AdviceTraitTesting;
import org.zalando.problem.spring.web.advice.ProblemHandling;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class NestedThrowableAdviceTraitTest implements AdviceTraitTesting {

    @Override
    public Object unit() {
        return new NestedProblemHandling();
    }

    @Override
    public ObjectMapper mapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new ProblemModule().withStackTraces());
        return mapper;
    }

    @Test
    public void throwable() throws Exception {
        mvc().perform(request(GET, "http://localhost/api/handler-throwable"))
                .andExpect(status().isInternalServerError())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(jsonPath("$.type").doesNotExist())
                .andExpect(jsonPath("$.title", is("Internal Server Error")))
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.detail", containsString("expected")))
                .andExpect(jsonPath("$.cause").exists());
    }

    @Test
    public void nestedThrowable() throws Exception {
        mvc().perform(request(GET, "http://localhost/api/nested-throwable"))
                .andExpect(status().isInternalServerError())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(jsonPath("$.type").doesNotExist())
                .andExpect(jsonPath("$.title", is("Internal Server Error")))
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.detail", containsString("Illegal State")))
                .andExpect(jsonPath("$.stacktrace", is(instanceOf(List.class))))
                .andExpect(jsonPath("$.stacktrace[0]", containsString("newIllegalState")))
                .andExpect(jsonPath("$.stacktrace[1]", containsString("nestedThrowable")))
                .andExpect(jsonPath("$.cause.type").doesNotExist())
                .andExpect(jsonPath("$.cause.title", is("Internal Server Error")))
                .andExpect(jsonPath("$.cause.status", is(500)))
                .andExpect(jsonPath("$.cause.detail", containsString("Illegal Argument")))
                .andExpect(jsonPath("$.cause.stacktrace", is(instanceOf(List.class))))
                .andExpect(jsonPath("$.cause.stacktrace[0]", containsString("newIllegalArgument")))
                .andExpect(jsonPath("$.cause.stacktrace[1]", containsString("nestedThrowable")))
                .andExpect(jsonPath("$.cause.cause.type").doesNotExist())
                .andExpect(jsonPath("$.cause.cause.title", is("Internal Server Error")))
                .andExpect(jsonPath("$.cause.cause.status", is(500)))
                .andExpect(jsonPath("$.cause.cause.detail", containsString("Null Pointer")))
                .andExpect(jsonPath("$.cause.cause.stacktrace", is(instanceOf(List.class))))
                .andExpect(jsonPath("$.cause.cause.stacktrace[0]", containsString("newNullPointer")))
                .andExpect(jsonPath("$.cause.cause.stacktrace[1]", containsString("nestedThrowable")))
                .andExpect(jsonPath("$.cause.cause.cause").doesNotExist());
    }

    @ControllerAdvice
    private static class NestedProblemHandling implements ProblemHandling {

        @Override
        public boolean isCausalChainsEnabled() {
            return true;
        }

    }

}