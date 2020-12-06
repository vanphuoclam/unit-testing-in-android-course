package com.techyourchance.testdoublesfundamentals.exercise4;

import com.techyourchance.testdoublesfundamentals.example4.networking.NetworkErrorException;
import com.techyourchance.testdoublesfundamentals.exercise4.FetchUserProfileUseCaseSync.UseCaseResult;
import com.techyourchance.testdoublesfundamentals.exercise4.networking.UserProfileHttpEndpointSync;
import com.techyourchance.testdoublesfundamentals.exercise4.users.User;
import com.techyourchance.testdoublesfundamentals.exercise4.users.UsersCache;

import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

public class FetchUserProfileUseCaseSyncTest {

    private static final String USER_ID = "userId";
    private FetchUserProfileUseCaseSync SUT;
    private UserProfileHttpEndpointSyncTd userProfileHttpEndpointSyncTd;
    private UsersCacheTd usersCacheTd;

    @Before
    public void setUp() throws Exception {
        userProfileHttpEndpointSyncTd = new UserProfileHttpEndpointSyncTd();
        usersCacheTd = new UsersCacheTd();
        SUT = new FetchUserProfileUseCaseSync(userProfileHttpEndpointSyncTd, usersCacheTd);
    }

    @Test
    public void fetchUserProfileSync_user_id_is_passed_to_end_point() {
        SUT.fetchUserProfileSync(USER_ID);
        Assert.assertThat(userProfileHttpEndpointSyncTd.mUserId, is(USER_ID));
    }

    @Test
    public void fetchUserProfileSync_success_user_is_cached() {
        SUT.fetchUserProfileSync(USER_ID);
        User user = usersCacheTd.getUser(USER_ID);
        Assert.assertThat(user.getUserId(), is(USER_ID));
    }

    @Test
    public void fetchUserProfileSync_fail_user_is_not_cached() {
        userProfileHttpEndpointSyncTd.mIsGeneralError = true;
        SUT.fetchUserProfileSync(USER_ID);
        Assert.assertThat(usersCacheTd.mInteractionsCount, is(0));
    }

    @Test
    public void fetchUserProfileSync_success_successReturned() {
        UseCaseResult res = SUT.fetchUserProfileSync(USER_ID);
        Assert.assertThat(res, is(UseCaseResult.SUCCESS));
    }

    @Test
    public void fetchUserProfileSync_general_error_failedReturned() {
        userProfileHttpEndpointSyncTd.mIsGeneralError = true;
        UseCaseResult res = SUT.fetchUserProfileSync(USER_ID);
        Assert.assertThat(res, is(UseCaseResult.FAILURE));
    }

    @Test
    public void fetchUserProfileSync_auth_error_failedReturned() {
        userProfileHttpEndpointSyncTd.mIsAuthError = true;
        UseCaseResult res = SUT.fetchUserProfileSync(USER_ID);
        Assert.assertThat(res, is(UseCaseResult.FAILURE));
    }

    @Test
    public void fetchUserProfileSync_server_error_failedReturned() {
        userProfileHttpEndpointSyncTd.mIsServerError = true;
        UseCaseResult res = SUT.fetchUserProfileSync(USER_ID);
        Assert.assertThat(res, is(UseCaseResult.FAILURE));
    }

    @Test
    public void fetchUserProfileSync_networt_error_failedReturned() {
        userProfileHttpEndpointSyncTd.mIsGeneralError = true;
        UseCaseResult res = SUT.fetchUserProfileSync(USER_ID);
        Assert.assertThat(res, is(UseCaseResult.NETWORK_ERROR));
    }

    private class UserProfileHttpEndpointSyncTd implements UserProfileHttpEndpointSync {
        public String mUserId;
        public String mFullName = "fullname";
        private String mImageUrl = "imageUrl";
        public boolean mIsGeneralError;
        public boolean mIsAuthError;
        public boolean mIsServerError;
        public boolean mIsNetworkError;

        @Override
        public EndpointResult getUserProfile(String userId) throws NetworkErrorException {
            mUserId = userId;
            if (mIsGeneralError) {
                return new EndpointResult(EndpointResultStatus.GENERAL_ERROR, "", "", "");
            } else if (mIsAuthError) {
                return new EndpointResult(EndpointResultStatus.AUTH_ERROR, "", "", "");
            } else if (mIsServerError) {
                return new EndpointResult(EndpointResultStatus.SERVER_ERROR, "", "", "");
            } else if (mIsNetworkError) {
                throw new NetworkErrorException();
            }
            return new EndpointResult(EndpointResultStatus.SUCCESS, userId, mFullName, mImageUrl);
        }
    }

    private class UsersCacheTd implements UsersCache {
        public int mInteractionsCount;
        private User mUser;
        @Override
        public void cacheUser(User user) {
            mInteractionsCount++;
            mUser = user;
        }

        @Nullable
        @Override
        public User getUser(String userId) {
            return mUser;
        }
    }
}