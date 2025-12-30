package com.muratcangzm.nerva.app

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.muratcangzm.nerva.app.nav.NervaNavHost

@Composable
fun AppRoot() {
    Surface {
        NervaNavHost()
    }
}