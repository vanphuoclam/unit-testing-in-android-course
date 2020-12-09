package com.techyourchance.mockitofundamentals.exercise5;

import com.techyourchance.mockitofundamentals.exercise5.UpdateUsernameUseCaseSync.UseCaseResult;
import com.techyourchance.mockitofundamentals.exercise5.eventbus.EventBusPoster;
import com.techyourchance.mockitofundamentals.exercise5.eventbus.UserDetailsChangedEvent;
import com.techyourchance.mockitofundamentals.exercise5.networking.NetworkErrorException;
import com.techyourchance.mockitofundamentals.exercise5.networking.UpdateUsernameHttpEndpointSync;
import com.techyourchance.mockitofundamentals.exercise5.networking.UpdateUsernameHttpEndpointSync.EndpointResult;
import com.techyourchance.mockitofundamentals.exercise5.networking.UpdateUsernameHttpEndpointSync.EndpointResultStatus;
import com.techyourchance.mockitofundamentals.exercise5.users.User;
import com.techyourchance.mockitofundamentals.exercise5.users.UsersCache;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UpdateUsernameUseCaseSyncTest {

    public static String USER_NAME = "userName";
    public static String USER_ID = "userID";

    UpdateUsernameUseCaseSync SUT;
    UpdateUsernameHttpEndpointSync updateUsernameHttpEndpointSyncMock;
    UsersCache usersCacheMock;
    EventBusPoster eventBusPosterMock;

    @Before
    public void setup() throws Exception {
        updateUsernameHttpEndpointSyncMock = Mockito.mock(UpdateUsernameHttpEndpointSync.class);
        usersCacheMock = Mockito.mock(UsersCache.class);
        eventBusPosterMock = Mockito.mock(EventBusPoster.class);
        SUT = new UpdateUsernameUseCaseSync(updateUsernameHttpEndpointSyncMock, usersCacheMock, eventBusPosterMock);
        success();
    }

    //user name is passed to endpoint
    @Test
    public void updateUsernameSync_user_id_and_userName_is_passed_to_end_point() throws Exception {
        ArgumentCaptor<String> ac = ArgumentCaptor.forClass(String.class);
        SUT.updateUsernameSync(USER_ID, USER_NAME);
        verify(updateUsernameHttpEndpointSyncMock, times(1)).updateUsername(ac.capture(), ac.capture());
        List<String> captures = ac.getAllValues();
        assertThat(captures.get(0), is(USER_ID));
        assertThat(captures.get(1), is(USER_NAME));
    }

    //success user name is cached
    @Test
    public void updateUsernameSync_success_user_name_is_cached() {
        ArgumentCaptor<User> ac = ArgumentCaptor.forClass(User.class);
        SUT.updateUsernameSync(USER_ID, USER_NAME);
        verify(usersCacheMock).cacheUser(ac.capture());
        assertThat(ac.getValue().getUserId(), is(USER_ID));
    }

    //success event is posted
    @Test
    public void updateUsernameSync_success_event_is_posted() {
        ArgumentCaptor<UserDetailsChangedEvent> ac = ArgumentCaptor.forClass(UserDetailsChangedEvent.class);
        SUT.updateUsernameSync(USER_ID, USER_NAME);
        verify(eventBusPosterMock).postEvent(ac.capture());
        assertThat(ac.getValue(), is(CoreMatchers.<UserDetailsChangedEvent>instanceOf(UserDetailsChangedEvent.class)));
        assertThat(ac.getValue().getUser().getUserId(), is(USER_ID));
    }

    //fail user name is not changed
    @Test
    public void updateUsernameSync_general_error_user_not_cached() throws Exception {
        generalError();
        ArgumentCaptor<User> ac = ArgumentCaptor.forClass(User.class);
        SUT.updateUsernameSync(USER_ID, USER_NAME);
        verify(usersCacheMock, times(0)).cacheUser(ac.capture());
    }

    @Test
    public void updateUsernameSync_auth_error_user_not_cached() throws Exception {
        authError();
        ArgumentCaptor<User> ac = ArgumentCaptor.forClass(User.class);
        SUT.updateUsernameSync(USER_ID, USER_NAME);
        verify(usersCacheMock, times(0)).cacheUser(ac.capture());
    }

    @Test
    public void updateUsernameSync_server_error_user_not_cached() throws Exception {
        serverError();
        ArgumentCaptor<User> ac = ArgumentCaptor.forClass(User.class);
        SUT.updateUsernameSync(USER_ID, USER_NAME);
        verify(usersCacheMock, times(0)).cacheUser(ac.capture());
    }

    @Test
    public void updateUsernameSync_network_error_user_not_cached() throws Exception {
        networkError();
        ArgumentCaptor<User> ac = ArgumentCaptor.forClass(User.class);
        SUT.updateUsernameSync(USER_ID, USER_NAME);
        verify(usersCacheMock, times(0)).cacheUser(ac.capture());
    }


    //fail event is not posted
    @Test
    public void updateUsernameSync_general_error_no_interaction_with_eventPoster() throws Exception {
        generalError();
        ArgumentCaptor<UserDetailsChangedEvent> ac = ArgumentCaptor.forClass(UserDetailsChangedEvent.class);
        SUT.updateUsernameSync(USER_ID, USER_NAME);
        verify(eventBusPosterMock, times(0)).postEvent(ac.capture());
    }

    @Test
    public void updateUsernameSync_auth_error_no_interaction_with_eventPoster() throws Exception {
        authError();
        ArgumentCaptor<UserDetailsChangedEvent> ac = ArgumentCaptor.forClass(UserDetailsChangedEvent.class);
        SUT.updateUsernameSync(USER_ID, USER_NAME);
        verify(eventBusPosterMock, times(0)).postEvent(ac.capture());
    }

    @Test
    public void updateUsernameSync_server_error_no_interaction_with_eventPoster() throws Exception {
        serverError();
        ArgumentCaptor<UserDetailsChangedEvent> ac = ArgumentCaptor.forClass(UserDetailsChangedEvent.class);
        SUT.updateUsernameSync(USER_ID, USER_NAME);
        verify(eventBusPosterMock, times(0)).postEvent(ac.capture());
    }

    @Test
    public void updateUsernameSync_network_error_no_interaction_with_eventPoster() throws Exception {
        networkError();
        ArgumentCaptor<UserDetailsChangedEvent> ac = ArgumentCaptor.forClass(UserDetailsChangedEvent.class);
        SUT.updateUsernameSync(USER_ID, USER_NAME);
        verify(eventBusPosterMock, times(0)).postEvent(ac.capture());
    }

    //success - success returned
    @Test
    public void updateUsernameSync_success_successReturned() throws Exception {
        UseCaseResult result = SUT.updateUsernameSync(USER_ID, USER_NAME);
        assertThat(result, is(UseCaseResult.SUCCESS));
    }

    //auth error - failure returned
    @Test
    public void updateUsernameSync_auth_error_failureReturned() throws Exception {
        authError();
        UseCaseResult result = SUT.updateUsernameSync(USER_ID, USER_NAME);
        assertThat(result, is(UseCaseResult.FAILURE));
    }

    //general error - failure returned
    @Test
    public void updateUsernameSync_general_error_failureReturned() throws Exception {
        generalError();
        UseCaseResult result = SUT.updateUsernameSync(USER_ID, USER_NAME);
        assertThat(result, is(UseCaseResult.FAILURE));
    }

    //server error - failure returned
    @Test
    public void updateUsernameSync_server_error_failureReturned() throws Exception {
        serverError();
        UseCaseResult result = SUT.updateUsernameSync(USER_ID, USER_NAME);
        assertThat(result, is(UseCaseResult.FAILURE));
    }
    //network error - network error returned
    @Test
    public void updateUsernameSync_network_error_failureReturned() throws Exception {
        networkError();
        UseCaseResult result = SUT.updateUsernameSync(USER_ID, USER_NAME);
        assertThat(result, is(UseCaseResult.NETWORK_ERROR));
    }

    private void authError() throws Exception{
        when(updateUsernameHttpEndpointSyncMock.updateUsername(anyString(), anyString()))
                .thenReturn(new EndpointResult(EndpointResultStatus.AUTH_ERROR, USER_ID, USER_NAME));
    }

    private void serverError() throws Exception {
        when(updateUsernameHttpEndpointSyncMock.updateUsername(anyString(), anyString()))
                .thenReturn(new EndpointResult(EndpointResultStatus.SERVER_ERROR, USER_ID, USER_NAME));
    }

    private void networkError() throws Exception {
       doThrow(new NetworkErrorException()).when(updateUsernameHttpEndpointSyncMock).updateUsername(anyString(), anyString());
    }

    private void generalError() throws Exception {
        when(updateUsernameHttpEndpointSyncMock.updateUsername(anyString(), anyString()))
                .thenReturn(new EndpointResult(EndpointResultStatus.GENERAL_ERROR, USER_ID, USER_NAME));
    }

    public void success() throws Exception {
        when(updateUsernameHttpEndpointSyncMock.updateUsername(anyString(), anyString()))
                .thenReturn(new EndpointResult(EndpointResultStatus.SUCCESS, USER_ID, USER_NAME));
    }
}