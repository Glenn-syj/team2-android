package com.wafflestudio.bunnybunny.pages

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.Navigation.findNavController
import com.wafflestudio.bunnybunny.R
import com.wafflestudio.bunnybunny.components.UI.dropShadow
import com.wafflestudio.bunnybunny.components.compose.LoginInputTextField
import com.wafflestudio.bunnybunny.components.compose.LoginPasswordTextField
import com.wafflestudio.bunnybunny.components.compose.MainWelcomeText
import com.wafflestudio.bunnybunny.data.example.SignupRequest
import com.wafflestudio.bunnybunny.data.example.SocialLoginRequest
import com.wafflestudio.bunnybunny.lib.network.dto.SocialLoginResponse
import com.wafflestudio.bunnybunny.lib.network.getKakaoOAuthToken
import com.wafflestudio.bunnybunny.viewModel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject


@Composable
fun StartPage(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    onNavigateToSignUp : () -> Unit,
    onNavigateToSocialSignUp: (idToken: String) -> Unit,
    onNavigateToSignIn : () -> Unit,
    //onNavigateToKakaoSignIn : () -> Unit,
    //onNavigateToGoogleSignIn : () -> Unit,
    //onNavigateToMain : () -> Unit
) {

    var emailInput by rememberSaveable { mutableStateOf("example7831@gmail.com") }
    var pwInput by rememberSaveable { mutableStateOf("hjlim7831@@") }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column (
            modifier = Modifier.fillMaxHeight(0.45f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.bunnybunny_square),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(0.4f)
                    .aspectRatio(1f)
                    .clip(CircleShape)
            )
            MainWelcomeText()
        }

        Column ( modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            //ID Input
            LoginInputTextField(
                value = emailInput,
                onValueChange = { newText -> emailInput = newText },
                placeholder = "이메일을 입력해주세요",
            )

            //PW Input
            LoginPasswordTextField (
                pwInput = pwInput,
                onPwChange = { newText -> pwInput = newText },
                onLoginClick = {
                    // Perform login
                    CoroutineScope(Dispatchers.IO).launch {
                        val loginRequest = viewModel.tryLogin(emailInput, pwInput)
                        withContext(Dispatchers.Main) {
                        }
                    }
                }
            )

            Column {
                Button(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(20),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(1.0f, 0.647f, 0.0f, 1.0f)),
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val loginResponse = viewModel.tryLogin(emailInput, pwInput)
                                Log.d("loginResponse", "${loginResponse}")
                                withContext(Dispatchers.Main) {
                                    if (loginResponse != null) {
                                        viewModel.setToken(loginResponse.token)
                                        Log.d("StartPage", "${viewModel.getTokenHeader()}")
                                        viewModel.setRefAreaId(loginResponse.refAreaIds)
                                        onNavigateToSignIn()
                                    }
                                }
                            } catch (e: HttpException) {
                                val message = e.response()?.code().toString()
                                if (message == "401") {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "존재하지 않거나 잘못된 사용자 정보입니다.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                Log.d("StartPage", "error: $message")

                            }
                        }
                    }
                ) {
                    Text("로그인", softWrap = true)
                } //Sign up Button

            }
            //ID & PW Sign in Button
            Row (
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth(0.4f),
                    shape = RoundedCornerShape(20),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(1.0f, 0.647f, 0.0f, 1.0f)),
                    onClick = { onNavigateToSignUp() }
                ) {
                    Text("회원가입")
                }

                //Kakao Sign-in Button
                Image(painter = painterResource(id = R.drawable.kakao_login_large_narrow),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .clickable {
                            CoroutineScope(Dispatchers.IO).launch {
                                val oAuthToken = getKakaoOAuthToken(context)
                                try {
                                    if (oAuthToken != null) {
                                        val socialLoginResponse = viewModel.trySocialLogin(SocialLoginRequest(oAuthToken.idToken!!))
                                        Log.d("socialLoginResponse", "${socialLoginResponse}")
                                        viewModel.setRefAreaId(socialLoginResponse.refAreaIds)
                                        viewModel.setToken(socialLoginResponse.token)
                                        withContext(Dispatchers.Main) {

                                            onNavigateToSignIn()
                                        }
                                    } else {
                                        withContext(Dispatchers.Main) {
                                            Toast
                                                .makeText(context, "토큰이 만료되었습니다", Toast.LENGTH_SHORT)
                                                .show()
                                        }
                                    }
                                } catch (e: HttpException) {
                                    withContext(Dispatchers.Main) {
                                        val idToken = oAuthToken!!.idToken
                                        Toast
                                            .makeText(context, "회원가입부터 진행해주세요!", Toast.LENGTH_SHORT)
                                            .show()
                                        onNavigateToSocialSignUp(idToken!!)
                                    }
                                }

                            }
                        }
                )
            }

        }


    }
}




//        }
//        Column(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {



            //Google Sign-in Button
//            Button(
//                modifier = Modifier.padding(12.dp),
//                onClick = {print("a")},
//                colors = ButtonDefaults.buttonColors(
//                    Color.White,
//                    contentColor = Color.Black
//                )
//            ){
//                Text("G")
//            }
//        }
//    }

//}