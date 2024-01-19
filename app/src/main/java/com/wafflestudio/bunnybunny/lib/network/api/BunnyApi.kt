package com.wafflestudio.bunnybunny.lib.network.api

import com.wafflestudio.bunnybunny.data.example.LoginRequest
import com.wafflestudio.bunnybunny.data.example.LoginResponse
import com.wafflestudio.bunnybunny.data.example.SignupRequest
import com.wafflestudio.bunnybunny.data.example.SocialLoginRequest
import com.wafflestudio.bunnybunny.data.example.SocialSignupRequest
import com.wafflestudio.bunnybunny.data.example.UserInfo
import com.wafflestudio.bunnybunny.lib.network.dto.GoodsPostContent
import com.wafflestudio.bunnybunny.lib.network.dto.GoodsPostList
import com.wafflestudio.bunnybunny.lib.network.dto.SocialLoginResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface BunnyApi {

    @POST("/auth/login")
    suspend fun loginRequest(
        @Body request: LoginRequest) : LoginResponse

    @GET("/posts")
    suspend fun getGoodsPostList(
        @Header("Authorization") authToken:String,
        @Query("cur") cur: Long?,
        @Query("seed") seed: Int?,
        @Query("distance") distance:Int,
        @Query("areaId") areaId: Int,
        @Query("count") count:Int?,
        ) : GoodsPostList

    @GET("/posts/{post_id}")
    suspend fun getGoodsPostContent(
        @Header("Authorization") authToken:String,
        @Path("post_id") postId:Long,
    ) : GoodsPostContent


    @POST("/posts/wish/{post_id}")
    suspend fun wishToggle(
        @Header("Authorization") authToken:String,
        @Path("post_id") postId: Long,
        @Query("enable") enable:Boolean,
    )
  
    @POST("/signup")
    suspend fun signupRequest(@Body request: SignupRequest): UserInfo

    @POST("/auth/login/{provider}")
    suspend fun socialLoginRequest(@Body request: SocialLoginRequest, @Path("provider") provider: String): SocialLoginResponse

    @POST("/signup/{provider}")
    suspend fun socialSignUpRequest(@Body request: SocialSignupRequest, @Path("provider") provider: String): UserInfo

}