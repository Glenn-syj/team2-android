package com.wafflestudio.bunnybunny.viewModel

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.wafflestudio.bunnybunny.SampleData
import com.wafflestudio.bunnybunny.SampleData.DefaultGoodsPostContentSample
import com.wafflestudio.bunnybunny.SampleData.DefaultGoodsPostListSample
import com.wafflestudio.bunnybunny.SampleData.DefaultUserInfo
import com.wafflestudio.bunnybunny.data.example.EditProfileRequest
import com.wafflestudio.bunnybunny.data.example.GoodsPostPagingSource
import com.wafflestudio.bunnybunny.data.example.LoginRequest
import com.wafflestudio.bunnybunny.data.example.LoginResponse
import com.wafflestudio.bunnybunny.data.example.PrefRepository
import com.wafflestudio.bunnybunny.data.example.SignupRequest
import com.wafflestudio.bunnybunny.data.example.SignupResponse
import com.wafflestudio.bunnybunny.data.example.SimpleAreaData
import com.wafflestudio.bunnybunny.data.example.SocialLoginRequest
import com.wafflestudio.bunnybunny.data.example.SocialSignupRequest
import com.wafflestudio.bunnybunny.data.example.UserInfo
import com.wafflestudio.bunnybunny.lib.network.api.BunnyApi
import com.wafflestudio.bunnybunny.lib.network.dto.CommunityPostList
import com.wafflestudio.bunnybunny.lib.network.dto.GoodsPostContent
import com.wafflestudio.bunnybunny.lib.network.dto.GoodsPostPreview
import com.wafflestudio.bunnybunny.lib.network.dto.SocialLoginResponse
import com.wafflestudio.bunnybunny.lib.network.dto.SubmitPostRequest
import com.wafflestudio.bunnybunny.lib.network.dto.postImagesResponse
import com.wafflestudio.bunnybunny.model.ToggleImageItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val api: BunnyApi,
    private val prefRepository: PrefRepository,
) : ViewModel() {


    val querySignal = MutableStateFlow(
        GoodsPostPagingSource(
            api = api,
            token = "",
            distance = 0,
            areaId = 0,
        )
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val goodsPostList = querySignal.flatMapLatest {
        getGoodsPostList(it)
            .cachedIn(viewModelScope)
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        PagingData.empty()
    )

    suspend fun updateGoodsPostList(
        cur: Long?,
        seed: Int?,
        distance: Int,
        areaId: Int,
        count: Int,
    ) {
        querySignal.emit(
            GoodsPostPagingSource(
                api = api,
                token = getTokenHeader()!!,
                distance = distance,
                areaId = areaId,
            )
        )
    }

    private val _wishList = MutableStateFlow(DefaultGoodsPostListSample.data)
    val wishList: StateFlow<List<GoodsPostPreview>> = _wishList.asStateFlow()


    private val _goodsPostContent = MutableStateFlow(DefaultGoodsPostContentSample)
    val goodsPostContent: StateFlow<GoodsPostContent> = _goodsPostContent.asStateFlow()

    fun updateGoodsPostContent(newContent: GoodsPostContent) {
        _goodsPostContent.value = newContent
    }

    private val _galleryImages = MutableStateFlow(listOf<ToggleImageItem>())
    val galleryImages: StateFlow<List<ToggleImageItem>> = _galleryImages.asStateFlow()

    fun updateGalleryImages(newContent: List<ToggleImageItem>) {
        _galleryImages.value = newContent
        Log.d("aaaa", galleryImages.value.toString())
        Log.d("aaaa", selectedImages.value.toString())

    }

    private val _selectedImages = MutableStateFlow(listOf<Int>())
    val selectedImages: StateFlow<List<Int>> = _selectedImages.asStateFlow()
    fun updateSelectedImages(newContent: List<Int>) {
        if (newContent.size + uploadImages.value.size > 10) return
        _selectedImages.value = newContent
        val updateList = galleryImages.value.toMutableList()
        updateList.forEachIndexed { imageIndex, item ->
            updateList[imageIndex] = item.copy(
                isSelected = selectedImages.value.contains(imageIndex),
                selectedOrder = if (selectedImages.value.contains(imageIndex)) selectedImages.value.indexOf(
                    imageIndex
                ) + 1 else null
            )
        }
        updateGalleryImages(updateList)
        //Log.d("aaaa","update gallery called")
    }

    private val _uploadImages = MutableStateFlow(listOf<Uri>())
    val uploadImages: StateFlow<List<Uri>> = _uploadImages.asStateFlow()
    fun updateUploadImages(newContent: List<Uri>) {
        _uploadImages.value = newContent
        //Log.d("aaaa","update gallery called")
    }


    private val _communityPostList = MutableStateFlow(SampleData.DefaultCommunityPostListSample)
    val communityPostList: StateFlow<CommunityPostList> = _communityPostList.asStateFlow()
    fun updateCommunityPostList(newContent: CommunityPostList) {
        _communityPostList.value = newContent
    }


    // 상태를 업데이트하는 함수입니다.


    private val _areaDetails: MutableStateFlow<List<SimpleAreaData>> = MutableStateFlow(listOf())
    val areaDetails: StateFlow<List<SimpleAreaData>> = _areaDetails.asStateFlow()

    private val _userInfo: MutableStateFlow<UserInfo> = MutableStateFlow(DefaultUserInfo)
    val userInfo: StateFlow<UserInfo> = _userInfo.asStateFlow()

    private val _currentRefAreaId: MutableStateFlow<MutableList<Int>> =
        MutableStateFlow(getRefAreaId().toMutableList())
    val currentRefAreaId: StateFlow<MutableList<Int>> = _currentRefAreaId.asStateFlow()

    suspend fun getUserInfo() {
        _userInfo.value = api.getUserInfo(getTokenHeader()!!)
    }

    suspend fun editProfile(request: EditProfileRequest) {
        _userInfo.value = api.putUserInfo(getTokenHeader()!!, request)
    }

    companion object {}

    fun getTokenHeader(): String? {
        //Log.d("aaaa", "tag:$accessToken")
        return prefRepository.getPref("token")?.let {
            "Bearer $it"
        } ?: ""
    }

    fun getOriginalToken(): String? {
        //Log.d("aaaa", "tag:$accessToken")
        return prefRepository.getPref("token")
    }

    fun setToken(token: String) {
        prefRepository.setPref("token", token)
    }

    fun getRefAreaId(): List<Int> {
        return prefRepository.getPref("refAreaId")?.trimEnd()?.split(" ")?.map {
            it.toInt()
        } ?: emptyList()
    }

    fun setRefAreaId(refAreaId: List<Int>) {
        val builder = StringBuilder()
        refAreaId.forEach {
            builder.append("$it ")
        }
        Log.d("aaaaa", builder.toString())
        prefRepository.setPref("refAreaId", builder.toString())
    }

    fun swapRefAreaIdValues() {

        // 0번째와 1번째 값 바꾸기
        if (_currentRefAreaId.value.size == 2) {
            val temp = _currentRefAreaId.value[0]
            _currentRefAreaId.value = _currentRefAreaId.value.toMutableList().apply {
                this[0] = _currentRefAreaId.value[1]
                this[1] = temp
            }
        }

        setRefAreaId(_currentRefAreaId.value)
    }
    /*
    fun getSelectedTabIndex():Int{
        return prefRepository.getPref("SelectedTabIndex")!!.toInt()
    }
    fun setSelectedTabIndex(index:Int){
        prefRepository.setPref("SelectedTabIndex",index.toString())
    }*/

    fun getGoodsPostList(item: GoodsPostPagingSource): Flow<PagingData<GoodsPostPreview>> {
        return Pager(
            config = PagingConfig(
                pageSize = 15,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                GoodsPostPagingSource(
                    api = api,
                    token = getTokenHeader()!!,
                    distance = item.distance,
                    areaId = item.areaId,
                )
            }
        ).flow
        /*
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
                    authToken=getToken()!!,
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
        }*/
    }

    fun getGoodsPostContent(id: Long) {

        Log.d("aaaa", "getGoodsPostContent called: authToken=${getTokenHeader()!!}, postId=$id")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = api.getGoodsPostContent(authToken = getTokenHeader()!!, postId = id)
                Log.d("aaaa", response.toString())

                withContext(Dispatchers.Main) {
                    updateGoodsPostContent(response)
                    //isgettingNewPostContent=false
                    //isgettingNewPostList.value=false
                }
            } catch (e: Exception) {
                //isgettingNewPostContent=false
                Log.d("aaaa", e.toString())
                ///////////////////////////////
                //updateGoodsPostContent(GoodsPostContentSample)
            }
        }
    }

    fun getCommunityPostList(distance: Int, areaId: Int) {
        Log.d("aaaa", "getCommunityPostList called:")
        if (communityPostList.value.count != 0 && communityPostList.value.isLast) {
            //Log.d("aaaa","getCommunityPostList canceled:$isgettingNewPostList,${communityPostList.value.isLast}")
            return
        }
        /*
        updateGoodsPostList(GoodsPostListSample)
        isgettingNewPostList.value=false*/

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = api.getCommunityPostList(
                    authToken = getTokenHeader()!!,
                    cur = communityPostList.value.cur,
                    seed = communityPostList.value.seed,
                    distance = distance,
                    areaId = areaId,
                    count = communityPostList.value.count
                )
                Log.d("aaaa", response.toString())

                withContext(Dispatchers.Main) {
                    updateCommunityPostList(response.copy(data = communityPostList.value.data + response.data))
                    //isgettingNewPostList=false
                }
            } catch (e: Exception) {
                //isgettingNewPostList=false
                Log.d("aaaa", "getCommunityPostList failed: $e")
                /////////////////////////////////////////
                //updateGoodsPostList(GoodsPostListSample)
            }
        }

    }

    suspend fun uploadImages(imageUris: List<Uri>, context: Context): postImagesResponse {
        Log.d("aaaa", imageUris.toString())
        return api.postImages(prepareMultiPartList(imageUris, context))
    }


    suspend fun wishToggle(id: Long, enable: Boolean) {
        Log.d("wish", "enable:$enable")
        api.wishToggle(authToken = getTokenHeader()!!, id, enable)
    }

    suspend fun submitPost(request: SubmitPostRequest) {
        api.submitPostRequest(authToken = getTokenHeader()!!, request)
    }

    suspend fun tryLogin(email: String, password: String): LoginResponse {
        return api.loginRequest(LoginRequest(email, password))
    }

    suspend fun trySignup(data: SignupRequest): SignupResponse {
        return api.signupRequest(data)
    }

    suspend fun tryCheckDuplicateNickname(nickName: String) {
        return api.checkDuplicateNickname(nickName)
    }

    suspend fun trySocialLogin(data: SocialLoginRequest): SocialLoginResponse {
        return api.socialLoginRequest(data, "kakao")
    }

    suspend fun trySocialSignUp(data: SocialSignupRequest): SignupRequest {
        return api.socialSignUpRequest(data, "kakao")
    }

    fun neededStoragePermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            arrayOf(
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.READ_MEDIA_VIDEO,
                android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.READ_MEDIA_VIDEO,
            )
        } else {
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    fun prepareMultiPartList(imageUris: List<Uri>, context: Context): List<MultipartBody.Part> {
        val parts: MutableList<MultipartBody.Part> = ArrayList()
        imageUris.forEach { uri ->
            parts.add(prepareFilePart("multipartFiles", uri, context))
        }
        return parts
    }

    fun prepareFilePart(partName: String, fileUri: Uri, context: Context): MultipartBody.Part {
        // Uri로부터 파일을 얻음
        val file = File(context.cacheDir, "tempFile_${System.currentTimeMillis()}.png")
        val inputStream = context.contentResolver.openInputStream(fileUri)
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)

        // 파일을 RequestBody로 생성
        val requestFile = file.asRequestBody(
            context.contentResolver.getType(fileUri)?.toMediaTypeOrNull()
        )

        // Part 이름과 파일 이름으로 MultipartBody.Part 생성
        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
    }

    suspend fun tryAreaSearch(query: String, cursor: Int): List<SimpleAreaData> {
        _areaDetails.value = api.areaSearch(query, cursor).areas;
        Log.d("VM", "${_areaDetails.value}")
        return _areaDetails.value;
    }

    fun initializeApp() {
        enableCallFirstGoodsPostList()
    }

    suspend fun getWishList() {
        _wishList.value = api.getWishList(getTokenHeader()!!)
    }


    fun CanCallFirstGoodsPostList(): Boolean {
        return prefRepository.getPref("CanCallGoodsPostList").toBoolean()
    }

    fun disableCallFirstGoodsPostList() {
        prefRepository.setPref("CanCallGoodsPostList", "false")
    }

    fun enableCallFirstGoodsPostList() {
        prefRepository.setPref("CanCallGoodsPostList", "true")
    }

    suspend fun getAreaName(id: Int): String {
        var name = ""
        try {
            viewModelScope.launch {
                name = api.getAreaName(id).name
            }
        } catch (e: HttpException) {
            name = " "
        }
        Log.d("aaaa", "$id-> $name")
        return name
    }
}