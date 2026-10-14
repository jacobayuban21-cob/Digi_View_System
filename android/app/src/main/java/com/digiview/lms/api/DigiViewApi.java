package com.digiview.lms.api;

import com.digiview.lms.model.*;
import retrofit2.Call;
import retrofit2.http.*;
import java.util.List;

public interface DigiViewApi {

    @POST("api/auth/register")
    Call<ApiResponse<User>> register(@Body RegisterRequest request);

    @POST("api/auth/login")
    Call<ApiResponse<User>> login(@Body AuthRequest request);

    @GET("api/users/{username}")
    Call<ApiResponse<UserProfile>> getProfile(@PathVariable("username") String username);

    @POST("api/quiz/record")
    Call<ApiResponse<QuizRecord>> submitQuizRecord(@Body QuizRecord record);

    @GET("api/quiz/history/{username}")
    Call<ApiResponse<List<QuizRecord>>> getQuizHistory(@PathVariable("username") String username);

    @GET("api/leaderboard")
    Call<ApiResponse<List<LeaderboardEntry>>> getLeaderboard();
}
