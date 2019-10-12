package edu.udacity.java.nano.chat;

import com.alibaba.fastjson.JSON;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class WebSocketChatServerTest {

    @InjectMocks
    WebSocketChatServer webSocketChatServer = new WebSocketChatServer();

    @Mock
    Session user1;

    @Mock
    Session user2;

    @Mock
    RemoteEndpoint.Basic endpoint;

    @Captor
    ArgumentCaptor<String> args;

    private List<Session> sessionList = new ArrayList<>();

    /*Utility method to create session.
     * sessionid is used to remove the session from onlineSessions when a session closes.
     * accessing getBasicRemote() as message is brodcasted to reduce the onlineCount when user session is closed.
     * */
    private void createSession(Session session, String sessionId) {
        when(session.getId()).thenReturn(sessionId);
        when(session.getBasicRemote()).thenReturn(endpoint);
        sessionList.add(session);
    }

    @Before
    //Creating one user session for every test
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        createSession(user1, "user1");
    }

    @After
    //closing the session after each test
    public void tearDown() {
        sessionList.forEach(session -> webSocketChatServer.onClose(session));
    }

    @Test
    //Check for onlinecount and message type when user joins.
    public void userJoinTest() throws IOException {
        webSocketChatServer.onOpen(user1, "test");
        verify(user1.getBasicRemote()).sendText(args.capture());
        assertEquals("test", JSON.parseObject(args.getValue()).getString("username"));
        assertEquals(1, JSON.parseObject(args.getValue()).getIntValue("onlineCount"));
        assertEquals("ENTER", JSON.parseObject(args.getValue()).getString("type"));
    }

    @Test
    //asserting for test message and message type when user chats
    public void userChatTest() throws IOException {
        webSocketChatServer.onOpen(user1, "test");
        Message msg = new Message("test", "hello", "SPEAK", 1);
        webSocketChatServer.onMessage(user1, JSON.toJSONString(msg));
        verify(user1.getBasicRemote(), times(2)).sendText(args.capture());
        assertEquals(msg.getType(), JSON.parseObject(args.getValue()).getString("type"));
        assertEquals(msg.getMsg(), JSON.parseObject(args.getValue()).getString("msg"));
    }

    @Test
    //checking for decrease in onlinecount when user leaves the chat
    public void userLeaveTest() throws IOException {
        webSocketChatServer.onOpen(user1, "user1");
        createSession(user2, "user2");
        webSocketChatServer.onOpen(user2, "user2");
        webSocketChatServer.onClose(user2);
        verify(user1.getBasicRemote(), times(4)).sendText(args.capture());
        List<String> msgs = args.getAllValues();
        assertEquals(2, JSON.parseObject(msgs.get(2)).getIntValue("onlineCount"));
        assertEquals(1, JSON.parseObject(msgs.get(3)).getIntValue("onlineCount"));
    }

}