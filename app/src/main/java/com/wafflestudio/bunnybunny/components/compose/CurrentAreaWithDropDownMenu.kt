package com.wafflestudio.bunnybunny.components.compose

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.wafflestudio.bunnybunny.viewModel.MainViewModel


@Composable
fun RowScope.CurrentAreaWithDropDownMenu(viewModel: MainViewModel) {

    var isDropDownMenuExpanded by remember { mutableStateOf (false)}
    val currentRefAreaId by viewModel.currentRefAreaId.collectAsState()

    var firstAreaName by remember { mutableStateOf("") }
    var secondAreaName by remember { mutableStateOf("") }

    LaunchedEffect(currentRefAreaId) {
        // 비동기로 지역명 가져오는 함수 (예시로 delay 사용)
        firstAreaName = viewModel.getAreaName(currentRefAreaId[0])
        if (currentRefAreaId.size == 2) {
            secondAreaName = viewModel.getAreaName(currentRefAreaId[1])
        }
    }

    Column (modifier = Modifier.clickable {
        isDropDownMenuExpanded = true
    }.weight(1f).background(Color.Yellow)) {
        Text(modifier = Modifier
            ,
            text = firstAreaName)

        if (viewModel.getRefAreaId().size == 2) {

            DropdownMenu (
                modifier = Modifier.wrapContentSize(),
                expanded = isDropDownMenuExpanded,
                onDismissRequest = { isDropDownMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    onClick = {
                        Log.d("CAWDD", "clicked")
                    },
                    text = { DropDownText(modifier = Modifier, text = firstAreaName)
                        }
                )
                DropdownMenuItem(
                    onClick = {
                        // 오직 2번 항목이 있을
                        viewModel.swapRefAreaIdValues()
                    },
                    text = { DropDownText(modifier = Modifier, text = secondAreaName)}
                )
                DropdownMenuItem(
                    onClick = {
                        // 지역 설정 페이지 관련으로 이동
                    },
                    text = { DropDownText(modifier = Modifier, text = "지역 설정") }
                )
            }
        }
    }


}