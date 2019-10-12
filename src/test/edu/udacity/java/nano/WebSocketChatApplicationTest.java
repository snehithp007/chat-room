package edu.udacity.java.nano;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@RunWith(SpringRunner.class)
@WebMvcTest(WebSocketChatApplication.class)
@AutoConfigureMockMvc
public class WebSocketChatApplicationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void loginTest() throws Exception {

        mvc.perform(
                MockMvcRequestBuilders.get("/")
                        .accept(MediaType.TEXT_HTML_VALUE)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("login"));

    }

    @Test
    public void indexTestWithoutUserName() throws Exception {
        MvcResult result = mvc.perform(
                MockMvcRequestBuilders.get("/index")
                        .accept(MediaType.TEXT_HTML_VALUE)
        )
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andReturn();

        Optional<MissingServletRequestParameterException> e = Optional.ofNullable((MissingServletRequestParameterException) result.getResolvedException());
        e.ifPresent((exception) -> assertThat(exception, is(notNullValue())));
        e.ifPresent((exception) -> assertThat(exception, is(instanceOf(MissingServletRequestParameterException.class))));
    }

    @Test
    public void indexTestWithUserName() throws Exception {
        MvcResult result = mvc.perform(
                MockMvcRequestBuilders.get("/index")
                        .param("username", "snehith")
                        .accept(MediaType.TEXT_HTML_VALUE)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("chat"))
                .andReturn();
        assertEquals(true, result.getResponse().getContentAsString().contains("snehith"));

    }

}