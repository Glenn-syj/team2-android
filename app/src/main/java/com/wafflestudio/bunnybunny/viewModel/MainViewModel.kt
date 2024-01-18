package com.wafflestudio.bunnybunny.viewModel

import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wafflestudio.bunnybunny.SampleData.DefaultGoodsPostContentSample
import com.wafflestudio.bunnybunny.SampleData.DefaultGoodsPostListSample
import com.wafflestudio.bunnybunny.SampleData.GoodsPostContentSample
import com.wafflestudio.bunnybunny.SampleData.GoodsPostListSample
import com.wafflestudio.bunnybunny.data.example.LoginRequest
import com.wafflestudio.bunnybunny.data.example.LoginResponse
import com.wafflestudio.bunnybunny.data.example.RefAreaId
import com.wafflestudio.bunnybunny.data.example.SignupRequest
import com.wafflestudio.bunnybunny.data.example.SocialLoginRequest
import com.wafflestudio.bunnybunny.data.example.SocialSignupRequest
import com.wafflestudio.bunnybunny.data.example.UserInfo
import com.wafflestudio.bunnybunny.lib.network.api.BunnyApi
import com.wafflestudio.bunnybunny.lib.network.dto.GoodsPostContent
import com.wafflestudio.bunnybunny.lib.network.dto.GoodsPostList
import com.wafflestudio.bunnybunny.lib.network.dto.SocialLoginResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val api: BunnyApi
): ViewModel() {
    //홈 탭:Home,0
    //동네생활 탭:Community,1
    //채팅 탭:Chat,2
    //나의당근 탭:My,3
    var selectedTabIndex= mutableIntStateOf(0)
    var isgettingNewPostList= false
    var isgettingNewPostContent= false

    var accessToken=""
    var refAreaId= listOf<Int>()

    private val _goodsPostList = MutableStateFlow(DefaultGoodsPostListSample)
    val goodsPostList: StateFlow<GoodsPostList> = _goodsPostList.asStateFlow()
    fun updateGoodsPostList(newContent: GoodsPostList) {
        _goodsPostList.value = newContent
    }

    private val _goodsPostContent = MutableStateFlow(DefaultGoodsPostContentSample)
    val goodsPostContent: StateFlow<GoodsPostContent> = _goodsPostContent.asStateFlow()

    // 상태를 업데이트하는 함수입니다.
    fun updateGoodsPostContent(newContent: GoodsPostContent) {
        _goodsPostContent.value = newContent
    }


    companion object {}
    fun getToken():String{
        //Log.d("aaaa", "tag:$accessToken")
        return "Bearer $accessToken"
    }
    fun getGoodsPostList(distance:Int,areaId: Int){
        Log.d("aaaa","getGoodsPostList called:")
        if(goodsPostList.value.count!=0&&goodsPostList.value.isLast){
            Log.d("aaaa","getGoodsPostList canceled:$isgettingNewPostList,${goodsPostList.value.isLast}")
            return
        }
        Log.d("aaaa","getGoodsPostList called")
        /*
        updateGoodsPostList(GoodsPostListSample)
        isgettingNewPostList.value=false*/

        viewModelScope.launch(Dispatchers.IO) {
            try{
                val response=api.getGoodsPostList(
                    authToken=getToken(),
                    cur = goodsPostList.value.cur,
                    seed = goodsPostList.value.seed,
                    distance = distance,
                    areaId = areaId,
                    count=goodsPostList.value.count)
                Log.d("aaaa",response.toString())

                withContext(Dispatchers.Main){
                    updateGoodsPostList(response.copy(data = goodsPostList.value.data+response.data))
                    isgettingNewPostList=false
                }
            }catch(e: Exception){
                isgettingNewPostList=false
                Log.d("aaaa", "getGoodsPostList failed: $e")
                /////////////////////////////////////////
                //updateGoodsPostList(GoodsPostListSample)
            }
        }
    }
    fun getGoodsPostContent(id:Long){

        Log.d("aaaa","getGoodsPostContent called: authToken=${getToken()}, postId=$id")
        viewModelScope.launch(Dispatchers.IO) {
            try{
                val response=api.getGoodsPostContent(authToken=getToken(),postId=id)
                Log.d("aaaa",response.toString())

                withContext(Dispatchers.Main){
                    updateGoodsPostContent(response)
                    isgettingNewPostContent=false
                    //isgettingNewPostList.value=false
                }
            }catch(e: Exception){
                isgettingNewPostContent=false
                Log.d("aaaa",e.toString())
                ///////////////////////////////
                updateGoodsPostContent(GoodsPostContentSample)
            }
        }
    }

    suspend fun wishToggle(id:Long,enable:Boolean) {
        Log.d("wish","enable:$enable")
        api.wishToggle(authToken=getToken(),id,enable)
    }

    suspend fun tryLogin(email: String, password: String): LoginResponse {
            return api.loginRequest(LoginRequest(email, password))
    }
    suspend fun trySignup(data: SignupRequest): UserInfo{
        return api.signupRequest(data)
    }

    suspend fun trySocialLogin(data: SocialLoginRequest): SocialLoginResponse {
        return api.socialLoginRequest(data, "kakao")
    }

    suspend fun trySocialSignUp(data: SocialSignupRequest): UserInfo {
        return api.socialSignUpRequest(data, "kakao")
    }


}