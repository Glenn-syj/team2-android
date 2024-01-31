package com.wafflestudio.bunnybunny.pages

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Diversity3
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import coil.compose.rememberImagePainter
import com.wafflestudio.bunnybunny.components.UI.bunnyColor
import com.wafflestudio.bunnybunny.components.compose.ChatContents
import com.wafflestudio.bunnybunny.components.compose.CurrentAreaWithDropDownMenu
import com.wafflestudio.bunnybunny.components.compose.LoginInputTextField
import com.wafflestudio.bunnybunny.components.compose.NotificationsButton
import com.wafflestudio.bunnybunny.components.compose.PersonButton
import com.wafflestudio.bunnybunny.components.compose.SearchButton
import com.wafflestudio.bunnybunny.components.compose.SettingsButton
import com.wafflestudio.bunnybunny.model.BottomNavItem
import com.wafflestudio.bunnybunny.utils.formatProductTime
import com.wafflestudio.bunnybunny.viewModel.ComunityViewModel
import com.wafflestudio.bunnybunny.viewModel.MainViewModel
import com.wafflestudio.bunnybunny.data.example.EditProfileRequest
import com.wafflestudio.bunnybunny.viewModel.ChatViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val homeTab = BottomNavItem(tag = "홈", title = "Home", selectedIcon = Icons.Filled.Home, unselectedIcon = Icons.Outlined.Home)
val communityTab = BottomNavItem(tag="동네생활", title="Community", selectedIcon = Icons.Filled.Diversity3, unselectedIcon = Icons.Outlined.Diversity3)
val chatTab = BottomNavItem(tag = "채팅",title="Chat", selectedIcon = Icons.Filled.Forum, unselectedIcon = Icons.Outlined.Forum)
val myTab = BottomNavItem(tag = "나의 당근",title="My", selectedIcon = Icons.Filled.Person, unselectedIcon = Icons.Outlined.Person)

// creating a list of all the tabs
val tabBarItems = listOf(homeTab, communityTab, chatTab, myTab)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabPage(index:Int?=null,chatViewModel: ChatViewModel, navController: NavController){

    val viewModel = hiltViewModel<MainViewModel>()
    val selectedTabIndex= remember {
        mutableIntStateOf(0)
    }
    if(index!=null) selectedTabIndex.intValue=index

    val homePagelistState = rememberLazyListState()


    val token = viewModel.getOriginalToken()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(token) {
        try {
            coroutineScope.launch {
                chatViewModel.connectToUser()
                chatViewModel.sendRecentMessagesFormattedStringRequest(255)
                delay(200)
                chatViewModel.sendRecentMessagesFormattedStringRequest(255)
            }
        } catch (e: Exception) {
            Log.d("CHAT", e.message!!)
        }
    }

    //viewModel.currentTab.value=tabName
    Scaffold(bottomBar = { TabNavigationBar(selectedTabIndex,tabBarItems) }, topBar = { TabPageToolBar(selectedTabIndex,navController)}) {paddingValues->
        Box(
            Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues),
        ) {
            
            when(selectedTabIndex.intValue){
                0-> {
                    HomeTabPageView( listState = homePagelistState,navController = navController)
                    WritePostButton(modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)){

                        navController.navigate("WriteGoodsPostPage")
                    }
                }
                1-> {
                    CommunityTabPageView( navController)
                    WritePostButton(modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)){

                        //navController.navigate(대출 동네생활 글쓰기 페이지)
                    }
                }
                2-> ChatTabPageView(chatViewModel, navController)
                3-> MyTabPageView(viewModel = viewModel, navController = navController)
            }
        }
    }


}

@Composable
fun TabNavigationBar(selectedTabIndex:MutableState<Int>,tabBarItems: List<BottomNavItem>) {
    NavigationBar {
        // looping over each tab to generate the views and navigation for each item
        tabBarItems.forEachIndexed { index, tabBarItem ->
            NavigationBarItem(
                selected = selectedTabIndex.value == index,
                onClick = {
                    selectedTabIndex.value = index
                },
                icon = {
                    TabBarIconView(
                        isSelected = selectedTabIndex.value == index,
                        selectedIcon = tabBarItem.selectedIcon,
                        unselectedIcon = tabBarItem.unselectedIcon,
                        title = tabBarItem.title,
                        badgeAmount = tabBarItem.badgeAmount
                    )
                },
                label = {Text(tabBarItem.tag)})
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabBarIconView(
    isSelected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    title: String,
    badgeAmount: Int? = null
) {
    BadgedBox(badge = { TabBarBadgeView(badgeAmount) }) {
        Icon(
            imageVector = if (isSelected) {selectedIcon} else {unselectedIcon},
            contentDescription = title
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TabBarBadgeView(count: Int? = null) {
    if (count != null) {
        Badge {
            Text(count.toString())
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabPageToolBar(selectedTabIndex:MutableState<Int>,navController: NavController) {

    val viewModel = hiltViewModel<MainViewModel>()

    Column {
        TopAppBar(
            title = {
                    Text(text=when(selectedTabIndex.value){
                        0->""
                        1->""
                        2->"채팅"
                        3->"나의당근"
                        else->""
                    })
            },
            navigationIcon = {
                Row{

                }
            },
            actions = {
                when(selectedTabIndex.value){
                    0-> {
                        CurrentAreaWithDropDownMenu(viewModel = viewModel)
                        SearchButton()
                        NotificationsButton()
                    }
                    1-> {
                        CurrentAreaWithDropDownMenu(viewModel = viewModel)
                        PersonButton()
                        SearchButton()
                        NotificationsButton()
                    }
                    2-> {
                        NotificationsButton()
                    }
                    3-> {
                        SettingsButton()
                    }
                }

            },
            colors = TopAppBarDefaults.smallTopAppBarColors(
                navigationIconContentColor = Color.Black,
                titleContentColor = Color.Black, // Color for the title
                actionIconContentColor = Color.Black // Color for action icons
            ),
        )
        Divider(
            Modifier
                .fillMaxWidth()
                .height(1.dp))
    }


}

@Composable
fun WritePostButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        modifier = modifier,
        onClick = { onClick() },
        icon = { Icon(Icons.Filled.Add, "Edit") },
        text = { Text(text = "글쓰기") },
        contentColor = Color.White,
        containerColor = bunnyColor,
        shape = CircleShape
    )
}
@Composable
fun HomeTabPageView(listState:LazyListState,navController: NavController){
    val viewModel = hiltViewModel<MainViewModel>()
    LaunchedEffect(key1 = true){
        if(viewModel.CanCallFirstGoodsPostList()){
            viewModel.disableCallFirstGoodsPostList()
            Log.d("aaaa", "updateGoodsPostList called in HomeTabPageView LaunchedEffect")
            viewModel.updateGoodsPostList(
                distance = 0,
                areaId = viewModel.getRefAreaId()[0],
                count=0,cur=null,seed=null)
        }
    }
    val itemList = viewModel.goodsPostList.collectAsLazyPagingItems()
    Log.d("aaaa123", "vcalled")



    LazyColumn(state = listState){
        item {
            //물품 필터
        }

        items(itemList){
            //Log.d("aaaa123", it.toString())
            it ?: return@items

            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp)
                .clickable {
                    Log.d("aaaa", it.id.toString())
                    //Log.d("aaaa","GoodsPostPage/${it.id}")
                    navController.navigate("GoodsPostPage/${it.id}")
                }
            ){
                Row(Modifier.align(Alignment.CenterStart)) {
                    val painter = rememberImagePainter(data = it.repImg)

                    Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .border(
                                1.dp,
                                Color.Gray.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(corner = CornerSize(8.dp))
                            )
                            .clip(RoundedCornerShape(corner = CornerSize(8.dp)))
                            .clipToBounds(),
                        contentScale = ContentScale.Crop,
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(text = it.title, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = it.tradingLocation+"·"+ formatProductTime(it.createdAt, it.refreshedAt), color = Color.Gray, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = it.sellPrice.toString()+"원", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        if (it.wishCnt > 0 || it.chatCnt > 0) {
                            Text(text = (if (it.wishCnt > 0) "관심 " + it.wishCnt.toString() else "") + (if (it.chatCnt > 0) "채팅 " + it.chatCnt.toString() else ""))
                        }
                    }
                }
            }
            Divider(
                color = Color.Gray.copy(alpha = 0.2f),
                modifier = Modifier
                    .height(1.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }

    }

}
@Composable
fun CommunityTabPageView(navController: NavController){
    val viewModel = hiltViewModel<ComunityViewModel>()
    val itemList = viewModel.communityPostList.collectAsLazyPagingItems()
    val listState = rememberLazyListState()

    LaunchedEffect(key1 = true){
        Log.d("aaaa123", "hihihi")
        viewModel.updateCommunityPostList(
            distance = 0,
            areaId = viewModel.getRefAreaId()[0],
            count=0,cur=null,seed=null)
    }
    LazyColumn(state = listState){
        item {
            //물품 필터
        }
        items(itemList){
            it ?: return@items
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(start = 16.dp, end = 16.dp)
                .clickable {
                    //Log.d("aaaa", it.id.toString())
                    //Log.d("aaaa","GoodsPostPage/${it.id}")
                    //navController.navigate("GoodsPostPage/${it.id}")
                }
            ){
                Column{
                    Row {
                        Column{
                            Text(it.title)
                            Text(it.description, overflow = TextOverflow.Clip, maxLines = 1)

                        }
                        val painter = rememberImagePainter(data = it.repImg)
                        Image(
                            painter = painter,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(100.dp)
                        )

                    }

                    Row {
                        Text("${it.areaId}·${it.createdAt}·조회${it.viewCnt}")
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(imageVector = Icons.Outlined.ThumbUp, contentDescription ="like" )
                        Text(it.likeCnt.toString())
                        Icon(imageVector = Icons.Outlined.ChatBubbleOutline, contentDescription ="chat" )
                        Text(it.chatCnt.toString())
                    }
                }
            }
            Divider(
                Modifier
                    .height(1.dp)
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp))
        }

    }

}
@Composable
fun ChatTabPageView(chatViewModel: ChatViewModel, navController: NavController){
    chatViewModel.getChannelList()


    ChatContents(modifier = Modifier, viewModel = chatViewModel, navController = navController)
//    ChatRoomScreen(viewModel = chatViewModel)
}
@Composable
fun MyTabPageView(
    viewModel: MainViewModel,
    navController: NavController
){
    LaunchedEffect(Dispatchers.IO){
        viewModel.getUserInfo()
    }
    val user = viewModel.userInfo.collectAsState().value
    Column{
        Box(
            modifier = Modifier
                .height(150.dp)
                .background(Color.Yellow)
                .clickable {
                    navController.navigate("ProfilePage")
                }
        ) {
            Row {
                val painter = rememberImagePainter(data = user.profileImageUrl)
                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(100.dp)
                )
                Text(user.nickname)
            }
        }
        Box(modifier = Modifier
            .padding(5.dp)
            .background(Color.Black)
            .clickable {
                navController.navigate("WishListPage")
            }) {
            Text("관심목록")
        }
    }
}

@Composable
fun WishListPage(viewModel: MainViewModel, navController: NavController){
    val listState = rememberLazyListState()

    LaunchedEffect(viewModel.wishList){
        viewModel.getWishList()
    }


    LazyColumn(state = listState){
        item {
            //물품 필터
        }
        items(viewModel.wishList.value){
            Log.d("aaaa",viewModel.wishList.toString())
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(start = 16.dp, end = 16.dp)
                .clickable {
                    Log.d("aaaa", it.id.toString())
                    //Log.d("aaaa","GoodsPostPage/${it.id}")
                    navController.navigate("GoodsPostPage/${it.id}")
                }
            ){
                Row {
                    val painter = rememberImagePainter(data = it.repImg)
                    Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(100.dp)
                    )
                    Column {
                        Text(text = it.title)
                        Text(text = it.tradingLocation+"·"+it.refreshedAt)
                        Text(text = it.sellPrice.toString()+"원")
                        Text(text = (if(it.wishCnt>0)"관심 "+it.wishCnt.toString() else "")+(if(it.chatCnt>0)"채팅 "+it.chatCnt.toString() else ""))
                    }
                }
            }
            Divider(
                Modifier
                    .height(1.dp)
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp))
        }

    }
}

@Composable
fun ProfilePage(viewModel: MainViewModel, navController: NavController){
    val user = viewModel.userInfo.collectAsState().value
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            val painter = rememberImagePainter(data = user.profileImageUrl)
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .padding(10.dp)
                    .width(100.dp)
                    .height(100.dp)
                    .clip(CircleShape)
            )
            Text(
                text = user.nickname,
                modifier = Modifier.padding(20.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
//            Text("${user.mannerTemp}", modifier = Modifier.padding(10.dp))
        }
        Button(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            onClick = {navController.navigate("ProfileEditPage")},
            colors = ButtonDefaults.buttonColors(Color(0xFFFF6822)),
            shape = RectangleShape
        ){
            Text("프로필 수정")
        }

        // 매너 온도 텍스트
        Text(
            text = "${user.mannerTemp} °C",
            modifier = Modifier
                .padding(10.dp)
                .align(Alignment.End),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        // 추후 온도 통일
//        val temp = goodsPostContent.authorMannerTemperature
//        val color = calculateMannerTempColor(temp)
//        val normalizedTemp = (temp - 30).coerceIn(0.0, 15.0) / 15f
//
//        LinearProgressIndicator(
//            progress = normalizedTemp.toFloat(), // Normalize to 0.0 to 1.0
//            modifier = Modifier
//                .width(48.dp)
//                .clip(CircleShape),
//            color = color
//        )
        // 가로 막대 그래프
        LinearProgressIndicator(
            progress = user.mannerTemp.toFloat() / 100f, // 매너 온도를 0~1 사이의 값으로 정규화
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .padding(vertical = 8.dp, horizontal = 10.dp)
                .background(color = Color.Gray.copy(alpha = 0.3f))
        )
    }

}

@Composable
fun ProfileEditPage(viewModel: MainViewModel, navController: NavController){
    val user = viewModel.userInfo.collectAsState().value
    /*var newPassword by rememberSaveable {
        mutableStateOf(user.)
    }*/
    var newNickname by rememberSaveable {
        mutableStateOf(user.nickname)
    }
    Column(){
        val painter = rememberImagePainter(data = user.profileImageUrl)
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .padding(10.dp)
                .background(Color.Yellow)
                .width(100.dp)
                .height(100.dp)
        )
        /*LoginInputTextField(
            value = newNickname,
            onValueChange = {newText -> newNickname = newText},
            placeholder = newNickname)*/
        LoginInputTextField(
            value = newNickname,
            onValueChange = {newText -> newNickname = newText},
            placeholder = newNickname,)
        Button(
            onClick = {
                Log.d("aaaa",viewModel.userInfo.value.toString())
                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.editProfile(EditProfileRequest("exampletest1!",newNickname,"https://mblogthumb-phinf.pstatic.net/MjAyMTAyMDRfMjcz/MDAxNjEyNDA5MDEyMjg0.lIRX6wm7X3nPYaviwnUFyLm5dC88Mggadj_nglswSHsg.r9so4CS-g8VZGAoaRWrwmPCIuDOsgsU64fQu0kKQRTwg.JPEG.sunny_side_up12/1612312679152%EF%BC%8D11.jpg?type=w800"))
                }
                Log.d("aaaa",viewModel.userInfo.value.toString())
            }
        ){
            Text("완료")
        }
    }
}